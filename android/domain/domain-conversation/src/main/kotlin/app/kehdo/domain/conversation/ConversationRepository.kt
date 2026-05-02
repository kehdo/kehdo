package app.kehdo.domain.conversation

import app.kehdo.core.common.Outcome
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for the conversation flow.
 *
 * Implementations live in :data:data-conversation. Domain code (use-cases)
 * depends on this interface only. The Phase-1 implementation is a Fake
 * (in-memory canned data) so the Android flow works end-to-end before the
 * Phase-4 backend endpoints land — see ADR 0006.
 */
interface ConversationRepository {

    /** Fetch the available tones (mode-grouped). Backed by GET /v1/tones. */
    suspend fun getTones(): Outcome<List<Tone>>

    /**
     * Create a new conversation from a local screenshot URI. Returns the
     * conversation in PENDING_UPLOAD or PROCESSING state — caller polls
     * via [observe] until status is READY or FAILED.
     *
     * Backed by POST /v1/conversations + presigned S3 upload.
     */
    suspend fun createConversation(screenshotUri: String): Outcome<Conversation>

    /**
     * Trigger reply generation for an already-uploaded conversation with
     * the user's chosen tone. Returns the conversation with READY status
     * and 3 ranked replies attached.
     *
     * Backed by POST /v1/conversations/{id}/generate.
     */
    suspend fun generateReplies(conversationId: String, toneCode: String): Outcome<Conversation>

    /** Reactive observer — emits whenever this conversation changes locally. */
    fun observe(conversationId: String): Flow<Conversation?>

    /** Mark a reply as copied (signal for the voice fingerprint). */
    suspend fun markCopied(replyId: String): Outcome<Unit>

    /** Toggle a reply's favorited state. */
    suspend fun toggleFavorite(replyId: String): Outcome<Unit>

    /** Reactive list of recent conversations for the History screen. */
    fun observeRecent(limit: Int = 50): Flow<List<Conversation>>

    /** Hard-delete a single conversation (user-initiated forget). */
    suspend fun deleteConversation(conversationId: String): Outcome<Unit>
}
