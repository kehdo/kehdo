package app.kehdo.backend.ai.llm;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Placeholder LLM. Returns generic ranked replies regardless of input so
 * the {@code POST /conversations/{id}/generate} controller can run
 * end-to-end before real credentials.
 *
 * <p>Active when {@code kehdo.ai.llm.provider=stub} (the default; flip to
 * {@code gcp} once Phase 4 PR 7 lands the Vertex AI adapter, or
 * {@code openai} for PR 8's failover-only mode).</p>
 */
@Service
@ConditionalOnProperty(name = "kehdo.ai.llm.provider", havingValue = "stub", matchIfMissing = true)
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
