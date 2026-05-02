package app.kehdo.data.conversation

import android.content.Context
import android.net.Uri
import app.kehdo.core.common.KehdoError
import app.kehdo.core.common.Outcome
import app.kehdo.core.network.api.ConversationApi
import app.kehdo.core.network.api.dto.GenerateRequestDto
import app.kehdo.core.network.di.UnauthenticatedHttpClient
import app.kehdo.data.conversation.mapper.ConversationMappers
import app.kehdo.domain.conversation.Conversation
import app.kehdo.domain.conversation.ConversationRepository
import app.kehdo.domain.conversation.ConversationStatus
import app.kehdo.domain.conversation.HistoryPage
import app.kehdo.domain.conversation.Tone
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Retrofit-backed [ConversationRepository] talking to the kehdo backend
 * at `BuildConfig.API_BASE_URL`.
 *
 * Conversation lifecycle:
 *   1. [createConversation] → backend reserves a row + returns a presigned
 *      PUT URL.
 *   2. We upload the screenshot bytes directly to that URL (OkHttp, no auth
 *      header — adding one would invalidate the S3 signature).
 *   3. [generateReplies] → backend runs OCR + LLM and returns the populated
 *      conversation.
 *
 * Local cache: a `MutableStateFlow<Map<id, Conversation>>` mirrors the
 * [FakeConversationRepository] so the reply screen's `observe(id)` keeps
 * working with the same Flow contract. Copy / favorite are kept local
 * for now — the backend doesn't yet expose interaction-signal endpoints
 * for replies; ADR 0006 schedules that with the data flywheel.
 *
 * History (`getHistoryPage`) is backed by `GET /conversations` with
 * cursor-based pagination; `deleteConversation` calls `DELETE
 * /conversations/{id}` (soft delete, hard-removed by the nightly
 * cleanup job after 30 days). Local cache is updated on success so the
 * Reply screen's Flow observers see the eviction immediately.
 */
@Singleton
class RealConversationRepository @Inject constructor(
    private val api: ConversationApi,
    @UnauthenticatedHttpClient private val uploadClient: OkHttpClient,
    @ApplicationContext private val context: Context
) : ConversationRepository {

    private val _conversations = MutableStateFlow<Map<String, Conversation>>(emptyMap())

    override suspend fun getTones(): Outcome<List<Tone>> = runNetwork {
        api.getTones().mapIndexed { index, dto -> ConversationMappers.fromTone(dto, index) }
    }

    override suspend fun createConversation(screenshotUri: String): Outcome<Conversation> {
        val reservation = runNetwork { api.createConversation() }
        if (reservation is Outcome.Failure) return reservation

        val payload = (reservation as Outcome.Success).value
        // Read the screenshot from the local content URI and PUT it to the
        // presigned URL. Failure here means the conversation row exists in
        // the backend but has no upload — backend treats it as PENDING_UPLOAD
        // until generate is called, which will 409 if we never PUT.
        val uploadResult = uploadScreenshot(payload.uploadUrl, Uri.parse(screenshotUri))
        if (uploadResult is Outcome.Failure) return uploadResult

        val now = System.currentTimeMillis()
        val conversation = Conversation(
            id = payload.conversationId,
            // After a successful PUT the backend will move us into PROCESSING
            // on the next /generate call — we model that here so the UI can
            // show "working on it…" immediately.
            status = ConversationStatus.PROCESSING,
            failureReason = null,
            toneCode = null,
            replies = emptyList(),
            createdAt = now,
            updatedAt = now
        )
        _conversations.update { it + (conversation.id to conversation) }
        return Outcome.success(conversation)
    }

    override suspend fun generateReplies(
        conversationId: String,
        toneCode: String
    ): Outcome<Conversation> {
        val result = runNetwork {
            api.generateReplies(conversationId, GenerateRequestDto(tone = toneCode))
        }
        if (result is Outcome.Success) {
            val conversation = ConversationMappers.fromGenerateResponse(result.value)
            _conversations.update { it + (conversation.id to conversation) }
            return Outcome.success(conversation)
        }
        return result as Outcome.Failure
    }

    override fun observe(conversationId: String): Flow<Conversation?> =
        _conversations.asStateFlow().map { it[conversationId] }

    override suspend fun markCopied(replyId: String): Outcome<Unit> {
        // Local-only for now — interaction-signal endpoint lands with the
        // ADR 0006 data flywheel. Mutate the cached conversation so the
        // UI reflects the action immediately.
        _conversations.update { snapshot ->
            snapshot.mapValues { (_, conv) ->
                conv.copy(
                    replies = conv.replies.map {
                        if (it.id == replyId) it.copy(isCopied = true) else it
                    }
                )
            }
        }
        return Outcome.success(Unit)
    }

    override suspend fun toggleFavorite(replyId: String): Outcome<Unit> {
        _conversations.update { snapshot ->
            snapshot.mapValues { (_, conv) ->
                conv.copy(
                    replies = conv.replies.map {
                        if (it.id == replyId) it.copy(isFavorited = !it.isFavorited) else it
                    }
                )
            }
        }
        return Outcome.success(Unit)
    }

    override fun observeRecent(limit: Int): Flow<List<Conversation>> =
        _conversations.asStateFlow().map { snapshot ->
            snapshot.values.sortedByDescending { it.createdAt }.take(limit)
        }

    override suspend fun getHistoryPage(limit: Int, cursor: String?): Outcome<HistoryPage> = runNetwork {
        val page = ConversationMappers.fromHistoryPage(api.listConversations(limit, cursor))
        // Hydrate the local cache so the Reply screen's observe() Flow
        // can pick up rows the user opens from the history list. We only
        // overwrite cache entries we don't already have replies for —
        // the history endpoint doesn't return replies, so blindly
        // overwriting would erase locally-cached generation results.
        _conversations.update { snapshot ->
            val merged = snapshot.toMutableMap()
            page.items.forEach { conv ->
                if (snapshot[conv.id]?.replies?.isEmpty() != false) {
                    merged[conv.id] = conv
                }
            }
            merged
        }
        page
    }

    override suspend fun deleteConversation(conversationId: String): Outcome<Unit> {
        val result = runNetwork {
            api.deleteConversation(conversationId)
            Unit
        }
        if (result is Outcome.Success) {
            _conversations.update { it - conversationId }
        }
        return result
    }

    // ---- helpers ----------------------------------------------------------

    private suspend fun uploadScreenshot(presignedUrl: String, uri: Uri): Outcome<Unit> {
        return try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return Outcome.failure(
                    KehdoError.ParsingFailed("Couldn't read screenshot bytes from $uri")
                )
            // Backend signed the URL with image content type; sending a
            // different type would fail the signature. Match exactly.
            val mediaType = "image/*".toMediaTypeOrNull()
            val request = Request.Builder()
                .url(presignedUrl)
                .put(bytes.toRequestBody(mediaType))
                .build()
            val response = uploadClient.newCall(request).execute()
            response.use {
                if (!it.isSuccessful) {
                    return Outcome.failure(
                        KehdoError.Server(
                            code = "UPLOAD_FAILED_${it.code}",
                            message = "Screenshot upload rejected by storage"
                        )
                    )
                }
            }
            Outcome.success(Unit)
        } catch (io: IOException) {
            Outcome.failure(KehdoError.Network(io))
        }
    }

    private inline fun <T> runNetwork(block: () -> T): Outcome<T> = try {
        Outcome.success(block())
    } catch (io: IOException) {
        Outcome.failure(KehdoError.Network(io))
    } catch (http: HttpException) {
        Outcome.failure(httpToDomain(http))
    } catch (t: Throwable) {
        Outcome.failure(KehdoError.Unknown(t))
    }

    private fun httpToDomain(e: HttpException): KehdoError = when (e.code()) {
        401 -> KehdoError.Unauthorized(e)
        402 -> KehdoError.RateLimit(limit = 0, resetAt = 0L)
        422 -> KehdoError.GenerationFailed(reason = "CONTENT_BLOCKED")
        else -> KehdoError.Server(code = "HTTP_${e.code()}", message = e.message())
    }
}
