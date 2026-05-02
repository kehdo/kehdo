package app.kehdo.backend.ai.llm;

/**
 * Thrown by {@link OpenAiClient} when the call fails for any reason
 * (transport error, non-2xx status, malformed envelope, empty content).
 * Treated as a transient failure by the {@code llm-openai} circuit
 * breaker and by {@link FailoverLlmService}, which will surface a
 * {@link app.kehdo.backend.ai.llm.LlmService}-level error to the
 * caller if the primary already failed.
 */
public class OpenAiCallException extends RuntimeException {

    public OpenAiCallException(String message) {
        super(message);
    }

    public OpenAiCallException(String message, Throwable cause) {
        super(message, cause);
    }
}
