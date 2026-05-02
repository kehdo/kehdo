package app.kehdo.backend.ai.speaker;

import app.kehdo.backend.ai.ocr.OcrLine;
import app.kehdo.backend.ai.speaker.SpeakerAttributor.AttributedLine;
import app.kehdo.backend.ai.speaker.SpeakerAttributor.AttributedLine.Speaker;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the layout-based attribution behaves as documented:
 * left-of-median → THEM, right-of-median → ME on standard chat layouts;
 * falls through to alternating when bounds missing or layout is
 * column-aligned.
 */
class HeuristicSpeakerAttributorTest {

    private final HeuristicSpeakerAttributor attributor = new HeuristicSpeakerAttributor();

    @Test
    void splits_left_them_right_me_on_standard_chat_layout() {
        // WhatsApp-style: incoming bubbles left (centerX≈260), outgoing right (centerX≈780).
        List<OcrLine> lines = List.of(
                line("Hey, are we on?",     40, 480),  // centerX = 260 → THEM
                line("Yeah, 7pm",          560, 1000),  // centerX = 780 → ME
                line("Cool see you then",   40, 480),  // centerX = 260 → THEM
                line("Bring the wine",     560, 1000)); // centerX = 780 → ME

        List<AttributedLine> out = attributor.attribute(lines);

        assertThat(out).extracting(AttributedLine::speaker)
                .containsExactly(Speaker.THEM, Speaker.ME, Speaker.THEM, Speaker.ME);
    }

    @Test
    void confidence_increases_with_distance_from_median() {
        // Three bubbles: well-left, near-median, well-right.
        List<OcrLine> lines = List.of(
                line("far left",   0, 100),    // centerX = 50
                line("near center", 480, 540), // centerX = 510
                line("far right",  900, 1000)); // centerX = 950

        List<AttributedLine> out = attributor.attribute(lines);

        // Edges should have higher confidence than the near-median bubble.
        assertThat(out.get(0).confidence()).isGreaterThan(out.get(1).confidence());
        assertThat(out.get(2).confidence()).isGreaterThan(out.get(1).confidence());
        // Confidence cap is 0.95.
        assertThat(out.get(0).confidence()).isLessThanOrEqualTo(0.95);
    }

    @Test
    void falls_back_to_alternating_when_any_bounds_missing() {
        List<OcrLine> lines = List.of(
                OcrLine.textOnly("a"),                     // no bounds
                line("b", 560, 1000));                     // bounds present

        List<AttributedLine> out = attributor.attribute(lines);

        // Alternating: THEM, ME, ...
        assertThat(out).extracting(AttributedLine::speaker)
                .containsExactly(Speaker.THEM, Speaker.ME);
        // Lower confidence than layout-based.
        assertThat(out.get(0).confidence()).isEqualTo(0.40);
    }

    @Test
    void falls_back_to_alternating_when_all_bubbles_column_aligned() {
        // Slack-style: all bubbles left-aligned with avatars; centers cluster.
        List<OcrLine> lines = List.of(
                line("first message",  60, 470),  // centerX = 265
                line("second message", 60, 480),  // centerX = 270
                line("third message",  60, 460)); // centerX = 260

        List<AttributedLine> out = attributor.attribute(lines);

        // Spread is 10px (well below 40px threshold) → alternating fallback.
        assertThat(out).extracting(AttributedLine::speaker)
                .containsExactly(Speaker.THEM, Speaker.ME, Speaker.THEM);
        assertThat(out.get(0).confidence()).isEqualTo(0.40);
    }

    @Test
    void empty_input_returns_empty_list() {
        assertThat(attributor.attribute(List.of())).isEmpty();
    }

    private static OcrLine line(String text, int leftX, int rightX) {
        return new OcrLine(text, new OcrLine.BoundingBox(leftX, 0, rightX, 30));
    }
}
