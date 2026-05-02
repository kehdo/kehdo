package app.kehdo.backend.ai.ocr;

import app.kehdo.backend.infra.storage.ScreenshotStorage;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Real OCR adapter over Google Cloud Vision. Activated by
 * {@code kehdo.ai.ocr.provider=gcp}; otherwise {@link StubOcrService}
 * runs.
 *
 * <p>Uses {@code DOCUMENT_TEXT_DETECTION} rather than basic
 * {@code TEXT_DETECTION} — chat screenshots are dense, multi-line text
 * with rich layout, and DOCUMENT mode preserves reading order much
 * better.</p>
 *
 * <p>Wrapped by Resilience4j's {@code ocr-vision} circuit breaker per
 * {@code backend/CLAUDE.md} AI rule 4. Configured in
 * {@code application.yml} → {@code resilience4j.circuitbreaker.instances.ocr-vision}.</p>
 */
@Service
@ConditionalOnProperty(name = "kehdo.ai.ocr.provider", havingValue = "gcp")
public class CloudVisionOcrService implements OcrService {

    private static final Logger log = LoggerFactory.getLogger(CloudVisionOcrService.class);
    private static final String CB_NAME = "ocr-vision";

    private final ImageAnnotatorClient client;
    private final ScreenshotStorage storage;

    public CloudVisionOcrService(ImageAnnotatorClient client, ScreenshotStorage storage) {
        this.client = client;
        this.storage = storage;
    }

    @Override
    @CircuitBreaker(name = CB_NAME)
    @Retry(name = CB_NAME)
    public OcrResult read(String screenshotObjectKey) {
        byte[] bytes = storage.download(screenshotObjectKey);

        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .setImage(Image.newBuilder().setContent(ByteString.copyFrom(bytes)).build())
                .addFeatures(Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build())
                .build();

        BatchAnnotateImagesResponse batch = client.batchAnnotateImages(List.of(request));
        AnnotateImageResponse response = batch.getResponses(0);

        if (response.hasError()) {
            // Vision returns errors per-image inside a 200 envelope; lift
            // them to a runtime failure so the circuit breaker counts it.
            String message = response.getError().getMessage();
            log.warn("Cloud Vision returned an error for object {}: {}", screenshotObjectKey, message);
            throw new IllegalStateException("Cloud Vision OCR failed: " + message);
        }

        List<String> lines = extractLines(response);
        Double confidence = extractOverallConfidence(response);
        Optional<String> contactName = ContactNameExtractor.extractFromHeader(response);

        return new OcrResult(lines, confidence, contactName.orElse(null));
    }

    private static List<String> extractLines(AnnotateImageResponse response) {
        String text = response.getFullTextAnnotation().getText();
        if (text.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(text.split("\\R"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private static Double extractOverallConfidence(AnnotateImageResponse response) {
        if (response.getFullTextAnnotation().getPagesCount() == 0) return null;
        // Vision exposes confidence per page; average across pages for
        // a single overall figure. Most chat screenshots produce one page.
        return response.getFullTextAnnotation().getPagesList().stream()
                .mapToDouble(p -> p.getConfidence())
                .average()
                .orElse(0.0);
    }
}
