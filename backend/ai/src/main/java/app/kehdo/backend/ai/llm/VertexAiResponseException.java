package app.kehdo.backend.ai.llm;

/**
 * Raised when Gemini returns text we can't shape into a {@code List<String>}.
 * Treated as a transient failure by the {@code llm-vertex} circuit breaker
 * — a single bad response shouldn't trip the breaker, but a sustained
 * stream of them should.
 */
public class VertexAiResponseException extends RuntimeException {

    public VertexAiResponseException(String message) {
        super(message);
    }
}
