package app.kehdo.backend.ai.budget;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenBudgeterTest {

    @Test
    void empty_string_estimates_to_zero_tokens() {
        TokenBudgeter b = new TokenBudgeter(3000);

        assertThat(b.estimateTokens("")).isZero();
        assertThat(b.estimateTokens(null)).isZero();
    }

    @Test
    void approximates_one_token_per_four_chars() {
        TokenBudgeter b = new TokenBudgeter(3000);
        // "hello world" = 11 chars → ceil(11/4) = 3 tokens
        assertThat(b.estimateTokens("hello world")).isEqualTo(3);
        // 100 chars → 25 tokens exactly
        String s = "a".repeat(100);
        assertThat(b.estimateTokens(s)).isEqualTo(25);
    }

    @Test
    void fits_returns_true_when_within_budget_false_when_over() {
        TokenBudgeter b = new TokenBudgeter(10);
        assertThat(b.fits("a".repeat(40))).isTrue();   // 40/4 = 10 tokens
        assertThat(b.fits("a".repeat(41))).isFalse();  // ceil(41/4) = 11 tokens
    }

    @Test
    void require_within_budget_throws_with_actual_and_budget() {
        TokenBudgeter b = new TokenBudgeter(10);
        String prompt = "a".repeat(80); // 20 tokens

        assertThatThrownBy(() -> b.requireWithinBudget(prompt))
                .isInstanceOf(TokenBudgetExceededException.class)
                .satisfies(ex -> {
                    TokenBudgetExceededException tex = (TokenBudgetExceededException) ex;
                    assertThat(tex.getActual()).isEqualTo(20);
                    assertThat(tex.getBudget()).isEqualTo(10);
                });
    }

    @Test
    void rejects_zero_or_negative_budget() {
        assertThatThrownBy(() -> new TokenBudgeter(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TokenBudgeter(-1)).isInstanceOf(IllegalArgumentException.class);
    }
}
