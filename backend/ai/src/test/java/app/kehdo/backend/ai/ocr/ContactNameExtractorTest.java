package app.kehdo.backend.ai.ocr;

import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.Block;
import com.google.cloud.vision.v1.BoundingPoly;
import com.google.cloud.vision.v1.Page;
import com.google.cloud.vision.v1.Paragraph;
import com.google.cloud.vision.v1.Symbol;
import com.google.cloud.vision.v1.TextAnnotation;
import com.google.cloud.vision.v1.Vertex;
import com.google.cloud.vision.v1.Word;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the extractor reads ONLY the topmost block (header region) and
 * applies the phone-number filter. Constructs Vision protobuf responses
 * by hand to avoid live API calls.
 */
class ContactNameExtractorTest {

    @Test
    void picks_topmost_block_as_header() {
        AnnotateImageResponse response = response(
                block(/* top=*/ 800, "this is the message body content"),
                block(/* top=*/ 50,  "Priya Sharma"),                   // ← header
                block(/* top=*/ 400, "earlier in the chat"));

        assertThat(ContactNameExtractor.extractFromHeader(response)).contains("Priya Sharma");
    }

    @Test
    void drops_phone_number_in_header() {
        AnnotateImageResponse response = response(
                block(50, "+91 98765 43210"),
                block(400, "Hi how are you"));

        assertThat(ContactNameExtractor.extractFromHeader(response)).isEmpty();
    }

    @Test
    void drops_overly_long_header_text() {
        String longText = "a".repeat(120);
        AnnotateImageResponse response = response(block(50, longText));

        assertThat(ContactNameExtractor.extractFromHeader(response)).isEmpty();
    }

    @Test
    void empty_response_returns_empty() {
        AnnotateImageResponse empty = AnnotateImageResponse.newBuilder().build();
        assertThat(ContactNameExtractor.extractFromHeader(empty)).isEmpty();
    }

    // ---- helpers -------------------------------------------------------

    private static AnnotateImageResponse response(Block... blocks) {
        Page.Builder page = Page.newBuilder();
        for (Block b : blocks) page.addBlocks(b);
        return AnnotateImageResponse.newBuilder()
                .setFullTextAnnotation(TextAnnotation.newBuilder().addPages(page).build())
                .build();
    }

    private static Block block(int top, String text) {
        Word.Builder word = Word.newBuilder();
        for (char c : text.toCharArray()) {
            word.addSymbols(Symbol.newBuilder().setText(String.valueOf(c)).build());
        }
        Paragraph para = Paragraph.newBuilder().addWords(word).build();
        BoundingPoly bbox = BoundingPoly.newBuilder()
                .addVertices(Vertex.newBuilder().setX(0).setY(top).build())
                .addVertices(Vertex.newBuilder().setX(100).setY(top).build())
                .addVertices(Vertex.newBuilder().setX(100).setY(top + 30).build())
                .addVertices(Vertex.newBuilder().setX(0).setY(top + 30).build())
                .build();
        return Block.newBuilder().setBoundingBox(bbox).addParagraphs(para).build();
    }
}
