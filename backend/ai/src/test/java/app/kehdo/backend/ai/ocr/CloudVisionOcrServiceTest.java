package app.kehdo.backend.ai.ocr;

import app.kehdo.backend.infra.storage.ScreenshotStorage;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Block;
import com.google.cloud.vision.v1.BoundingPoly;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.Page;
import com.google.cloud.vision.v1.Paragraph;
import com.google.cloud.vision.v1.Symbol;
import com.google.cloud.vision.v1.TextAnnotation;
import com.google.cloud.vision.v1.Vertex;
import com.google.cloud.vision.v1.Word;
import com.google.rpc.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-level coverage for {@link CloudVisionOcrService}. Mocks
 * {@link ImageAnnotatorClient} so the test never makes a network call.
 * Resilience4j retry/circuit-breaker decorators are off in tests (no
 * Spring context), which is fine — those concerns are integration-level
 * and tested separately.
 */
class CloudVisionOcrServiceTest {

    private ImageAnnotatorClient client;
    private ScreenshotStorage storage;
    private CloudVisionOcrService service;

    @BeforeEach
    void setUp() {
        client = mock(ImageAnnotatorClient.class);
        storage = mock(ScreenshotStorage.class);
        service = new CloudVisionOcrService(client, storage);
    }

    @Test
    void downloads_bytes_then_calls_vision_with_document_text_detection_feature() {
        when(storage.download("conv/abc/screenshot.png")).thenReturn(new byte[]{1, 2, 3});
        when(client.batchAnnotateImages(anyList())).thenReturn(responseWith(
                blockAt(50, 0, 200, "Priya Sharma"),                   // header
                blockAt(200, 40, 480, "Hey are you free tonight?"),    // left bubble (THEM)
                blockAt(280, 560, 1000, "Yeah 7pm")));                  // right bubble (ME)

        OcrResult result = service.read("conv/abc/screenshot.png");

        ArgumentCaptor<List<AnnotateImageRequest>> captor = ArgumentCaptor.forClass(List.class);
        verify(client).batchAnnotateImages(captor.capture());
        List<AnnotateImageRequest> sent = captor.getValue();

        assertThat(sent).hasSize(1);
        assertThat(sent.get(0).getFeaturesList()).hasSize(1);
        assertThat(sent.get(0).getFeatures(0).getType().name()).isEqualTo("DOCUMENT_TEXT_DETECTION");
        assertThat(sent.get(0).getImage().getContent().toByteArray()).containsExactly(1, 2, 3);

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
    void filters_phone_number_in_header() {
        when(storage.download(any())).thenReturn(new byte[]{1});
        when(client.batchAnnotateImages(anyList())).thenReturn(responseWith(
                blockAt(50, 0, 200, "+91 98765 43210"),       // header is a phone
                blockAt(400, 40, 480, "Message body")));

        OcrResult result = service.read("anything");

        assertThat(result.contactNameHint()).isNull();
    }

    @Test
    void empty_text_response_yields_empty_lines_list() {
        when(storage.download(any())).thenReturn(new byte[]{1});
        when(client.batchAnnotateImages(anyList())).thenReturn(BatchAnnotateImagesResponse.newBuilder()
                .addResponses(AnnotateImageResponse.newBuilder()
                        .setFullTextAnnotation(TextAnnotation.newBuilder().setText("").build())
                        .build())
                .build());

        OcrResult result = service.read("anything");

        assertThat(result.lines()).isEmpty();
        assertThat(result.contactNameHint()).isNull();
    }

    @Test
    void per_image_error_throws_so_circuit_breaker_counts_it() {
        when(storage.download(any())).thenReturn(new byte[]{1});
        when(client.batchAnnotateImages(anyList())).thenReturn(BatchAnnotateImagesResponse.newBuilder()
                .addResponses(AnnotateImageResponse.newBuilder()
                        .setError(Status.newBuilder().setMessage("Image dimensions are invalid"))
                        .build())
                .build());

        assertThatThrownBy(() -> service.read("anything"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Image dimensions are invalid");
    }

    // ---- helpers -------------------------------------------------------

    private static <T> T any() { return org.mockito.ArgumentMatchers.any(); }

    private static BatchAnnotateImagesResponse responseWith(Block... blocks) {
        Page.Builder page = Page.newBuilder().setConfidence(0.95f);
        for (Block b : blocks) page.addBlocks(b);
        AnnotateImageResponse response = AnnotateImageResponse.newBuilder()
                .setFullTextAnnotation(TextAnnotation.newBuilder()
                        .addPages(page)
                        .build())
                .build();
        return BatchAnnotateImagesResponse.newBuilder().addResponses(response).build();
    }

    /** @param top vertical position; @param leftX/rightX horizontal extent of the bubble. */
    private static Block blockAt(int top, int leftX, int rightX, String text) {
        Word.Builder word = Word.newBuilder();
        for (char c : text.toCharArray()) {
            word.addSymbols(Symbol.newBuilder().setText(String.valueOf(c)).build());
        }
        return Block.newBuilder()
                .setBoundingBox(BoundingPoly.newBuilder()
                        .addVertices(Vertex.newBuilder().setX(leftX).setY(top).build())
                        .addVertices(Vertex.newBuilder().setX(rightX).setY(top).build())
                        .addVertices(Vertex.newBuilder().setX(rightX).setY(top + 30).build())
                        .addVertices(Vertex.newBuilder().setX(leftX).setY(top + 30).build())
                        .build())
                .addParagraphs(Paragraph.newBuilder().addWords(word).build())
                .build();
    }
}
