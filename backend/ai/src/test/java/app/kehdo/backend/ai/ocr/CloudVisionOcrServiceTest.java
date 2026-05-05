package app.kehdo.backend.ai.ocr;

import app.kehdo.backend.infra.storage.ScreenshotStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-level coverage for {@link CloudVisionOcrService}. Mocks the
 * {@link HttpClient} so the test never makes a network call.
 * Resilience4j retry/circuit-breaker decorators are off in tests (no
 * Spring context), which is fine — those concerns are integration-level
 * and tested separately.
 */
class CloudVisionOcrServiceTest {

    private HttpClient http;
    private ScreenshotStorage storage;
    private CloudVisionOcrService service;

    @BeforeEach
    void setUp() throws Exception {
        http = mock(HttpClient.class);
        storage = mock(ScreenshotStorage.class);
        service = new CloudVisionOcrService(storage, new ObjectMapper(), "test-api-key");
        // Inject the mocked HttpClient over the one the constructor builds.
        Field field = CloudVisionOcrService.class.getDeclaredField("http");
        field.setAccessible(true);
        field.set(service, http);
    }

    @Test
    void downloads_bytes_then_calls_vision_with_document_text_detection_feature() throws Exception {
        when(storage.download("conv/abc/screenshot.png")).thenReturn(new byte[]{1, 2, 3});

        // Three blocks: header (top), left bubble (THEM), right bubble (ME).
        String responseJson = """
                {
                  "responses": [{
                    "fullTextAnnotation": {
                      "pages": [{
                        "confidence": 0.95,
                        "blocks": [
                          %s,
                          %s,
                          %s
                        ]
                      }]
                    }
                  }]
                }""".formatted(
                blockJson(0, 50, 200, "Priya Sharma"),
                blockJson(40, 200, 480, "Hey are you free tonight?"),
                blockJson(560, 280, 1000, "Yeah 7pm"));

        HttpResponse<String> mockResponse = mockResponse(200, responseJson);
        when(http.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        OcrResult result = service.read("conv/abc/screenshot.png");

        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(http).send(captor.capture(), any(HttpResponse.BodyHandler.class));
        HttpRequest sent = captor.getValue();

        assertThat(sent.uri().toString())
                .startsWith("https://vision.googleapis.com/v1/images:annotate?key=test-api-key");
        assertThat(sent.method()).isEqualTo("POST");
        assertThat(sent.headers().firstValue("Content-Type")).contains("application/json");

        assertThat(result.lines()).extracting(OcrLine::text).containsExactly(
                "Priya Sharma",
                "Hey are you free tonight?",
                "Yeah 7pm");
        // Each line carries the bounding box from the corresponding Vision block.
        assertThat(result.lines()).allSatisfy(l -> assertThat(l.bounds()).isNotNull());
        // Specifically, the right-side bubble's center is to the right of the left-side bubble's.
        assertThat(result.lines().get(2).bounds().centerX())
                .isGreaterThan(result.lines().get(1).bounds().centerX());
        assertThat(result.contactNameHint()).isEqualTo("Priya Sharma");
    }

    @Test
    void filters_phone_number_in_header() throws Exception {
        when(storage.download(any())).thenReturn(new byte[]{1});
        String json = """
                {"responses":[{"fullTextAnnotation":{"pages":[{
                  "confidence":0.9,
                  "blocks":[%s, %s]
                }]}}]}""".formatted(
                blockJson(0, 50, 200, "+91 98765 43210"),
                blockJson(40, 400, 480, "Message body"));
        // Build the canned response before calling when() — mockResponse()
        // does its own stubbing internally, so calling it inside a parent
        // when().thenReturn() trips Mockito's UnfinishedStubbingException.
        HttpResponse<String> ok = mockResponse(200, json);
        when(http.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(ok);

        OcrResult result = service.read("anything");

        assertThat(result.contactNameHint()).isNull();
    }

    @Test
    void empty_text_response_yields_empty_lines_list() throws Exception {
        when(storage.download(any())).thenReturn(new byte[]{1});
        String json = """
                {"responses":[{"fullTextAnnotation":{"pages":[{"confidence":0.5,"blocks":[]}]}}]}""";
        HttpResponse<String> ok = mockResponse(200, json);
        when(http.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(ok);

        OcrResult result = service.read("anything");

        assertThat(result.lines()).isEmpty();
        assertThat(result.contactNameHint()).isNull();
    }

    @Test
    void per_image_error_throws_so_circuit_breaker_counts_it() throws Exception {
        when(storage.download(any())).thenReturn(new byte[]{1});
        String json = """
                {"responses":[{"error":{"code":3,"message":"Image dimensions are invalid"}}]}""";
        HttpResponse<String> ok = mockResponse(200, json);
        when(http.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(ok);

        assertThatThrownBy(() -> service.read("anything"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Image dimensions are invalid");
    }

    @Test
    void non_200_http_status_throws() throws Exception {
        when(storage.download(any())).thenReturn(new byte[]{1});
        HttpResponse<String> denied = mockResponse(403, "{\"error\":{\"message\":\"API key not authorized\"}}");
        when(http.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(denied);

        assertThatThrownBy(() -> service.read("anything"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("HTTP 403");
    }

    // ---- helpers -------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static HttpResponse<String> mockResponse(int statusCode, String body) {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);
        return response;
    }

    /**
     * Builds one Vision-API "block" JSON node. The bounding box is a
     * rectangle with vertices at (leftX, top), (rightX, top),
     * (rightX, top + 30), (leftX, top + 30). Each character becomes a
     * symbol inside one word inside one paragraph — same shape as the
     * old protobuf-based fixture.
     */
    private static String blockJson(int top, int leftX, int rightX, String text) {
        StringBuilder symbols = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            if (i > 0) symbols.append(',');
            char c = text.charAt(i);
            // Escape backslash and quote for JSON safety; spaces and
            // common punctuation pass through.
            String escaped = switch (c) {
                case '"' -> "\\\"";
                case '\\' -> "\\\\";
                default -> String.valueOf(c);
            };
            symbols.append("{\"text\":\"").append(escaped).append("\"}");
        }
        return """
                {
                  "boundingBox": {
                    "vertices": [
                      {"x": %d, "y": %d},
                      {"x": %d, "y": %d},
                      {"x": %d, "y": %d},
                      {"x": %d, "y": %d}
                    ]
                  },
                  "paragraphs": [{
                    "words": [{"symbols": [%s]}]
                  }]
                }""".formatted(
                leftX, top,
                rightX, top,
                rightX, top + 30,
                leftX, top + 30,
                symbols);
    }
}
