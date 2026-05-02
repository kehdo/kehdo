package app.kehdo.backend.ai.budget;

/**
 * Thrown by {@link TokenBudgeter#requireWithinBudget} when a fully-rendered
 * prompt exceeds the configured cap. The orchestrator catches this and
 * surfaces a 422 with code {@code SCREENSHOT_INVALID} (the only realistic
 * cause is an unusually long conversation).
 */
public class TokenBudgetExceededException extends RuntimeException {

    private final int actual;
    private final int budget;

    public TokenBudgetExceededException(int actual, int budget) {
        super("Prompt exceeds token budget: " + actual + " > " + budget);
        this.actual = actual;
        this.budget = budget;
    }

    public int getActual() { return actual; }
    public int getBudget() { return budget; }
}
