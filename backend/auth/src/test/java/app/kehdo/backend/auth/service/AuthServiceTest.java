package app.kehdo.backend.auth.service;

import app.kehdo.backend.auth.error.EmailAlreadyRegisteredException;
import app.kehdo.backend.auth.error.InvalidCredentialsException;
import app.kehdo.backend.auth.error.RefreshTokenInvalidException;
import app.kehdo.backend.auth.jwt.JwtKeys;
import app.kehdo.backend.auth.jwt.JwtProperties;
import app.kehdo.backend.auth.jwt.JwtService;
import app.kehdo.backend.auth.session.Session;
import app.kehdo.backend.auth.session.SessionRepository;
import app.kehdo.backend.auth.token.RefreshTokens;
import app.kehdo.backend.user.User;
import app.kehdo.backend.user.UserPlan;
import app.kehdo.backend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private static final Instant NOW = Instant.parse("2026-05-01T10:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    private UserRepository userRepository;
    private SessionRepository sessionRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private JwtProperties jwtProperties;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        sessionRepository = mock(SessionRepository.class);
        passwordEncoder = new BCryptPasswordEncoder(4); // low cost for tests
        jwtProperties = new JwtProperties("https://api.kehdo.test", 300, 30, "", "");
        JwtKeys keys = JwtKeys.load(jwtProperties);
        jwtService = new JwtService(jwtProperties, keys, FIXED_CLOCK);

        authService = new AuthService(
                userRepository,
                sessionRepository,
                passwordEncoder,
                jwtService,
                jwtProperties,
                FIXED_CLOCK);
    }

    // -------------------- signup --------------------

    @Test
    @DisplayName("signup creates user + session and returns access + refresh tokens")
    void signup_happy_path() {
        when(userRepository.existsActiveByEmail("alex@example.com")).thenReturn(false);

        AuthService.AuthResult result = authService.signup(new AuthService.SignupCommand(
                "alex@example.com",
                "correct-horse-battery-staple",
                "Alex",
                "Mozilla/5.0",
                "203.0.113.1"));

        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.refreshToken()).startsWith("rt_").hasSize(67);
        assertThat(result.accessTokenExpiresAt())
                .isEqualTo(NOW.plusSeconds(jwtProperties.accessTokenTtlSeconds()));
        assertThat(result.user().getEmail()).isEqualTo("alex@example.com");
        assertThat(result.user().getDisplayName()).isEqualTo("Alex");
        assertThat(result.user().getPlan()).isEqualTo(UserPlan.STARTER);
        assertThat(passwordEncoder.matches("correct-horse-battery-staple", result.user().getPasswordHash()))
                .isTrue();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("alex@example.com");

        ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository).save(sessionCaptor.capture());
        Session savedSession = sessionCaptor.getValue();
        assertThat(savedSession.getUserAgent()).isEqualTo("Mozilla/5.0");
        assertThat(savedSession.getIpAddress()).isEqualTo("203.0.113.1");
        // Stored hash != raw token; never persist plaintext refresh tokens.
        assertThat(savedSession.getRefreshTokenHash())
                .isEqualTo(RefreshTokens.hash(result.refreshToken()))
                .isNotEqualTo(result.refreshToken());
    }

    @Test
    @DisplayName("signup with existing active email throws EmailAlreadyRegisteredException")
    void signup_email_taken() {
        when(userRepository.existsActiveByEmail("alex@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(new AuthService.SignupCommand(
                "alex@example.com", "any-password-of-min-len", null, null, null)))
                .isInstanceOf(EmailAlreadyRegisteredException.class);

        verify(userRepository, never()).save(any());
        verify(sessionRepository, never()).save(any());
    }

    // -------------------- login --------------------

    @Test
    @DisplayName("login with correct credentials issues a fresh session")
    void login_happy_path() {
        String passwordHash = passwordEncoder.encode("correct-horse-battery-staple");
        User user = new User(
                UUID.randomUUID(),
                "alex@example.com",
                passwordHash,
                "Alex",
                UserPlan.STARTER,
                NOW.minusSeconds(86400));
        when(userRepository.findActiveByEmail("alex@example.com")).thenReturn(Optional.of(user));

        AuthService.AuthResult result = authService.login(new AuthService.LoginCommand(
                "alex@example.com", "correct-horse-battery-staple", "Mozilla/5.0", "203.0.113.1"));

        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.user().getId()).isEqualTo(user.getId());
        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    @DisplayName("login with wrong password throws InvalidCredentialsException")
    void login_wrong_password() {
        User user = new User(
                UUID.randomUUID(),
                "alex@example.com",
                passwordEncoder.encode("correct-horse-battery-staple"),
                null, UserPlan.STARTER, NOW);
        when(userRepository.findActiveByEmail("alex@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new AuthService.LoginCommand(
                "alex@example.com", "WRONG", null, null)))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("login with unknown email throws InvalidCredentialsException — no info leak")
    void login_unknown_email() {
        when(userRepository.findActiveByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new AuthService.LoginCommand(
                "ghost@example.com", "anything", null, null)))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(sessionRepository, never()).save(any());
    }

    // -------------------- refresh --------------------

    @Test
    @DisplayName("refresh rotates the session and issues new tokens")
    void refresh_happy_path() {
        String rawRefresh = RefreshTokens.generate();
        String hash = RefreshTokens.hash(rawRefresh);
        UUID sessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Session session = new Session(
                sessionId, userId, hash, "ua", "ip",
                NOW.plusSeconds(86400), NOW.minusSeconds(60));
        User user = new User(
                userId, "alex@example.com",
                passwordEncoder.encode("any"), null, UserPlan.STARTER, NOW.minusSeconds(86400));

        when(sessionRepository.findByRefreshTokenHash(hash)).thenReturn(Optional.of(session));
        when(userRepository.findActiveById(userId)).thenReturn(Optional.of(user));

        AuthService.AuthResult result = authService.refresh(new AuthService.RefreshCommand(rawRefresh));

        // New refresh token returned and is different from the one passed in
        assertThat(result.refreshToken()).startsWith("rt_").isNotEqualTo(rawRefresh);
        // Same session id (rotation in place, not a new session row)
        assertThat(session.getId()).isEqualTo(sessionId);
        // Stored hash now matches the new returned token, not the old one
        assertThat(session.getRefreshTokenHash()).isEqualTo(RefreshTokens.hash(result.refreshToken()));
        // lastUsedAt advanced
        assertThat(session.getLastUsedAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("refresh with unknown token hash throws RefreshTokenInvalidException")
    void refresh_unknown_token() {
        String rawRefresh = RefreshTokens.generate();
        when(sessionRepository.findByRefreshTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(new AuthService.RefreshCommand(rawRefresh)))
                .isInstanceOf(RefreshTokenInvalidException.class);
    }

    @Test
    @DisplayName("refresh with revoked session throws RefreshTokenInvalidException")
    void refresh_revoked_session() {
        String rawRefresh = RefreshTokens.generate();
        String hash = RefreshTokens.hash(rawRefresh);
        Session session = new Session(
                UUID.randomUUID(), UUID.randomUUID(), hash, null, null,
                NOW.plusSeconds(86400), NOW.minusSeconds(60));
        session.revoke(NOW.minusSeconds(10));

        when(sessionRepository.findByRefreshTokenHash(hash)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> authService.refresh(new AuthService.RefreshCommand(rawRefresh)))
                .isInstanceOf(RefreshTokenInvalidException.class);
    }

    @Test
    @DisplayName("refresh with expired session throws RefreshTokenInvalidException")
    void refresh_expired_session() {
        String rawRefresh = RefreshTokens.generate();
        String hash = RefreshTokens.hash(rawRefresh);
        // expires_at in the past relative to FIXED_CLOCK
        Session session = new Session(
                UUID.randomUUID(), UUID.randomUUID(), hash, null, null,
                NOW.minusSeconds(60), NOW.minusSeconds(86400));

        when(sessionRepository.findByRefreshTokenHash(hash)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> authService.refresh(new AuthService.RefreshCommand(rawRefresh)))
                .isInstanceOf(RefreshTokenInvalidException.class);
    }

    @Test
    @DisplayName("refresh with deleted user throws RefreshTokenInvalidException")
    void refresh_deleted_user() {
        String rawRefresh = RefreshTokens.generate();
        String hash = RefreshTokens.hash(rawRefresh);
        UUID userId = UUID.randomUUID();
        Session session = new Session(
                UUID.randomUUID(), userId, hash, null, null,
                NOW.plusSeconds(86400), NOW.minusSeconds(60));

        when(sessionRepository.findByRefreshTokenHash(hash)).thenReturn(Optional.of(session));
        when(userRepository.findActiveById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(new AuthService.RefreshCommand(rawRefresh)))
                .isInstanceOf(RefreshTokenInvalidException.class);
    }

    // -------------------- logout --------------------

    @Test
    @DisplayName("logout revokes the session")
    void logout_happy_path() {
        UUID sessionId = UUID.randomUUID();
        Session session = new Session(
                sessionId, UUID.randomUUID(), "hash", null, null,
                NOW.plusSeconds(86400), NOW.minusSeconds(60));
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        authService.logout(sessionId);

        assertThat(session.getRevokedAt()).isEqualTo(NOW);
        verify(sessionRepository, times(1)).findById(sessionId);
    }

    @Test
    @DisplayName("logout for unknown session is a no-op (does not throw)")
    void logout_unknown_session() {
        UUID sessionId = UUID.randomUUID();
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        authService.logout(sessionId); // must not throw
    }
}
