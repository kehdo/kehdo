package app.kehdo.data.conversation

import app.kehdo.core.common.Outcome
import app.kehdo.domain.conversation.Conversation
import app.kehdo.domain.conversation.ConversationRepository
import app.kehdo.domain.conversation.ConversationStatus
import app.kehdo.domain.conversation.HistoryPage
import app.kehdo.domain.conversation.Mode
import app.kehdo.domain.conversation.Reply
import app.kehdo.domain.conversation.Tone
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory fake of [ConversationRepository] used until Phase-4 backend
 * endpoints (POST /v1/conversations, /generate, /refine, GET /v1/tones)
 * are implemented. Returns plausible canned data with simulated latency
 * so the Android UI flow is fully exercisable end-to-end today.
 *
 * Will be replaced by RealConversationRepository (Retrofit-backed) in a
 * follow-up PR once the backend is live. The Hilt @Binds in
 * [di.ConversationModule] is the only place that needs to flip.
 */
@Singleton
class FakeConversationRepository @Inject constructor() : ConversationRepository {

    private val _conversations = MutableStateFlow<Map<String, Conversation>>(emptyMap())

    override suspend fun getTones(): Outcome<List<Tone>> {
        delay(150)
        return Outcome.success(SEED_TONES)
    }

    override suspend fun createConversation(screenshotUri: String): Outcome<Conversation> {
        delay(800) // simulate upload latency
        val now = System.currentTimeMillis()
        val conversation = Conversation(
            id = UUID.randomUUID().toString(),
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
        delay(1200) // simulate Gemini Flash inference latency
        val existing = _conversations.value[conversationId]
            ?: return Outcome.success(
                Conversation(
                    id = conversationId,
                    status = ConversationStatus.FAILED,
                    failureReason = "CONVERSATION_NOT_FOUND",
                    toneCode = toneCode,
                    replies = emptyList(),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )

        val now = System.currentTimeMillis()
        val replies = sampleRepliesFor(toneCode, conversationId)
        val updated = existing.copy(
            status = ConversationStatus.READY,
            toneCode = toneCode,
            replies = replies,
            updatedAt = now
        )
        _conversations.update { it + (conversationId to updated) }
        return Outcome.success(updated)
    }

    override fun observe(conversationId: String): Flow<Conversation?> =
        _conversations.asStateFlow().map { it[conversationId] }

    override suspend fun markCopied(replyId: String): Outcome<Unit> {
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
            snapshot.values
                .sortedByDescending { it.createdAt }
                .take(limit)
        }

    override suspend fun getHistoryPage(limit: Int, cursor: String?): Outcome<HistoryPage> {
        delay(120) // simulate a network round-trip
        val sorted = _conversations.value.values
            .sortedByDescending { it.createdAt }
        // Cursor is just an integer offset string in the Fake — opaque to
        // the caller, just like the real backend's base64 cursor would be.
        val offset = cursor?.toIntOrNull() ?: 0
        val slice = sorted.drop(offset).take(limit)
        val nextCursor = if (offset + slice.size < sorted.size) (offset + slice.size).toString() else null
        return Outcome.success(HistoryPage(items = slice, nextCursor = nextCursor))
    }

    override suspend fun deleteConversation(conversationId: String): Outcome<Unit> {
        _conversations.update { it - conversationId }
        return Outcome.success(Unit)
    }

    private fun sampleRepliesFor(toneCode: String, conversationId: String): List<Reply> {
        val now = System.currentTimeMillis()
        val texts = SAMPLE_REPLIES[toneCode] ?: SAMPLE_REPLIES.getValue("CASUAL")
        return texts.mapIndexed { index, text ->
            Reply(
                id = UUID.randomUUID().toString(),
                conversationId = conversationId,
                rank = index + 1,
                text = text,
                toneCode = toneCode,
                isFavorited = false,
                isCopied = false,
                createdAt = now
            )
        }
    }

    companion object {
        private val SEED_TONES = listOf(
            Tone("CASUAL",     "Casual",     "💬", "Relaxed, everyday",       Mode.CASUAL,       false, 1),
            Tone("PLAYFUL",    "Playful",    "🎈", "Fun, light",              Mode.CASUAL,       true,  2),
            Tone("WITTY",      "Witty",      "✨", "Sharp, clever",           Mode.CASUAL,       false, 3),
            Tone("FLIRTY",     "Flirty",     "😉", "Playful, charming",       Mode.FLIRTY,       false, 4),
            Tone("POETIC",     "Poetic",     "🖋", "Literary, evocative",     Mode.FLIRTY,       true,  5),
            Tone("SARCASTIC",  "Sarcastic",  "🎭", "Dry, ironic",             Mode.FLIRTY,       true,  6),
            Tone("FORMAL",     "Formal",     "💼", "Polished, professional",  Mode.PROFESSIONAL, false, 7),
            Tone("DIRECT",     "Direct",     "⚡", "Clear, no-nonsense",      Mode.PROFESSIONAL, false, 8),
            Tone("CONFIDENT",  "Confident",  "👑", "Self-assured",            Mode.PROFESSIONAL, true,  9),
            Tone("WARM",       "Warm",       "🙏", "Sincere, kind",           Mode.SINCERE,      false, 10),
            Tone("GRATEFUL",   "Grateful",   "💕", "Appreciative, heartfelt", Mode.SINCERE,      true,  11),
            Tone("THOUGHTFUL", "Thoughtful", "💭", "Considered, deep",        Mode.SINCERE,      true,  12)
        )

        private val SAMPLE_REPLIES = mapOf(
            "CASUAL" to listOf(
                "haha yeah for sure, count me in!",
                "sounds good, what time were you thinking?",
                "ya let's do it 🙌"
            ),
            "FLIRTY" to listOf(
                "well now you've got my attention 😏",
                "is that so? tell me more...",
                "hmm, you're trouble — I like it 😉"
            ),
            "PROFESSIONAL" to listOf(
                "Sounds good — let's lock that in.",
                "Happy to help. What works best for you?",
                "Got it. I'll send a calendar invite shortly."
            ),
            "FORMAL" to listOf(
                "Thank you — that works for me.",
                "I appreciate the update. I'll review and respond.",
                "Understood. Please proceed as planned."
            ),
            "WARM" to listOf(
                "That means a lot, thank you 💕",
                "I'm so glad you reached out.",
                "Sending you a hug — talk soon."
            ),
            "WITTY" to listOf(
                "Bold of you to assume I have plans.",
                "Plot twist: I was thinking the same thing.",
                "I'd say no, but you make a compelling case."
            )
        )
    }
}
