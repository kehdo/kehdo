package app.kehdo.backend.api.me;

import app.kehdo.backend.api.auth.dto.UserDto;
import app.kehdo.backend.auth.web.JwtAuthenticationFilter;
import app.kehdo.backend.common.error.ApiException;
import app.kehdo.backend.common.error.ErrorCode;
import app.kehdo.backend.user.User;
import app.kehdo.backend.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Current-user endpoints, mounted under {@code /v1/me/*}.
 *
 * <p>Implements {@code contracts/openapi/kehdo.v1.yaml}#paths./me. Future
 * sibling endpoints ({@code /me/usage} for quota state) live here too.
 * Authentication is enforced by {@code SecurityConfig.anyRequest().authenticated()};
 * the user UUID is read from the request attribute that
 * {@link JwtAuthenticationFilter} populates from the access-token claims.</p>
 */
@RestController
@RequestMapping("/me")
public class MeController {

    private final UserRepository userRepository;

    public MeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public UserDto getCurrentUser(HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute(JwtAuthenticationFilter.USER_ID_ATTRIBUTE);
        if (userId == null) {
            // SecurityConfig guarantees we never get here with a valid filter
            // chain — but if the attribute is missing the safest answer is
            // "your token didn't identify a user", not a 500.
            throw new ApiException(
                    ErrorCode.UNAUTHORIZED,
                    HttpStatus.UNAUTHORIZED.value(),
                    "Authentication required.");
        }
        User user = userRepository.findActiveById(userId)
                // JWT was valid but the user was soft-deleted between issue
                // and request. Treat as "your session is no longer valid"
                // so the client clears tokens and signs out.
                .orElseThrow(() -> new ApiException(
                        ErrorCode.UNAUTHORIZED,
                        HttpStatus.UNAUTHORIZED.value(),
                        "Session no longer valid."));
        return UserDto.from(user);
    }
}
