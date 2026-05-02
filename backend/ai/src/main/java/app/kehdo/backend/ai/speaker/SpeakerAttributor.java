package app.kehdo.backend.ai.speaker;

import java.util.List;

/**
 * Tags raw OCR lines with {@code ME} or {@code THEM}. Phase 4 PR 9 will
 * implement the documented 2-stage attribution; PR 3 ships a naive
 * alternator so downstream stages can be wired before the real model is
 * trained.
 *
 * <p>Output preserves input order. Confidence per line in
 * [0.0, 1.0]; null is allowed when the implementation doesn't produce a
 * numeric score (stub).</p>
 */
public interface SpeakerAttributor {

    List<AttributedLine> attribute(List<String> ocrLines);

    record AttributedLine(Speaker speaker, String text, Double confidence) {

        public enum Speaker { ME, THEM }
    }
}
