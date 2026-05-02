package app.kehdo.backend.ai.ocr;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Placeholder OCR. Returns a fixed two-line conversation so the generate
 * endpoint has something coherent to feed downstream stages.
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
                        "Hey, are we still on for tonight?",
                        "Yeah! 7pm at the usual spot."
                ),
                /* overallConfidence */ 0.95,
                /* contactNameHint  */ null);
    }
}
