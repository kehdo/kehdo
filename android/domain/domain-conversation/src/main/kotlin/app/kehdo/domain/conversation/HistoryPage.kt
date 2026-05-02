package app.kehdo.domain.conversation

/**
 * One page of conversation history. [nextCursor] is opaque to the client
 * (base64 of a server-side composite key). Null means there are no more
 * pages — UI hides the "load more" affordance.
 */
data class HistoryPage(
    val items: List<Conversation>,
    val nextCursor: String?
)
