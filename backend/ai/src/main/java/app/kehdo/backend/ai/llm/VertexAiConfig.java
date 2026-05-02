package app.kehdo.backend.ai.llm;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provisions the Vertex AI client + Gemini model whenever Vertex is in
 * play — that's {@code kehdo.ai.llm.provider=gcp} (Vertex-only) or
 * {@code =failover} (Vertex primary + OpenAI fallback). Auth is via
 * Application Default Credentials — locally the file gcloud writes to
 * {@code %APPDATA%\gcloud\application_default_credentials.json}; in
 * production the platform's attached service account / Workload Identity
 * is picked up automatically.
 *
 * <p>{@link GenerationConfig} is wired with
 * {@code responseMimeType=application/json} so Gemini emits a clean JSON
 * array of strings — matching the contract our
 * {@code generate-replies.mustache} prompt asks for. No markdown
 * fences, no preamble, just bytes the parser can hand directly to Jackson.</p>
 */
@Configuration
@ConditionalOnExpression(
        "'${kehdo.ai.llm.provider:stub}' == 'gcp' || '${kehdo.ai.llm.provider:stub}' == 'failover'")
public class VertexAiConfig {

    @Bean(destroyMethod = "close")
    public VertexAI vertexAi(
            @Value("${kehdo.ai.vertex.project-id}") String projectId,
            @Value("${kehdo.ai.vertex.location}") String location) {
        return new VertexAI(projectId, location);
    }

    @Bean
    public GenerativeModel geminiModel(
            VertexAI vertexAi,
            @Value("${kehdo.ai.vertex.model}") String modelName) {
        GenerationConfig config = GenerationConfig.newBuilder()
                .setResponseMimeType("application/json")
                .setMaxOutputTokens(1024)
                .setTemperature(0.7f)
                .setTopP(0.95f)
                .build();
        return new GenerativeModel(modelName, vertexAi).withGenerationConfig(config);
    }
}
