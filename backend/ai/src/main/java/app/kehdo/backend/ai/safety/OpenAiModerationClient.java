package app.kehdo.backend.ai.safety;

import app.kehdo.backend.ai.llm.OpenAiClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Real moderation backed by OpenAI's {@code /v1/moderations} endpoint.
 * Activated by {@code kehdo.ai.moderation.provider=openai} per
 * {@code backend/CLAUDE.md} AI rule 5. Wrapped by Resilience4j's
 * {@code moderation} circuit breaker + retry; on any failure (transport
 * error, breaker open, malformed envelope) the {@link #checkFallback}
 * fail-open returns {@link ModerationVerdict#allow()} so a moderation
 * outage doesn't break reply generation. The breaker still sees the
 * underlying failure, so repeated outages will eventually open the
 * breaker and short-circuit the fallback path.
 *
 * <p>OpenAI returns ~11 categories (hate, hate/threatening, harassment,
 * harassment/threatening, self-harm[*], sexual, sexual/minors,
 * violence, violence/graphic). We collapse those into the five buckets
 * documented on {@link ModerationVerdict}: {@code SEXUAL_MINORS},
 * {@code HATE}, {@code VIOLENCE}, {@code SELF_HARM}, {@code OTHER}.
 * When multiple categories are flagged we pick the one with the highest
 * provider score.</p>
 */
@Service
@ConditionalOnProperty(name = "kehdo.ai.moderation.provider", havingValue = "openai")
public class OpenAiModerationClient implements ModerationClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiModerationClient.class);
    private static final String CB_NAME = "moderation";

    private final OpenAiClient client;
    private final String modelName;

    public OpenAiModerationClient(
            OpenAiClient client,
            @Value("${kehdo.ai.openai.moderation-model:omni-moderation-latest}") String modelName) {
        this.client = client;
        this.modelName = modelName;
    }

    @Override
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "checkFallback")
    @Retry(name = CB_NAME)
    public ModerationVerdict check(String candidateText) {
        OpenAiClient.ModerationResult result = client.moderate(
                new OpenAiClient.ModerationRequest(modelName, candidateText));
        if (!result.flagged()) {
            return ModerationVerdict.allow();
        }
        return verdictFromHighestFlaggedCategory(result);
    }

    /**
     * Resilience4j fallback. Invoked when the wrapped call throws OR when
     * the breaker is already open. Fail open with a WARN log — the
     * circuit breaker has already counted the failure, and we'd rather
     * deliver a possibly-unmoderated reply than a 500. Repeated failures
     * trip the breaker, and at that point the operator should be paged.
     *
     * <p>Signature must match {@link #check} plus a trailing
     * {@link Throwable} per Resilience4j's fallback contract.</p>
     */
    @SuppressWarnings("unused") // referenced by name from @CircuitBreaker
    private ModerationVerdict checkFallback(String candidateText, Throwable cause) {
        log.warn("OpenAI moderation unavailable; failing open: {}", cause.toString());
        return ModerationVerdict.allow();
    }

    private static ModerationVerdict verdictFromHighestFlaggedCategory(OpenAiClient.ModerationResult result) {
        Map<String, Boolean> categories = result.categories();
        Map<String, Double> scores = result.categoryScores();
        if (categories == null || categories.isEmpty()) {
            // flagged=true but no category map — defensive default
            return ModerationVerdict.block("OTHER", null);
        }

        String topCategory = null;
        double topScore = -1.0;
        for (Map.Entry<String, Boolean> entry : categories.entrySet()) {
            if (!Boolean.TRUE.equals(entry.getValue())) {
                continue;
            }
            double score = scores != null && scores.get(entry.getKey()) != null
                    ? scores.get(entry.getKey())
                    : 0.0;
            if (score > topScore) {
                topScore = score;
                topCategory = entry.getKey();
            }
        }

        if (topCategory == null) {
            // flagged=true but every entry is false — provider self-contradiction
            return ModerationVerdict.block("OTHER", null);
        }
        return ModerationVerdict.block(canonicalize(topCategory), topScore);
    }

    /**
     * Map OpenAI's slash-separated category strings to our coarse enum
     * (kept as String per {@link ModerationVerdict}). Unknown / future
     * categories collapse to {@code OTHER} so a new OpenAI rollout
     * doesn't crash the pipeline.
     */
    private static String canonicalize(String openAiCategory) {
        if (openAiCategory == null) return "OTHER";
        String c = openAiCategory.toLowerCase();
        if (c.equals("sexual/minors")) return "SEXUAL_MINORS";
        if (c.startsWith("self-harm")) return "SELF_HARM";
        if (c.startsWith("violence")) return "VIOLENCE";
        if (c.startsWith("hate") || c.startsWith("harassment")) return "HATE";
        if (c.startsWith("sexual")) return "OTHER"; // adult sexual content — block bucket but not the SEXUAL_MINORS slot
        return "OTHER";
    }
}
