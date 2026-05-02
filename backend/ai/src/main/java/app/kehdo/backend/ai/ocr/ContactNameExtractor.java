package app.kehdo.backend.ai.ocr;

import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.Block;
import com.google.cloud.vision.v1.Page;
import com.google.cloud.vision.v1.Vertex;

import java.util.Comparator;
import java.util.Optional;

/**
 * Extracts a candidate contact name from a Cloud Vision response, looking
 * <em>only</em> at the chat-header region of the screenshot per
 * {@code backend/CLAUDE.md} AI rule 8.
 *
 * <p>"Header region" = the topmost block by Y-coordinate. Chat apps put
 * the contact name immediately below the back button at the very top
 * of the screen, so the highest-on-screen text block is almost always
 * the right pick.</p>
 *
 * <p>If the highest block looks like a phone number, we drop it (rule 9).
 * The orchestrator decides whether to actually attach the result to a
 * {@code contact_profiles} row based on Level 2 consent.</p>
 */
public final class ContactNameExtractor {

    /** Max character length we'll accept as a contact name. Keeps junk like fully-OCR'd timestamps from leaking through. */
    private static final int MAX_NAME_LENGTH = 80;

    private ContactNameExtractor() {}

    public static Optional<String> extractFromHeader(AnnotateImageResponse response) {
        if (response == null || response.getFullTextAnnotation().getPagesCount() == 0) {
            return Optional.empty();
        }
        Page page = response.getFullTextAnnotation().getPages(0);
        return page.getBlocksList().stream()
                .min(Comparator.comparingInt(ContactNameExtractor::topY))
                .map(ContactNameExtractor::concatBlockText)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .filter(s -> s.length() <= MAX_NAME_LENGTH)
                .filter(s -> !PhoneNumberDetector.looksLikePhoneNumber(s));
    }

    private static int topY(Block block) {
        return block.getBoundingBox().getVerticesList().stream()
                .mapToInt(Vertex::getY)
                .min()
                .orElse(Integer.MAX_VALUE);
    }

    private static String concatBlockText(Block block) {
        StringBuilder sb = new StringBuilder();
        block.getParagraphsList().forEach(para ->
                para.getWordsList().forEach(word -> {
                    word.getSymbolsList().forEach(sym -> sb.append(sym.getText()));
                    sb.append(' ');
                }));
        return sb.toString();
    }
}
