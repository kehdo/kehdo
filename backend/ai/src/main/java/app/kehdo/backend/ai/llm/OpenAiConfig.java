package app.kehdo.backend.ai.llm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Provisions the OpenAI {@link RestClient} + {@link OpenAiClient} when
 * the LLM provider includes OpenAI ({@code openai}-only or
 * {@code failover}). Skipped entirely on {@code stub} or {@code gcp}-only
 * — in those modes the bean isn't created, so missing
 * {@code OPENAI_API_KEY} doesn't fail startup.
 */
@Configuration
@ConditionalOnExpression(
        "'${kehdo.ai.llm.provider:stub}' == 'openai' || '${kehdo.ai.llm.provider:stub}' == 'failover'")
public class OpenAiConfig {

    private static final String DEFAULT_BASE_URL = "https://api.openai.com";

    @Bean
    public RestClient openAiRestClient(
            @Value("${kehdo.ai.openai.api-key}") String apiKey,
            @Value("${kehdo.ai.openai.base-url:" + DEFAULT_BASE_URL + "}") String baseUrl) {
        if (apiKey == null || apiKey.isBlank()) {
            // Fail fast at startup rather than at first request — if the
            // operator forgot the key, they should know immediately.
            throw new IllegalStateException(
                    "kehdo.ai.openai.api-key (env OPENAI_API_KEY) is required when " +
                    "kehdo.ai.llm.provider includes OpenAI.");
        }
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .requestFactory(simpleRequestFactory())
                .build();
    }

    @Bean
    public OpenAiClient openAiClient(RestClient openAiRestClient) {
        return new OpenAiClient(openAiRestClient);
    }

    /**
     * Connect timeout 5s, read timeout 15s. Resilience4j time-limiter
     * caps the whole call at 10s on top, but the underlying socket
     * timeout is the real failsafe if the time-limiter ever isn't
     * applied (e.g., via reflection in tests).
     */
    private static org.springframework.http.client.SimpleClientHttpRequestFactory simpleRequestFactory() {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(15).toMillis());
        return factory;
    }
}
