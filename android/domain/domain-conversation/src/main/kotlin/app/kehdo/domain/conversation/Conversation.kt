package app.kehdo.domain.conversation

/**
 * A single screenshot upload + the replies generated from it.
 * Mirrors the backend `conversations` row.
 */
data class Conversation(
    val id: String,
    val status: ConversationStatus,
    val failureReason: String?,
    val toneCode: String?,
    val replies: List<Reply>,
    val createdAt: Long,
    val updatedAt: Long
)
