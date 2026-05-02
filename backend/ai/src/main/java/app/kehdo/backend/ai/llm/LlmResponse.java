package app.kehdo.backend.ai.llm;

import java.util.List;

/**
 * Output of a single {@link LlmService#generate} call.
 *
 * @param replies    {@code count} ranked replies, ordered best-first
 * @param modelUsed  identifier of the model that actually answered, e.g.
 *                   {@code "vertex-ai/gemini-2.0-flash"} or
 *                   {@code "openai/gpt-4o-mini"}. Persisted on every
 *                   {@code replies} row for the observability requirement
 *                   in {@code backend/CLAUDE.md}.
 */
public record LlmResponse(List<LlmReply> replies, String modelUsed) {

    public LlmResponse {
        if (replies == null || replies.isEmpty()) {
            throw new IllegalArgumentException("replies must not be empty");
        }
        if (modelUsed == null || modelUsed.isBlank()) {
            throw new IllegalArgumentException("modelUsed must not be blank");
        }
    }
}
