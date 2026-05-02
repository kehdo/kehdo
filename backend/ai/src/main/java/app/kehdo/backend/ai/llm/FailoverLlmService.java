package app.kehdo.backend.ai.llm;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Composes Vertex AI Gemini (primary) with OpenAI gpt-4o-mini (fallback)
 * per {@code backend/CLAUDE.md} AI rule 4. Active when
 * {@code kehdo.ai.llm.provider=failover} — the recommended production
 * setting.
 *
 * <p>Failover triggers:
 * <ul>
 *   <li>{@link CallNotPermittedException} — Vertex's circuit breaker is
 *       open. Don't even attempt; jump straight to OpenAI.</li>
 *   <li>{@link VertexAiResponseException} — Vertex returned but the
 *       response was unusable (empty / malformed JSON). One bad answer
 *       is enough to try the other model.</li>
 *   <li>Other {@link RuntimeException} from Vertex — same story; we'd
 *       rather show the user a reply than a 500.</li>
 * </ul>
 *
 * <p>If the fallback also fails, that exception propagates up. The
 * orchestrator's {@code ContentBlockedException} mapping doesn't apply —
 * a model failure is a 502 LLM_UPSTREAM_ERROR, mapped at the controller.</p>
 *
 * <p>{@link Primary} ensures Spring picks this bean for any
 * {@code @Autowired LlmService} when failover mode is active, even
 * though Vertex and OpenAI services are also registered (the failover
 * service needs them as collaborators).</p>
 */
@Service
@Primary
@ConditionalOnProperty(name = "kehdo.ai.llm.provider", havingValue = "failover")
public class FailoverLlmService implements LlmService {

    private static final Logger log = LoggerFactory.getLogger(FailoverLlmService.class);

    private final VertexAiLlmService primary;
    private final OpenAiLlmService fallback;

    public FailoverLlmService(VertexAiLlmService primary, OpenAiLlmService fallback) {
        this.primary = primary;
        this.fallback = fallback;
    }

    @Override
    public LlmResponse generate(LlmRequest request) {
        try {
            return primary.generate(request);
        } catch (CallNotPermittedException openBreaker) {
            log.warn("Vertex AI circuit breaker is open; failing over to OpenAI.");
            return fallback.generate(request);
        } catch (VertexAiResponseException badResponse) {
            log.warn("Vertex AI returned an unusable response; failing over to OpenAI: {}",
                    badResponse.getMessage());
            return fallback.generate(request);
        } catch (RuntimeException unexpected) {
            // Don't trust the primary if it threw something exotic; let the
            // fallback try. The fallback's own breaker will protect us if
            // OpenAI is also unhealthy.
            log.warn("Vertex AI threw {}; failing over to OpenAI: {}",
                    unexpected.getClass().getSimpleName(), unexpected.getMessage());
            return fallback.generate(request);
        }
    }
}
