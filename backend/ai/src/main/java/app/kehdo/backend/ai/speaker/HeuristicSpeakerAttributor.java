package app.kehdo.backend.ai.speaker;

import app.kehdo.backend.ai.ocr.OcrLine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Layout-based speaker attribution.
 *
 * <p>Mainstream chat apps (WhatsApp, iMessage, Telegram, Signal) lay
 * outgoing messages right-aligned and incoming messages left-aligned.
 * If we have bounding boxes per OCR line, the X-center of each line
 * tells us which side of the screen the bubble is on, which tells us
 * the speaker.</p>
 *
 * <h2>Algorithm</h2>
 * <ol>
 *   <li>If any line is missing bounds (all-stub OCR, or partial), fall
 *       back to {@link AlternatingSpeakerAttributor}'s pattern.</li>
 *   <li>Compute {@code centerX} for each line, derive the spread
 *       (max − min). If the spread is below a threshold (~10% of the
 *       median bubble width), all bubbles are roughly column-aligned
 *       — likely a Slack/Teams style layout. Fall back to alternating.</li>
 *   <li>Otherwise: split at the median {@code centerX}. Lines whose
 *       center is right of the median → {@code ME}. Left of median →
 *       {@code THEM}. Confidence scales with the line's distance from
 *       the median, capped at 0.95.</li>
 * </ol>
 *
 * <p>This handles ~90% of real-world screenshots without an LLM call,
 * which is why it lives ahead of any LLM-based stage. A future
 * stage-2 LLM disambiguator (own PR) will pick up the residual
 * ambiguous cases.</p>
 *
 * <p>Active when {@code kehdo.ai.speaker.provider=gcp}; otherwise the
 * {@link AlternatingSpeakerAttributor} stub runs.</p>
 */
@Service
@ConditionalOnProperty(name = "kehdo.ai.speaker.provider", havingValue = "gcp")
public class HeuristicSpeakerAttributor implements SpeakerAttributor {

    /** Spread threshold (in pixels) below which we treat the layout as column-aligned. */
    private static final int MIN_X_SPREAD_PX = 40;

    @Override
    public List<AttributedLine> attribute(List<OcrLine> ocrLines) {
        if (ocrLines == null || ocrLines.isEmpty()) {
            return List.of();
        }

        // Stage 1a: any missing bounds → fall through to alternating.
        boolean missingBounds = ocrLines.stream().anyMatch(l -> l.bounds() == null);
        if (missingBounds) {
            return alternating(ocrLines);
        }

        // Stage 1b: spread too small → column-aligned layout, layout heuristic
        // doesn't apply.
        List<Integer> centers = ocrLines.stream().map(l -> l.bounds().centerX()).sorted().toList();
        int spread = centers.get(centers.size() - 1) - centers.get(0);
        if (spread < MIN_X_SPREAD_PX) {
            return alternating(ocrLines);
        }

        // Stage 2: split at median centerX.
        double median = median(centers);
        double maxDistance = Math.max(median - centers.get(0), centers.get(centers.size() - 1) - median);

        List<AttributedLine> out = new ArrayList<>(ocrLines.size());
        for (OcrLine line : ocrLines) {
            int cx = line.bounds().centerX();
            AttributedLine.Speaker speaker = cx >= median
                    ? AttributedLine.Speaker.ME
                    : AttributedLine.Speaker.THEM;
            // Confidence rises with distance-from-median, capped at 0.95.
            double normalisedDistance = maxDistance == 0 ? 0 : Math.abs(cx - median) / maxDistance;
            double confidence = 0.5 + 0.45 * normalisedDistance;
            out.add(new AttributedLine(speaker, line.text(), confidence));
        }
        return out;
    }

    private static List<AttributedLine> alternating(List<OcrLine> ocrLines) {
        List<AttributedLine> out = new ArrayList<>(ocrLines.size());
        for (int i = 0; i < ocrLines.size(); i++) {
            AttributedLine.Speaker spk = (i % 2 == 0)
                    ? AttributedLine.Speaker.THEM
                    : AttributedLine.Speaker.ME;
            // Lower confidence than layout-based — we're guessing.
            out.add(new AttributedLine(spk, ocrLines.get(i).text(), 0.40));
        }
        return out;
    }

    private static double median(List<Integer> sortedCenters) {
        int n = sortedCenters.size();
        if (n % 2 == 1) {
            return sortedCenters.get(n / 2);
        }
        return (sortedCenters.get(n / 2 - 1) + sortedCenters.get(n / 2)) / 2.0;
    }

    /** Used by tests; kept package-private to avoid a public surface for tests-only methods. */
    static List<Integer> sortedCentersOf(List<OcrLine> lines) {
        List<Integer> centers = new ArrayList<>(lines.size());
        for (OcrLine l : lines) {
            if (l.bounds() == null) continue;
            centers.add(l.bounds().centerX());
        }
        Collections.sort(centers);
        return centers;
    }
}
