package app.kehdo.backend.ai.ocr;

/**
 * One text line from the OCR pass, optionally carrying its bounding-box
 * geometry on the source image.
 *
 * <p>Bounding boxes are populated by {@code CloudVisionOcrService}
 * (Phase 4 PR 6) and consumed by the layout-aware
 * {@code HeuristicSpeakerAttributor} (Phase 4 PR 9). The stub adapter
 * leaves {@link #bounds()} null; downstream code falls through to
 * non-layout strategies in that case.</p>
 *
 * @param text   detected text, post-trim
 * @param bounds optional bounding box on the source image; null when the
 *               OCR adapter doesn't produce layout info
 */
public record OcrLine(String text, BoundingBox bounds) {

    /** Helper for adapters (or tests) that don't have layout info. */
    public static OcrLine textOnly(String text) {
        return new OcrLine(text, null);
    }

    /**
     * Axis-aligned bounding box in source-image pixel coordinates.
     * Origin is top-left; X increases rightward, Y increases downward.
     */
    public record BoundingBox(int leftX, int topY, int rightX, int bottomY) {

        public int centerX() {
            return (leftX + rightX) / 2;
        }

        public int width() {
            return rightX - leftX;
        }
    }
}
