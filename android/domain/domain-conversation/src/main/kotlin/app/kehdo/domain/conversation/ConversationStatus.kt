package app.kehdo.domain.conversation

/**
 * Lifecycle state of a conversation. Mirrors the backend `conversations.status`
 * column — see V1__init_schema.sql.
 */
enum class ConversationStatus {
    PENDING_UPLOAD,
    PROCESSING,
    READY,
    FAILED
}
