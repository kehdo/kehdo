package app.kehdo.backend.ai.ocr;

import app.kehdo.backend.infra.storage.ScreenshotStorage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Real OCR adapter over Google Cloud Vision, talking to the REST endpoint
 * {@code https://vision.googleapis.com/v1/images:annotate} with an API key
 * passed in {@code ?key=}. Activated by {@code kehdo.ai.ocr.provider=gcp};
 * otherwise {@link StubOcrService} runs.
 *
 * <p>We deliberately use REST + API key rather than the gRPC SDK with
 * Application Default Credentials. The gRPC SDK requires either a
 * service-account JSON or workload identity, both of which fight
 * Google's "Secure by Default" org policy that blocks
 * {@code iam.disableServiceAccountKeyCreation} on free-tier organisations.
 * A simple {@code AIzaSy...} API key is restrict-able to the Cloud Vision
 * API alone and avoids that policy entirely.</p>
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
    private static final String ENDPOINT = "https://vision.googleapis.com/v1/images:annotate";

    /** Max characters we'll accept as a contact name — stops fully-OCR'd timestamps from leaking through. */
    private static final int MAX_NAME_LENGTH = 80;

    private final HttpClient http;
    private final ObjectMapper mapper;
    private final ScreenshotStorage storage;
    private final String apiKey;

    public CloudVisionOcrService(
            ScreenshotStorage storage,
            ObjectMapper mapper,
            @Value("${kehdo.ai.google-vision.api-key:}") String apiKey) {
        this.storage = storage;
        this.mapper = mapper;
        this.apiKey = apiKey;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("kehdo.ai.google-vision.api-key is empty — OCR calls will fail with HTTP 400 from Cloud Vision");
        }
    }

    @Override
    @CircuitBreaker(name = CB_NAME)
    @Retry(name = CB_NAME)
    public OcrResult read(String screenshotObjectKey) {
        byte[] bytes = storage.download(screenshotObjectKey);

        String base64 = Base64.getEncoder().encodeToString(bytes);
        String requestBody = """
                {
                  "requests": [{
                    "image": {"content": "%s"},
                    "features": [{"type": "DOCUMENT_TEXT_DETECTION"}]
                  }]
                }""".formatted(base64);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT + "?key=" + apiKey))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (java.io.IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new IllegalStateException("Cloud Vision REST call failed", e);
        }

        if (response.statusCode() != 200) {
            log.warn("Cloud Vision returned HTTP {} for object {}: {}",
                    response.statusCode(), screenshotObjectKey, response.body());
            throw new IllegalStateException(
                    "Cloud Vision OCR failed with HTTP " + response.statusCode());
        }

        JsonNode root;
        try {
            root = mapper.readTree(response.body());
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException("Could not parse Cloud Vision response", e);
        }

        JsonNode first = root.path("responses").path(0);
        if (first.has("error")) {
            // Vision returns errors per-image inside a 200 envelope; lift
            // them to a runtime failure so the circuit breaker counts it.
            String message = first.path("error").path("message").asText();
            log.warn("Cloud Vision returned an error for object {}: {}", screenshotObjectKey, message);
            throw new IllegalStateException("Cloud Vision OCR failed: " + message);
        }

        JsonNode pages = first.path("fullTextAnnotation").path("pages");
        List<OcrLine> lines = extractLines(pages);
        Double confidence = extractOverallConfidence(pages);
        Optional<String> contactName = extractContactNameFromHeader(pages);

        return new OcrResult(lines, confidence, contactName.orElse(null));
    }

    /**
     * One {@link OcrLine} per Vision block — chat-message bubbles produce
     * one block each in DOCUMENT_TEXT_DETECTION mode, which is the natural
     * unit for the speaker-attribution X-coordinate heuristic.
     */
    private static List<OcrLine> extractLines(JsonNode pages) {
        List<OcrLine> out = new ArrayList<>();
        for (JsonNode page : pages) {
            for (JsonNode block : page.path("blocks")) {
                String text = concatBlockText(block).trim();
                if (text.isEmpty()) continue;
                out.add(new OcrLine(text, boundsOf(block.path("boundingBox"))));
            }
        }
        return out;
    }

    private static String concatBlockText(JsonNode block) {
        StringBuilder sb = new StringBuilder();
        for (JsonNode para : block.path("paragraphs")) {
            for (JsonNode word : para.path("words")) {
                for (JsonNode sym : word.path("symbols")) {
                    sb.append(sym.path("text").asText());
                }
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    private static OcrLine.BoundingBox boundsOf(JsonNode boundingBox) {
        JsonNode vertices = boundingBox.path("vertices");
        if (!vertices.isArray() || vertices.isEmpty()) return null;
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        for (JsonNode v : vertices) {
            int x = v.path("x").asInt(0);
            int y = v.path("y").asInt(0);
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }
        return new OcrLine.BoundingBox(minX, minY, maxX, maxY);
    }

    private static Double extractOverallConfidence(JsonNode pages) {
        if (!pages.isArray() || pages.isEmpty()) return null;
        // Vision exposes confidence per page; average across pages for
        // a single overall figure. Most chat screenshots produce one page.
        double sum = 0.0;
        int count = 0;
        for (JsonNode page : pages) {
            if (page.has("confidence")) {
                sum += page.path("confidence").asDouble(0.0);
                count++;
            }
        }
        return count == 0 ? 0.0 : sum / count;
    }

    /**
     * Header-only contact-name extraction per backend/CLAUDE.md AI rule 8.
     * "Header region" = the topmost block by minimum Y of its bounding
     * polygon. Phone numbers (rule 9) are dropped.
     */
    private static Optional<String> extractContactNameFromHeader(JsonNode pages) {
        if (!pages.isArray() || pages.isEmpty()) return Optional.empty();
        JsonNode firstPage = pages.get(0);
        JsonNode blocks = firstPage.path("blocks");
        if (!blocks.isArray() || blocks.isEmpty()) return Optional.empty();

        JsonNode topmost = null;
        int topY = Integer.MAX_VALUE;
        for (JsonNode block : blocks) {
            int y = minY(block.path("boundingBox").path("vertices"));
            if (y < topY) {
                topY = y;
                topmost = block;
            }
        }
        if (topmost == null) return Optional.empty();

        String text = concatBlockText(topmost).trim();
        if (text.isEmpty() || text.length() > MAX_NAME_LENGTH) return Optional.empty();
        if (PhoneNumberDetector.looksLikePhoneNumber(text)) return Optional.empty();
        return Optional.of(text);
    }

    private static int minY(JsonNode vertices) {
        int min = Integer.MAX_VALUE;
        if (!vertices.isArray()) return min;
        for (JsonNode v : vertices) {
            int y = v.path("y").asInt(Integer.MAX_VALUE);
            if (y < min) min = y;
        }
        return min;
    }
}
