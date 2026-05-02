package app.kehdo.backend.ai.orchestrator;

/**
 * Input to the {@link GenerationOrchestrator}.
 *
 * @param screenshotObjectKey storage key from {@code conversations.screenshot_object_key}
 * @param toneCode            requested tone; verbatim from the API request
 * @param count               number of ranked replies to return, 1..5
 */
public record GenerationRequest(
        String screenshotObjectKey,
        String toneCode,
        int count) {

    public GenerationRequest {
        if (screenshotObjectKey == null || screenshotObjectKey.isBlank()) {
            throw new IllegalArgumentException("screenshotObjectKey must not be blank");
        }
        if (toneCode == null || toneCode.isBlank()) {
            throw new IllegalArgumentException("toneCode must not be blank");
        }
        if (count < 1 || count > 5) {
            throw new IllegalArgumentException("count must be between 1 and 5");
        }
    }
}
