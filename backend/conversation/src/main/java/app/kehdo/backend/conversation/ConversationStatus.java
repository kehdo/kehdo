package app.kehdo.backend.conversation;

/**
 * Lifecycle states for a {@link Conversation}, mirroring
 * {@code contracts/openapi/kehdo.v1.yaml}'s {@code Conversation.status} enum.
 *
 * <p>Linear progression: {@code PENDING_UPLOAD → PROCESSING → READY}, with
 * {@link #FAILED} as a sink from any non-terminal state. A conversation
 * can re-enter {@code PROCESSING} on a tone change re-generation, but in
 * practice the OCR/speaker output is cached and only the LLM step
 * re-runs — so the status stays {@code READY} after the first success.</p>
 */
public enum ConversationStatus {
    PENDING_UPLOAD,
    PROCESSING,
    READY,
    FAILED
}
