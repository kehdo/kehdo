package app.kehdo.backend.auth.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Bound from {@code kehdo.jwt.*} in {@code application.yml}.
 *
 * <pre>{@code
 * kehdo:
 *   jwt:
 *     issuer: https://api.kehdo.app
 *     access-token-ttl-seconds: 300
 *     refresh-token-ttl-days: 30
 *     public-key-path: classpath:keys/jwt-public.pem
 *     private-key-path: classpath:keys/jwt-private.pem
 * }</pre>
 *
 * <p>If neither key path resolves to an existing resource at startup, an
 * ephemeral RS256 keypair is generated in memory (suitable for local dev
 * and integration tests; never use in production — tokens become invalid
 * the moment the JVM restarts).</p>
 */
@ConfigurationProperties(prefix = "kehdo.jwt")
public record JwtProperties(
        String issuer,
        long accessTokenTtlSeconds,
        long refreshTokenTtlDays,
        String publicKeyPath,
        String privateKeyPath) {

    public Duration accessTokenTtl() {
        return Duration.ofSeconds(accessTokenTtlSeconds);
    }

    public Duration refreshTokenTtl() {
        return Duration.ofDays(refreshTokenTtlDays);
    }
}
