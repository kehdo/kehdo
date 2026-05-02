package app.kehdo.backend.conversation;

/**
 * One message the OCR + speaker-attribution pipeline extracted from a
 * screenshot. Stored as part of {@code conversations.parsed_messages}
 * (JSONB column) — never as its own table.
 *
 * <p>Mirrors {@code ParsedMessage} in
 * {@code contracts/openapi/kehdo.v1.yaml}.</p>
 *
 * @param speaker    who said it — derived by 2-stage attribution in :ai
 * @param text       the message body, post-OCR cleanup
 * @param confidence overall confidence in [0.0, 1.0] for both OCR + speaker
 *                   attribution; null when the pipeline didn't produce a
 *                   numeric score (e.g. legacy fixtures)
 */
public record ParsedMessage(
        Speaker speaker,
        String text,
        Double confidence) {

    public enum Speaker {
        ME,
        THEM
    }
}
