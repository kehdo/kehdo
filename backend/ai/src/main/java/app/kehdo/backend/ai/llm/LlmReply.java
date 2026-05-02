package app.kehdo.backend.ai.llm;

/**
 * One ranked reply produced by {@link LlmService}. {@code rank} is 1-indexed
 * with 1 = best per the model's own ranking.
 */
public record LlmReply(int rank, String text) {

    public LlmReply {
        if (rank < 1 || rank > 5) {
            throw new IllegalArgumentException("rank must be between 1 and 5");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }
    }
}
