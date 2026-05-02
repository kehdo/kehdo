package app.kehdo.backend.api.conversation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Wire shape for {@code POST /conversations/{id}/generate}.
 *
 * @param tone  ToneCode (validated server-side against the 18-value enum)
 * @param count optional, 1..5; defaults to 4 in {@link #effectiveCount()}
 */
public record GenerateRequest(
        @NotBlank String tone,
        @Min(1) @Max(5) Integer count) {

    public int effectiveCount() {
        return count == null ? 4 : count;
    }
}
