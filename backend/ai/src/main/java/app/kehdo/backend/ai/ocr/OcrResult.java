package app.kehdo.backend.ai.ocr;

import java.util.List;

/**
 * Output of {@link OcrService#read}. Lines are still speaker-untagged at
 * this stage — the {@code SpeakerAttributor} handles ME/THEM in the next
 * pipeline step.
 *
 * @param lines           detected lines (text + optional bounding box) in
 *                        screen order (top → bottom). Phase 4 PR 6 gave
 *                        Cloud Vision an opportunity to populate the bounds;
 *                        the stub adapter leaves them null. Layout-aware
 *                        speaker attribution (Phase 4 PR 9) reads
 *                        {@code bounds.centerX()} when present.
 * @param overallConfidence aggregate OCR confidence in [0.0, 1.0]; null when
 *                          the implementation doesn't produce one (stub /
 *                          legacy fixtures)
 * @param contactNameHint contact name extracted from the header region
 *                        only (never message body), or {@code null} if the
 *                        user has not opted into Contact Intelligence or
 *                        the header was unreadable
 */
public record OcrResult(
        List<OcrLine> lines,
        Double overallConfidence,
        String contactNameHint) {
}
