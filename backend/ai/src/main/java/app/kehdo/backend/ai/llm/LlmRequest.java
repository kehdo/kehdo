package app.kehdo.backend.ai.llm;

/**
 * Input to a single {@link LlmService#generate} call.
 *
 * @param prompt    fully-rendered prompt string (output of {@code PromptRenderer})
 * @param toneCode  the tone the user picked, propagated for telemetry only —
 *                  the prompt itself already encodes the tone instructions
 * @param count     how many ranked alternatives to ask for, 1..5
 */
public record LlmRequest(
        String prompt,
        String toneCode,
        int count) {

    public LlmRequest {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("prompt must not be blank");
        }
        if (count < 1 || count > 5) {
            throw new IllegalArgumentException("count must be between 1 and 5");
        }
    }
}
