package app.kehdo.backend.ai.speaker;

import app.kehdo.backend.ai.ocr.OcrLine;

import java.util.List;

/**
 * Tags OCR lines with {@code ME} or {@code THEM}.
 *
 * <p>Phase 4 PR 9 ships {@code HeuristicSpeakerAttributor} — a layout-
 * based approach that splits messages by their X-center on the screen
 * (left = THEM, right = ME on standard chat apps). Falls back to
 * alternating when bounding boxes aren't available or all bubbles are
 * roughly aligned (Slack-style avatar layouts).</p>
 *
 * <p>Output preserves input order. Confidence per line in [0.0, 1.0];
 * null is allowed when the implementation doesn't produce a numeric
 * score (stub).</p>
 */
public interface SpeakerAttributor {

    List<AttributedLine> attribute(List<OcrLine> ocrLines);

    record AttributedLine(Speaker speaker, String text, Double confidence) {

        public enum Speaker { ME, THEM }
    }
}
