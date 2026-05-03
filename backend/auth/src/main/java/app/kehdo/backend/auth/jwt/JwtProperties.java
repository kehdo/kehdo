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
 *     # OR (preferred for hosted environments — inline PEM via secrets):
 *     public-key-pem: ${KEHDO_JWT_PUBLIC_KEY_PEM:}
 *     private-key-pem: ${KEHDO_JWT_PRIVATE_KEY_PEM:}
 * }</pre>
 *
 * <p>Resolution order at startup (in {@link JwtKeys#load}):</p>
 * <ol>
 *   <li>Inline PEM via {@link #publicKeyPem} / {@link #privateKeyPem} —
 *       the path used by Fly.io / AWS / any hosted environment that
 *       injects secrets as env vars. No filesystem touched.</li>
 *   <li>Filesystem / classpath PEM via {@link #publicKeyPath} /
 *       {@link #privateKeyPath} — the local-dev path with PEM files
 *       checked into the repo.</li>
 *   <li>Ephemeral RSA-2048 keypair generated in memory — only safe for
 *       local dev and tests; tokens become invalid on JVM restart.</li>
 * </ol>
 */
@ConfigurationProperties(prefix = "kehdo.jwt")
public record JwtProperties(
        String issuer,
        long accessTokenTtlSeconds,
        long refreshTokenTtlDays,
        String publicKeyPath,
        String privateKeyPath,
        String publicKeyPem,
        String privateKeyPem) {

    public Duration accessTokenTtl() {
        return Duration.ofSeconds(accessTokenTtlSeconds);
    }

    public Duration refreshTokenTtl() {
        return Duration.ofDays(refreshTokenTtlDays);
    }
}
