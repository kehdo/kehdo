package app.kehdo.feature.reply

object ReplyRoute {
    const val GRAPH = "reply_graph"
    const val REPLY = "reply"
    const val ARG_CONVERSATION_ID = "conversationId"
    const val REPLY_WITH_ARG = "$REPLY/{$ARG_CONVERSATION_ID}"
    fun reply(conversationId: String) = "$REPLY/$conversationId"
}
