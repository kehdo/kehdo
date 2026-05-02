package app.kehdo.backend.api.conversation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Wire shape for {@code POST /replies/{id}/refine}.
 *
 * @param instructions free-text refinement directives (e.g. "shorter",
 *                     "less formal", "add a question"). 1–200 chars per
 *                     {@code contracts/openapi/kehdo.v1.yaml}.
 */
public record RefineRequest(
        @NotBlank @Size(min = 1, max = 200) String instructions) {
}
