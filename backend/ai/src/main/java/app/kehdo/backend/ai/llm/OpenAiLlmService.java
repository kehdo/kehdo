package app.kehdo.backend.ai.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Real LLM adapter backed by OpenAI gpt-4o-mini. Activated by
 * {@code kehdo.ai.llm.provider=openai} (OpenAI-only) or
 * {@code =failover} (composes with Vertex per AI rule 4).
 *
 * <p>Uses {@code response_format=json_object} on the OpenAI request so
 * the model is constrained to valid JSON. Our prompt asks for an
 * <em>array</em> of strings, but {@code json_object} mandates a top-level
 * object — gpt-4o-mini reliably wraps as {@code {"replies": [...]}} or
 * similar; the shared {@link VertexAiResponseParser} handles either
 * shape via its wrapped-array branch.</p>
 *
 * <p>Wrapped by Resilience4j's {@code llm-openai} circuit breaker per
 * {@code backend/CLAUDE.md} AI rule 4. Configured in
 * {@code application.yml}.</p>
 */
@Service
@ConditionalOnExpression(
        "'${kehdo.ai.llm.provider:stub}' == 'openai' || '${kehdo.ai.llm.provider:stub}' == 'failover'")
public class OpenAiLlmService implements LlmService {

    private static final String CB_NAME = "llm-openai";

    /** Hard cap on output tokens — 1024 fits five 220-char replies comfortably. */
    private static final int MAX_OUTPUT_TOKENS = 1024;
    private static final double TEMPERATURE = 0.7;

    private final OpenAiClient client;
    private final VertexAiResponseParser parser;
    private final String modelName;
    private final String modelId;

    public OpenAiLlmService(
            OpenAiClient client,
            ObjectMapper objectMapper,
            @Value("${kehdo.ai.openai.model}") String modelName) {
        this.client = client;
        this.parser = new VertexAiResponseParser(objectMapper);
        this.modelName = modelName;
        // modelId is what we persist on replies.model_used per AI rule 7.
        this.modelId = "openai/" + modelName;
    }

    @Override
    @CircuitBreaker(name = CB_NAME)
    @Retry(name = CB_NAME)
    public LlmResponse generate(LlmRequest request) {
        OpenAiClient.ChatCompletionRequest body = OpenAiClient.ChatCompletionRequest.jsonObject(
                modelName,
                request.prompt(),
                MAX_OUTPUT_TOKENS,
                TEMPERATURE);

        String raw = client.complete(body);
        // Same parser as Vertex — handles wrapped objects ({"replies": [...]})
        // which is what json_object mode produces.
        List<String> texts = parser.parse(raw);
        if (texts.isEmpty()) {
            throw new OpenAiCallException("OpenAI returned an empty reply array");
        }

        int n = Math.min(texts.size(), request.count());
        List<LlmReply> replies = IntStream.range(0, n)
                .mapToObj(i -> new LlmReply(i + 1, texts.get(i).trim()))
                .toList();
        return new LlmResponse(replies, modelId);
    }
}
