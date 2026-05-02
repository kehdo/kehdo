package app.kehdo.backend.ai.ocr;

/**
 * Reads text from a screenshot in object storage and returns the raw
 * (speaker-untagged) message lines.
 *
 * <p>Per {@code backend/CLAUDE.md} AI rule 8: when Contact Intelligence
 * (Level 2 consent) is active, the prompt fed to the OCR model MUST
 * extract the contact's name from the chat-header region only —
 * never from message-body content. That guarantee is enforced inside
 * the implementation, not at this interface.</p>
 *
 * <p>Implementations live in {@code :ai/ocr/}:
 * <ul>
 *   <li>{@code StubOcrService} — Phase 4 PR 3 — canned messages, no network</li>
 *   <li>{@code CloudVisionOcrService} — Phase 4 PR 6 — Google Cloud Vision</li>
 * </ul>
 */
public interface OcrService {

    /**
     * @param screenshotObjectKey the {@code conversations.screenshot_object_key}
     *                            (an object key in S3 / R2; not a presigned URL)
     */
    OcrResult read(String screenshotObjectKey);
}
