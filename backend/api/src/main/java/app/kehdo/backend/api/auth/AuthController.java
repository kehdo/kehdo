package app.kehdo.backend.api.auth;

import app.kehdo.backend.api.auth.dto.AuthResponse;
import app.kehdo.backend.api.auth.dto.RefreshRequest;
import app.kehdo.backend.api.auth.dto.SignInRequest;
import app.kehdo.backend.api.auth.dto.SignUpRequest;
import app.kehdo.backend.auth.service.AuthService;
import app.kehdo.backend.auth.service.AuthService.AuthResult;
import app.kehdo.backend.auth.web.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.util.UUID;

/**
 * Email + password authentication endpoints, mounted under {@code /v1/auth/*}
 * via {@code server.servlet.context-path}.
 *
 * <p>Implements the contract in
 * {@code contracts/openapi/kehdo.v1.yaml}#paths./auth/{signup,login,refresh,logout}.
 * The {@code /auth/google} variant defined in the spec is intentionally NOT
 * implemented yet — see {@code contracts/CHANGELOG.md} (deferred to post-Phase 2).</p>
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final Clock clock;

    public AuthController(AuthService authService, Clock clock) {
        this.authService = authService;
        this.clock = clock;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUp(
            @Valid @RequestBody SignUpRequest body,
            HttpServletRequest req) {
        AuthResult result = authService.signup(new AuthService.SignupCommand(
                body.email(),
                body.password(),
                body.displayName(),
                req.getHeader("User-Agent"),
                clientIp(req)));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthResponse.from(result, clock.instant()));
    }

    @PostMapping("/login")
    public AuthResponse signIn(
            @Valid @RequestBody SignInRequest body,
            HttpServletRequest req) {
        AuthResult result = authService.login(new AuthService.LoginCommand(
                body.email(),
                body.password(),
                req.getHeader("User-Agent"),
                clientIp(req)));
        return AuthResponse.from(result, clock.instant());
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest body) {
        AuthResult result = authService.refresh(new AuthService.RefreshCommand(body.refreshToken()));
        return AuthResponse.from(result, clock.instant());
    }

    @PostMapping("/logout")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.NO_CONTENT)
    public void signOut(HttpServletRequest req) {
        UUID sessionId = (UUID) req.getAttribute(JwtAuthenticationFilter.SESSION_ID_ATTRIBUTE);
        if (sessionId != null) {
            authService.logout(sessionId);
        }
        // No-op if the request had no session — security chain already guarantees
        // an authenticated principal here, so a missing attribute would indicate
        // a misconfiguration but is not a failure mode worth exposing.
    }

    private static String clientIp(HttpServletRequest req) {
        // server.forward-headers-strategy=framework already populates
        // request.getRemoteAddr() with the X-Forwarded-For client IP behind
        // a proxy.
        return req.getRemoteAddr();
    }
}
