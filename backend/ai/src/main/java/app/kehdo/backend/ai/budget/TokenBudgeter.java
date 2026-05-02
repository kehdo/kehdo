package app.kehdo.backend.ai.budget;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Estimates token counts and enforces the 3,000-token context limit per
 * {@code backend/CLAUDE.md} AI rule 3.
 *
 * <p>Approximation: ~4 chars per token, the OpenAI guideline that's also
 * close enough for Gemini's tokenizer. Real per-model tokenizers can
 * replace this when accuracy starts mattering (probably never — we're
 * far below context windows in practice).</p>
 *
 * <p>Default cap is 3,000 tokens; override with
 * {@code kehdo.ai.token-budget=N} for tests.</p>
 */
@Component
public class TokenBudgeter {

    /** Average chars-per-token for English text in BPE-style tokenizers. */
    private static final double CHARS_PER_TOKEN = 4.0;

    private final int maxTokens;

    public TokenBudgeter(@Value("${kehdo.ai.token-budget:3000}") int maxTokens) {
        if (maxTokens < 1) {
            throw new IllegalArgumentException("token budget must be >= 1");
        }
        this.maxTokens = maxTokens;
    }

    public int estimateTokens(String text) {
        if (text == null || text.isEmpty()) return 0;
        return (int) Math.ceil(text.length() / CHARS_PER_TOKEN);
    }

    public int maxTokens() {
        return maxTokens;
    }

    /** True when the prompt fits within the configured budget. */
    public boolean fits(String prompt) {
        return estimateTokens(prompt) <= maxTokens;
    }

    /**
     * Throws if the prompt exceeds the budget. The orchestrator calls this
     * before any LLM call so we never spend tokens on a prompt that would
     * fail validation upstream.
     */
    public void requireWithinBudget(String prompt) {
        int tokens = estimateTokens(prompt);
        if (tokens > maxTokens) {
            throw new TokenBudgetExceededException(tokens, maxTokens);
        }
    }
}
