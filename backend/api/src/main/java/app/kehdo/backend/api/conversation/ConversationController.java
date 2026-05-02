package app.kehdo.backend.api.conversation;

import app.kehdo.backend.api.conversation.dto.ConversationDto;
import app.kehdo.backend.api.conversation.dto.ConversationPageDto;
import app.kehdo.backend.api.conversation.dto.CreateConversationResponse;
import app.kehdo.backend.api.conversation.dto.GenerateRequest;
import app.kehdo.backend.api.conversation.dto.GenerateResponse;
import app.kehdo.backend.auth.web.JwtAuthenticationFilter;
import app.kehdo.backend.common.error.ApiException;
import app.kehdo.backend.common.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST surface for {@code /conversations/*} per
 * {@code contracts/openapi/kehdo.v1.yaml}.
 *
 * <p>All endpoints are bearer-authed by
 * {@code SecurityConfig.anyRequest().authenticated()}. The user UUID is
 * read from the request attribute that {@code JwtAuthenticationFilter}
 * populates from the access-token claims — same pattern as
 * {@link app.kehdo.backend.api.me.MeController}.</p>
 */
@RestController
@RequestMapping("/conversations")
public class ConversationController {

    private final ConversationService service;

    public ConversationController(ConversationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CreateConversationResponse> createConversation(HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(currentUserId(req)));
    }

    @GetMapping
    public ConversationPageDto listConversations(
            HttpServletRequest req,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String cursor) {
        if (limit < 1 || limit > 100) {
            throw new ApiException(
                    ErrorCode.BAD_REQUEST,
                    HttpStatus.BAD_REQUEST.value(),
                    "limit must be between 1 and 100");
        }
        return service.list(currentUserId(req), limit, cursor);
    }

    @GetMapping("/{id}")
    public ConversationDto getConversation(HttpServletRequest req, @PathVariable UUID id) {
        return service.get(currentUserId(req), id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteConversation(HttpServletRequest req, @PathVariable UUID id) {
        service.delete(currentUserId(req), id);
    }

    @PostMapping("/{id}/generate")
    public GenerateResponse generateReplies(
            HttpServletRequest req,
            @PathVariable UUID id,
            @Valid @RequestBody GenerateRequest body) {
        return service.generate(
                currentUserId(req),
                id,
                body.tone(),
                body.effectiveCount());
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
