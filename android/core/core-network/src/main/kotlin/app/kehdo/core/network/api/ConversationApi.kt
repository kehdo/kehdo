package app.kehdo.core.network.api

import app.kehdo.core.network.api.dto.ConversationDto
import app.kehdo.core.network.api.dto.ConversationPageDto
import app.kehdo.core.network.api.dto.CreateConversationResponseDto
import app.kehdo.core.network.api.dto.GenerateRequestDto
import app.kehdo.core.network.api.dto.GenerateResponseDto
import app.kehdo.core.network.api.dto.ToneDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit binding for the conversation + reply + tone + usage endpoints
 * — see `/contracts/openapi/kehdo.v1.yaml`.
 *
 * Hand-rolled rather than from `:core:network-generated` so PR 14 can land
 * before the OpenAPI generator wiring (still TODO from Phase 0). When the
 * generated client lands, this file deletes and `:data:conversation`
 * imports the generated equivalents.
 *
 * The screenshot upload is NOT a Retrofit call — clients PUT bytes
 * directly to the presigned `uploadUrl` returned by `createConversation`,
 * using OkHttp directly (no auth header, since the URL is pre-signed).
 */
interface ConversationApi {

    /**
     * Reserve a conversation row + presigned PUT URL. The client uploads
     * the screenshot bytes to `uploadUrl` (using OkHttp, not Retrofit)
     * before calling [generate].
     */
    @POST("conversations")
    suspend fun createConversation(): CreateConversationResponseDto

    /**
     * Trigger OCR + LLM pipeline for an already-uploaded screenshot.
     * Counts against the daily quota; first call moves status from
     * `PENDING_UPLOAD` → `PROCESSING` → `READY`.
     */
    @POST("conversations/{id}/generate")
    suspend fun generateReplies(
        @Path("id") conversationId: String,
        @Body body: GenerateRequestDto
    ): GenerateResponseDto

    /** Fetch a single conversation by id (for refresh / deep link). */
    @GET("conversations/{id}")
    suspend fun getConversation(@Path("id") id: String): ConversationDto

    /**
     * Paginated history list. {@code cursor} is opaque base64 returned by
     * the previous page's {@code nextCursor}; pass null on the first call.
     * Last page returns {@code nextCursor=null}.
     */
    @GET("conversations")
    suspend fun listConversations(
        @Query("limit") limit: Int = 20,
        @Query("cursor") cursor: String? = null
    ): ConversationPageDto

    /** Soft-delete a conversation. Backend hard-deletes after 30 days. */
    @DELETE("conversations/{id}")
    suspend fun deleteConversation(@Path("id") id: String)

    /** 18-tone catalog with the free/pro split flagged on each entry. */
    @GET("tones")
    suspend fun getTones(): List<ToneDto>
}
