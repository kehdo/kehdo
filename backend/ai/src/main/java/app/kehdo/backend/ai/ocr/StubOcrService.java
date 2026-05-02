package app.kehdo.backend.ai.ocr;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Phase 4 PR 3 placeholder. Returns a fixed two-line conversation so the
 * generate endpoint has something coherent to feed downstream stages
 * before {@code CloudVisionOcrService} (PR 6) lands.
 */
@Service
@Profile({"stub-llm", "test", "default"})
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
