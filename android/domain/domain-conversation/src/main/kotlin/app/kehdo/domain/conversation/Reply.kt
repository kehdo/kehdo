package app.kehdo.domain.conversation

/**
 * One AI-generated reply suggestion. Three Replies are returned per
 * generation, ranked 1..3.
 */
data class Reply(
    val id: String,
    val conversationId: String,
    val rank: Int,
    val text: String,
    val toneCode: String,
    val isFavorited: Boolean,
    val isCopied: Boolean,
    val createdAt: Long
)
