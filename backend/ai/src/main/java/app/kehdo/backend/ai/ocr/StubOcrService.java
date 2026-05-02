package app.kehdo.backend.ai.ocr;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Placeholder OCR. Returns a fixed two-line conversation with synthetic
 * bounding boxes — left-aligned for THEM, right-aligned for ME — so the
 * downstream {@code HeuristicSpeakerAttributor} (Phase 4 PR 9) can do
 * something meaningful even in stub mode.
 *
 * <p>Active when {@code kehdo.ai.ocr.provider=stub} (the default).
 * Set {@code kehdo.ai.ocr.provider=gcp} to use {@link CloudVisionOcrService}.</p>
 */
@Service
@ConditionalOnProperty(name = "kehdo.ai.ocr.provider", havingValue = "stub", matchIfMissing = true)
public class StubOcrService implements OcrService {

    @Override
    public OcrResult read(String screenshotObjectKey) {
        return new OcrResult(
                List.of(
                        // Left-side bubble (incoming, "THEM" on most chat apps).
                        new OcrLine(
                                "Hey, are we still on for tonight?",
                                new OcrLine.BoundingBox(40, 200, 480, 260)),
                        // Right-side bubble (outgoing, "ME").
                        new OcrLine(
                                "Yeah! 7pm at the usual spot.",
                                new OcrLine.BoundingBox(560, 280, 1000, 340))),
                /* overallConfidence */ 0.95,
                /* contactNameHint  */ null);
    }
}
