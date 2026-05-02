package app.kehdo.backend.ai.ocr;

import java.util.List;

/**
 * Output of {@link OcrService#read}. Lines are still speaker-untagged at
 * this stage — the {@code SpeakerAttributor} handles ME/THEM in the next
 * pipeline step.
 *
 * @param lines           raw text lines, in screen order (top → bottom)
 * @param overallConfidence aggregate OCR confidence in [0.0, 1.0]; null when
 *                          the implementation doesn't produce one (stub /
 *                          legacy fixtures)
 * @param contactNameHint contact name extracted from the header region
 *                        only (never message body), or {@code null} if the
 *                        user has not opted into Contact Intelligence or
 *                        the header was unreadable
 */
public record OcrResult(
        List<String> lines,
        Double overallConfidence,
        String contactNameHint) {
}
