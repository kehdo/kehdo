package app.kehdo.backend.ai.orchestrator;

import app.kehdo.backend.ai.budget.TokenBudgeter;
import app.kehdo.backend.ai.llm.LlmReply;
import app.kehdo.backend.ai.llm.LlmRequest;
import app.kehdo.backend.ai.llm.LlmResponse;
import app.kehdo.backend.ai.llm.LlmService;
import app.kehdo.backend.ai.prompt.PromptRenderer;
import app.kehdo.backend.ai.safety.ModerationClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Reworks a single reply per the user's freeform instructions.
 *
 * <p>Distinct from {@link GenerationOrchestrator} because it doesn't
 * touch OCR or speaker attribution — refine operates on text the user
 * already saw and chose to tweak. Same {@link LlmService} (so it
 * benefits from circuit-breaker + failover), same
 * {@link ModerationClient} gate, just a different prompt template.</p>
 *
 * <p>Refine returns a single reply, not a ranked list. The
 * {@code refine-reply.mustache} template asks for a JSON array of one
 * string so the parser path is shared with generate.</p>
 */
@Component
public class RefineOrchestrator {

    private static final String TEMPLATE = "refine-reply";

    private final PromptRenderer promptRenderer;
    private final TokenBudgeter tokenBudgeter;
    private final LlmService llmService;
    private final ModerationClient moderationClient;

    public RefineOrchestrator(
            PromptRenderer promptRenderer,
            TokenBudgeter tokenBudgeter,
            LlmService llmService,
            ModerationClient moderationClient) {
        this.promptRenderer = promptRenderer;
        this.tokenBudgeter = tokenBudgeter;
        this.llmService = llmService;
        this.moderationClient = moderationClient;
    }

    /**
     * @return refined reply text + the model that produced it
     * @throws ContentBlockedException when moderation blocks the output
     */
    public RefineOutput refine(RefineInput request) {
        String prompt = promptRenderer.render(TEMPLATE, Map.of(
                "originalReply", request.originalReply(),
                "tone", request.tone(),
                "instructions", request.instructions()));
        tokenBudgeter.requireWithinBudget(prompt);

        LlmResponse response = llmService.generate(
                new LlmRequest(prompt, request.tone(), 1));
        if (response.replies().isEmpty()) {
            throw new ContentBlockedException();
        }

        LlmReply candidate = response.replies().get(0);
        if (!moderationClient.check(candidate.text()).allowed()) {
            throw new ContentBlockedException();
        }
        return new RefineOutput(candidate.text(), response.modelUsed());
    }

    public record RefineInput(String originalReply, String tone, String instructions) {

        public RefineInput {
            if (originalReply == null || originalReply.isBlank()) {
                throw new IllegalArgumentException("originalReply must not be blank");
            }
            if (tone == null || tone.isBlank()) {
                throw new IllegalArgumentException("tone must not be blank");
            }
            if (instructions == null || instructions.isBlank()) {
                throw new IllegalArgumentException("instructions must not be blank");
            }
        }
    }

    public record RefineOutput(String text, String modelUsed) {}
}
