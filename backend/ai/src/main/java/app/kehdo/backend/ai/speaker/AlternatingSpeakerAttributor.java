package app.kehdo.backend.ai.speaker;

import app.kehdo.backend.ai.ocr.OcrLine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Placeholder attributor. Tags lines as {@code THEM, ME, THEM, ME, ...} on
 * the assumption that the last bubble shown belongs to the other party
 * and conversations alternate. Wrong on monologues but unblocks the
 * generate flow when no real attributor is configured.
 *
 * <p>Active when {@code kehdo.ai.speaker.provider=stub} (the default).
 * Set {@code kehdo.ai.speaker.provider=gcp} to use
 * {@link HeuristicSpeakerAttributor}, which uses bounding-box layout for
 * X-coordinate attribution.</p>
 */
@Service
@ConditionalOnProperty(name = "kehdo.ai.speaker.provider", havingValue = "stub", matchIfMissing = true)
public class AlternatingSpeakerAttributor implements SpeakerAttributor {

    @Override
    public List<AttributedLine> attribute(List<OcrLine> ocrLines) {
        List<AttributedLine> out = new ArrayList<>(ocrLines.size());
        for (int i = 0; i < ocrLines.size(); i++) {
            AttributedLine.Speaker spk = (i % 2 == 0)
                    ? AttributedLine.Speaker.THEM
                    : AttributedLine.Speaker.ME;
            out.add(new AttributedLine(spk, ocrLines.get(i).text(), 0.50));
        }
        return out;
    }
}
