package app.kehdo.backend.ai.orchestrator;

import app.kehdo.backend.ai.budget.TokenBudgeter;
import app.kehdo.backend.ai.llm.LlmReply;
import app.kehdo.backend.ai.llm.LlmRequest;
import app.kehdo.backend.ai.llm.LlmResponse;
import app.kehdo.backend.ai.llm.LlmService;
import app.kehdo.backend.ai.ocr.OcrResult;
import app.kehdo.backend.ai.ocr.OcrService;
import app.kehdo.backend.ai.prompt.PromptRenderer;
import app.kehdo.backend.ai.safety.ModerationClient;
import app.kehdo.backend.ai.speaker.SpeakerAttributor;
import app.kehdo.backend.ai.speaker.SpeakerAttributor.AttributedLine;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Conducts a single reply-generation run: OCR → speaker attribution →
 * prompt render → LLM → moderation. Pure orchestration; persistence is
 * the caller's responsibility (see {@code :api/conversation/ConversationService}).
 *
 * <p>Phase 4 PR 4 ships the straight-line pipeline. Caching of OCR
 * output (re-using {@code parsed_messages} when only the tone changed)
 * lands in PR 12 once we have real cloud calls worth caching.</p>
 */
@Component
public class GenerationOrchestrator {

    private static final String TEMPLATE = "generate-replies";

    private final OcrService ocrService;
    private final SpeakerAttributor speakerAttributor;
    private final PromptRenderer promptRenderer;
    private final TokenBudgeter tokenBudgeter;
    private final LlmService llmService;
    private final ModerationClient moderationClient;

    public GenerationOrchestrator(
            OcrService ocrService,
            SpeakerAttributor speakerAttributor,
            PromptRenderer promptRenderer,
            TokenBudgeter tokenBudgeter,
            LlmService llmService,
            ModerationClient moderationClient) {
        this.ocrService = ocrService;
        this.speakerAttributor = speakerAttributor;
        this.promptRenderer = promptRenderer;
        this.tokenBudgeter = tokenBudgeter;
        this.llmService = llmService;
        this.moderationClient = moderationClient;
    }

    public GenerationOutput run(GenerationRequest request) {
        OcrResult ocrResult = ocrService.read(request.screenshotObjectKey());
        List<AttributedLine> attributed = speakerAttributor.attribute(ocrResult.lines());

        String prompt = promptRenderer.render(TEMPLATE, Map.of(
                "tone", request.toneCode(),
                "conversation", renderConversation(attributed),
                "count", String.valueOf(request.count())));
        tokenBudgeter.requireWithinBudget(prompt);

        LlmResponse llmResponse = llmService.generate(
                new LlmRequest(prompt, request.toneCode(), request.count()));

        List<LlmReply> approved = llmResponse.replies().stream()
                .filter(r -> moderationClient.check(r.text()).allowed())
                .toList();

        if (approved.isEmpty()) {
            throw new ContentBlockedException();
        }

        return new GenerationOutput(attributed, approved, llmResponse.modelUsed());
    }

    private static String renderConversation(List<AttributedLine> lines) {
        return lines.stream()
                .map(l -> l.speaker().name() + ": " + l.text())
                .collect(Collectors.joining("\n"));
    }
}
