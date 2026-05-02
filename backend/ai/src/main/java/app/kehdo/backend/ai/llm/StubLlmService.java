package app.kehdo.backend.ai.llm;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Phase 4 PR 3 placeholder. Returns generic ranked replies regardless of
 * input so PR 4 can stand up the {@code POST /conversations/{id}/generate}
 * controller end-to-end before real LLM credentials are in hand.
 *
 * <p>Active when the {@code stub-llm} profile is on (default in
 * {@code application-local.yml} / dev). Real adapters (Phase 4 PR 7 Vertex
 * AI, PR 8 OpenAI failover) will register a higher-precedence
 * {@link LlmService} bean and disable this one.</p>
 */
@Service
@Profile({"stub-llm", "test", "default"})
public class StubLlmService implements LlmService {

    /** Identifier persisted on {@code replies.model_used}. Used to filter stub-generated rows out of analytics. */
    public static final String MODEL_ID = "stub/canned-v1";

    private static final List<String> CANNED_BODIES = List.of(
            "Sounds good!",
            "Tell me more — what feels off about it?",
            "I hear you. Want to talk through it?",
            "Got it, let's figure it out.",
            "Whatever you decide is fine with me."
    );

    @Override
    public LlmResponse generate(LlmRequest request) {
        int n = Math.min(request.count(), CANNED_BODIES.size());
        List<LlmReply> replies = java.util.stream.IntStream.range(0, n)
                .mapToObj(i -> new LlmReply(i + 1, CANNED_BODIES.get(i)))
                .toList();
        return new LlmResponse(replies, MODEL_ID);
    }
}
