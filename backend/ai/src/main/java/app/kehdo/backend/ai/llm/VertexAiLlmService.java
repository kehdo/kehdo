package app.kehdo.backend.ai.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Real LLM adapter backed by Vertex AI Gemini 2.0 Flash. Activated by
 * {@code kehdo.ai.llm.provider=gcp}; otherwise {@link StubLlmService}
 * runs.
 *
 * <p>Wrapped by Resilience4j's {@code llm-vertex} circuit breaker per
 * {@code backend/CLAUDE.md} AI rule 4. The OpenAI failover (Phase 4 PR
 * 8) will compose this service via a higher-level {@code FailoverLlmService}
 * that catches breaker-open / IO failures and falls through.</p>
 */
@Service
@ConditionalOnProperty(name = "kehdo.ai.llm.provider", havingValue = "gcp")
public class VertexAiLlmService implements LlmService {

    private static final Logger log = LoggerFactory.getLogger(VertexAiLlmService.class);
    private static final String CB_NAME = "llm-vertex";

    private final GenerativeModel model;
    private final VertexAiResponseParser parser;
    private final String modelId;

    public VertexAiLlmService(
            GenerativeModel model,
            ObjectMapper objectMapper,
            @Value("${kehdo.ai.vertex.model}") String modelName) {
        this.model = model;
        this.parser = new VertexAiResponseParser(objectMapper);
        // modelId is what we persist on replies.model_used per AI rule 7;
        // prefix with the vendor so analytics can bucket by provider.
        this.modelId = "vertex-ai/" + modelName;
    }

    @Override
    @CircuitBreaker(name = CB_NAME)
    @Retry(name = CB_NAME)
    public LlmResponse generate(LlmRequest request) {
        GenerateContentResponse response;
        try {
            response = model.generateContent(request.prompt());
        } catch (IOException e) {
            log.warn("Vertex AI call failed: {}", e.getMessage());
            // Wrap so the circuit breaker counts it (it counts RuntimeException only).
            throw new VertexAiResponseException("Vertex AI request failed: " + e.getMessage());
        }

        String rawText = ResponseHandler.getText(response);
        List<String> texts = parser.parse(rawText);
        if (texts.isEmpty()) {
            throw new VertexAiResponseException("Vertex AI returned an empty reply array");
        }

        // Cap at the requested count — the model occasionally over-delivers,
        // and the contract pins count at 1..5.
        int n = Math.min(texts.size(), request.count());
        List<LlmReply> replies = IntStream.range(0, n)
                .mapToObj(i -> new LlmReply(i + 1, texts.get(i).trim()))
                .toList();

        return new LlmResponse(replies, modelId);
    }
}
