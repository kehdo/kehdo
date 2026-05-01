package app.kehdo.backend.auth.service;

import app.kehdo.backend.auth.error.EmailAlreadyRegisteredException;
import app.kehdo.backend.auth.error.EmailDomainNotAllowedException;
import app.kehdo.backend.auth.error.InvalidCredentialsException;
import app.kehdo.backend.auth.error.RefreshTokenInvalidException;
import app.kehdo.backend.auth.jwt.JwtProperties;
import app.kehdo.backend.auth.jwt.JwtService;
import app.kehdo.backend.auth.session.Session;
import app.kehdo.backend.auth.session.SessionRepository;
import app.kehdo.backend.auth.token.RefreshTokens;
import app.kehdo.backend.auth.validation.DisposableEmailValidator;
import app.kehdo.backend.common.Ids;
import app.kehdo.backend.user.User;
import app.kehdo.backend.user.UserPlan;
import app.kehdo.backend.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * Orchestrates the four auth flows: signup, login, refresh, logout.
 *
 * <p>Signup and login both create a fresh {@link Session} (a new device/login
 * gets its own row). Refresh rotates the session in place — same id, new
 * refresh-token hash, pushed-out expiry — per {@code SECURITY.md}'s
 * "rotate on every use" rule. Logout flips {@code revokedAt} on the
 * caller's session without deleting it, so we keep audit + replay-detection
 * data.</p>
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final DisposableEmailValidator disposableEmailValidator;
    private final Clock clock;

    public AuthService(
            UserRepository userRepository,
            SessionRepository sessionRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            JwtProperties jwtProperties,
            DisposableEmailValidator disposableEmailValidator,
            Clock clock) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.disposableEmailValidator = disposableEmailValidator;
        this.clock = clock;
    }

    @Transactional
    public AuthResult signup(SignupCommand cmd) {
        // Block disposable email domains before any DB hit; cheaper to reject
        // these early and avoids leaking timing info about whether the email
        // already exists.
        String blockedDomain = disposableEmailValidator.findBlockedDomain(cmd.email());
        if (blockedDomain != null) {
            throw new EmailDomainNotAllowedException(blockedDomain);
        }

        if (userRepository.existsActiveByEmail(cmd.email())) {
            throw new EmailAlreadyRegisteredException();
        }

        Instant now = clock.instant();
        User user = new User(
                Ids.newId(),
                cmd.email(),
                passwordEncoder.encode(cmd.password()),
                cmd.displayName(),
                UserPlan.STARTER,
                now);
        userRepository.save(user);

        return issueSession(user, cmd.userAgent(), cmd.ipAddress(), now);
    }

    @Transactional
    public AuthResult login(LoginCommand cmd) {
        User user = userRepository.findActiveByEmail(cmd.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(cmd.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return issueSession(user, cmd.userAgent(), cmd.ipAddress(), clock.instant());
    }

    @Transactional
    public AuthResult refresh(RefreshCommand cmd) {
        String hash = RefreshTokens.hash(cmd.refreshToken());
        Session session = sessionRepository.findByRefreshTokenHash(hash)
                .orElseThrow(RefreshTokenInvalidException::new);

        Instant now = clock.instant();
        if (!session.isActive(now)) {
            throw new RefreshTokenInvalidException();
        }

        User user = userRepository.findActiveById(session.getUserId())
                .orElseThrow(RefreshTokenInvalidException::new);

        // Rotate: stamp a new refresh-token hash + push expiry out, keep the
        // session id stable so client-side correlation works across rotations.
        String newRawToken = RefreshTokens.generate();
        String newHash = RefreshTokens.hash(newRawToken);
        Instant newExpiresAt = now.plus(jwtProperties.refreshTokenTtl());
        session.rotate(newHash, now, newExpiresAt);

        JwtService.IssuedAccessToken access = jwtService.issueAccess(user.getId(), session.getId());
        return new AuthResult(access.token(), newRawToken, access.expiresAt(), user);
    }

    @Transactional
    public void logout(UUID sessionId) {
        sessionRepository.findById(sessionId).ifPresent(session ->
                session.revoke(clock.instant()));
    }

    private AuthResult issueSession(User user, String userAgent, String ipAddress, Instant now) {
        UUID sessionId = Ids.newId();
        Instant expiresAt = now.plus(jwtProperties.refreshTokenTtl());

        String rawRefresh = RefreshTokens.generate();
        String hashedRefresh = RefreshTokens.hash(rawRefresh);

        Session session = new Session(
                sessionId, user.getId(), hashedRefresh,
                userAgent, ipAddress, expiresAt, now);
        sessionRepository.save(session);

        JwtService.IssuedAccessToken access = jwtService.issueAccess(user.getId(), sessionId);
        return new AuthResult(access.token(), rawRefresh, access.expiresAt(), user);
    }

    public record AuthResult(
            String accessToken,
            String refreshToken,
            Instant accessTokenExpiresAt,
            User user) {}

    public record SignupCommand(
            String email,
            String password,
            String displayName,
            String userAgent,
            String ipAddress) {}

    public record LoginCommand(
            String email,
            String password,
            String userAgent,
            String ipAddress) {}

    public record RefreshCommand(String refreshToken) {}
}
