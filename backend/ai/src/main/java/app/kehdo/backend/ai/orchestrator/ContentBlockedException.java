package app.kehdo.backend.ai.orchestrator;

/**
 * Thrown when every candidate reply was blocked by {@code ModerationClient}.
 * The controller maps this to a 422 with code {@code CONTENT_BLOCKED}
 * per the contract.
 */
public class ContentBlockedException extends RuntimeException {

    public ContentBlockedException() {
        super("All candidate replies were blocked by moderation.");
    }
}
