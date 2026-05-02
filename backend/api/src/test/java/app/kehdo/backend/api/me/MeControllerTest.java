package app.kehdo.backend.api.me;

import app.kehdo.backend.api.auth.dto.UserDto;
import app.kehdo.backend.auth.web.JwtAuthenticationFilter;
import app.kehdo.backend.common.error.ApiException;
import app.kehdo.backend.common.error.ErrorCode;
import app.kehdo.backend.user.User;
import app.kehdo.backend.user.UserPlan;
import app.kehdo.backend.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MeControllerTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final MeController controller = new MeController(userRepository);

    @Test
    void should_return_user_dto_when_authenticated() {
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-04-30T10:15:30Z");
        User user = new User(
                userId,
                "alex@example.com",
                "$2a$12$hash",
                "Alex",
                UserPlan.STARTER,
                createdAt);
        when(request.getAttribute(JwtAuthenticationFilter.USER_ID_ATTRIBUTE)).thenReturn(userId);
        when(userRepository.findActiveById(userId)).thenReturn(Optional.of(user));

        UserDto dto = controller.getCurrentUser(request);

        assertThat(dto.id()).isEqualTo(userId);
        assertThat(dto.email()).isEqualTo("alex@example.com");
        assertThat(dto.displayName()).isEqualTo("Alex");
        assertThat(dto.plan()).isEqualTo("STARTER");
        assertThat(dto.createdAt()).isEqualTo(createdAt);
    }

    @Test
    void should_throw_unauthorized_when_user_id_attribute_missing() {
        when(request.getAttribute(JwtAuthenticationFilter.USER_ID_ATTRIBUTE)).thenReturn(null);

        assertThatThrownBy(() -> controller.getCurrentUser(request))
                .isInstanceOf(ApiException.class)
                .hasMessage("Authentication required.")
                .extracting("code", "httpStatus")
                .containsExactly(ErrorCode.UNAUTHORIZED, 401);
    }

    @Test
    void should_throw_unauthorized_when_user_soft_deleted_after_token_issue() {
        UUID userId = UUID.randomUUID();
        when(request.getAttribute(JwtAuthenticationFilter.USER_ID_ATTRIBUTE)).thenReturn(userId);
        when(userRepository.findActiveById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.getCurrentUser(request))
                .isInstanceOf(ApiException.class)
                .hasMessage("Session no longer valid.")
                .extracting("code", "httpStatus")
                .containsExactly(ErrorCode.UNAUTHORIZED, 401);
    }
}
