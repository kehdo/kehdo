package app.kehdo.backend.ai.llm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Thin HTTP wrapper over OpenAI's {@code POST /v1/chat/completions}.
 * Avoids pulling another SDK — the API surface we need (one endpoint,
 * fixed request shape) is small enough that a Jackson-mapped record
 * pair is clearer than an extra dependency.
 */
public class OpenAiClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiClient.class);
    private static final String CHAT_COMPLETIONS_PATH = "/v1/chat/completions";

    private final RestClient http;

    public OpenAiClient(RestClient http) {
        this.http = http;
    }

    /**
     * @return raw assistant content string from the first choice, never null
     * @throws OpenAiCallException on transport / non-2xx / shape errors
     */
    public String complete(ChatCompletionRequest request) {
        try {
            ChatCompletionResponse response = http.post()
                    .uri(CHAT_COMPLETIONS_PATH)
                    .body(request)
                    .retrieve()
                    .body(ChatCompletionResponse.class);
            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new OpenAiCallException("OpenAI response had no choices");
            }
            String content = response.choices().get(0).message().content();
            if (content == null || content.isBlank()) {
                throw new OpenAiCallException("OpenAI returned empty content");
            }
            return content;
        } catch (OpenAiCallException e) {
            throw e;
        } catch (Exception e) {
            log.warn("OpenAI call failed: {}", e.getMessage());
            throw new OpenAiCallException("OpenAI request failed: " + e.getMessage(), e);
        }
    }

    // ---- request / response records --------------------------------------

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ChatCompletionRequest(
            String model,
            List<Message> messages,
            @JsonProperty("response_format") ResponseFormat responseFormat,
            @JsonProperty("max_tokens") Integer maxTokens,
            Double temperature) {

        public static ChatCompletionRequest jsonObject(String model, String userPrompt, int maxTokens, double temperature) {
            return new ChatCompletionRequest(
                    model,
                    List.of(new Message("user", userPrompt)),
                    new ResponseFormat("json_object"),
                    maxTokens,
                    temperature);
        }
    }

    public record Message(String role, String content) {}

    public record ResponseFormat(String type) {}

    public record ChatCompletionResponse(List<Choice> choices, String model) {
        public record Choice(Message message, @JsonProperty("finish_reason") String finishReason) {}
    }
}
