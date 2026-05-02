package app.kehdo.backend.api.conversation;

import app.kehdo.backend.api.conversation.dto.RefineRequest;
import app.kehdo.backend.api.conversation.dto.ReplyDto;
import app.kehdo.backend.auth.web.JwtAuthenticationFilter;
import app.kehdo.backend.common.error.ApiException;
import app.kehdo.backend.common.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST surface for {@code /replies/*} per
 * {@code contracts/openapi/kehdo.v1.yaml}. Currently only refine; reply
 * "select" (interaction-signal flywheel — AI rule 11) and "delete" land
 * in later PRs.
 */
@RestController
@RequestMapping("/replies")
public class ReplyController {

    private final ReplyService service;

    public ReplyController(ReplyService service) {
        this.service = service;
    }

    @PostMapping("/{id}/refine")
    public ReplyDto refineReply(
            HttpServletRequest req,
            @PathVariable UUID id,
            @Valid @RequestBody RefineRequest body) {
        return service.refine(currentUserId(req), id, body.instructions());
    }

    private static UUID currentUserId(HttpServletRequest req) {
        UUID id = (UUID) req.getAttribute(JwtAuthenticationFilter.USER_ID_ATTRIBUTE);
        if (id == null) {
            throw new ApiException(
                    ErrorCode.UNAUTHORIZED,
                    HttpStatus.UNAUTHORIZED.value(),
                    "Authentication required.");
        }
        return id;
    }
}
