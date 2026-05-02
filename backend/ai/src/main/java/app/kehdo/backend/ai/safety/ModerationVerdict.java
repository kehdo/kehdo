package app.kehdo.backend.ai.safety;

/**
 * Result of running candidate text through the moderation pipeline.
 *
 * @param allowed       false → caller must drop this reply and either fall
 *                      back to a sibling or surface {@code CONTENT_BLOCKED}
 * @param category      coarse reason ("HATE", "SEXUAL_MINORS", "VIOLENCE",
 *                      "SELF_HARM", "OTHER", or {@code null} when allowed)
 * @param providerScore raw provider confidence in [0.0, 1.0] for telemetry
 *                      only — never gate decisions on this directly
 */
public record ModerationVerdict(
        boolean allowed,
        String category,
        Double providerScore) {

    public static ModerationVerdict allow() {
        return new ModerationVerdict(true, null, null);
    }

    public static ModerationVerdict block(String category, Double providerScore) {
        return new ModerationVerdict(false, category, providerScore);
    }
}
