package app.kehdo.backend.ai.speaker;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Placeholder attributor. Tags lines as {@code THEM, ME, THEM, ME, ...} on
 * the assumption that the last bubble shown belongs to the other party
 * and conversations alternate. Wrong on monologues but unblocks the
 * generate flow.
 *
 * <p>Real 2-stage attribution lands in Phase 4 PR 9 — flip
 * {@code kehdo.ai.speaker.provider=gcp} when it does.</p>
 */
@Service
@ConditionalOnProperty(name = "kehdo.ai.speaker.provider", havingValue = "stub", matchIfMissing = true)
public class AlternatingSpeakerAttributor implements SpeakerAttributor {

    @Override
    public List<AttributedLine> attribute(List<String> ocrLines) {
        List<AttributedLine> out = new ArrayList<>(ocrLines.size());
        for (int i = 0; i < ocrLines.size(); i++) {
            AttributedLine.Speaker spk = (i % 2 == 0)
                    ? AttributedLine.Speaker.THEM
                    : AttributedLine.Speaker.ME;
            out.add(new AttributedLine(spk, ocrLines.get(i), 0.50));
        }
        return out;
    }
}
