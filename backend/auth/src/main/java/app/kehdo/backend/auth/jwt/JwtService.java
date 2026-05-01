package app.kehdo.backend.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Issues and validates short-lived RS256 access tokens.
 *
 * <p>Claims:</p>
 * <ul>
 *   <li>{@code sub} — user UUID</li>
 *   <li>{@code sid} — session UUID (so we can revoke a single device without
 *       invalidating the whole user)</li>
 *   <li>{@code iss} — {@link JwtProperties#issuer()}</li>
 *   <li>{@code iat} — issue time</li>
 *   <li>{@code exp} — issue time + {@link JwtProperties#accessTokenTtl()}</li>
 * </ul>
 */
public class JwtService {

    private final JwtProperties props;
    private final JwtKeys keys;
    private final Clock clock;

    public JwtService(JwtProperties props, JwtKeys keys, Clock clock) {
        this.props = props;
        this.keys = keys;
        this.clock = clock;
    }

    public IssuedAccessToken issueAccess(UUID userId, UUID sessionId) {
        Instant now = clock.instant();
        Instant exp = now.plus(props.accessTokenTtl());

        String token = Jwts.builder()
                .subject(userId.toString())
                .claim("sid", sessionId.toString())
                .issuer(props.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(keys.privateKey(), Jwts.SIG.RS256)
                .compact();

        return new IssuedAccessToken(token, now, exp);
    }

    /**
     * Validates the signature, issuer, and expiration. Throws
     * {@link JwtException} on any failure — callers should catch and map
     * to {@code UNAUTHORIZED}.
     */
    public ValidatedAccessToken validateAccess(String token) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(keys.publicKey())
                .requireIssuer(props.issuer())
                .build()
                .parseSignedClaims(token);

        Claims claims = jws.getPayload();
        UUID userId = UUID.fromString(claims.getSubject());
        UUID sessionId = UUID.fromString(claims.get("sid", String.class));

        return new ValidatedAccessToken(
                userId,
                sessionId,
                claims.getIssuedAt().toInstant(),
                claims.getExpiration().toInstant());
    }

    public record IssuedAccessToken(String token, Instant issuedAt, Instant expiresAt) {}

    public record ValidatedAccessToken(
            UUID userId,
            UUID sessionId,
            Instant issuedAt,
            Instant expiresAt) {}
}
