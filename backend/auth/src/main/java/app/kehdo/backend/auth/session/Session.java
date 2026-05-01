package app.kehdo.backend.auth.session;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * A refresh-token session bound to a single user device/login.
 *
 * <p>The raw refresh token never leaves the client + a single in-memory
 * issuance moment; only its SHA-256 hex hash is stored in
 * {@link #refreshTokenHash}, per {@code SECURITY.md}: "Refresh tokens are
 * hashed in the database and rotate on every use."</p>
 *
 * <p>Sessions are revoked (not deleted) so we can audit usage and detect
 * replay attacks. {@link #revokedAt} non-null = invalid for refresh.</p>
 */
@Entity
@Table(name = "sessions")
public class Session {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "refresh_token_hash", nullable = false, length = 64, unique = true)
    private String refreshTokenHash;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /** Stored as Postgres INET; mapped as String for simplicity. */
    @Column(name = "ip_address", columnDefinition = "inet")
    private String ipAddress;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_used_at", nullable = false)
    private Instant lastUsedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    /** JPA requires a no-args constructor; do not use directly. */
    protected Session() {}

    public Session(
            UUID id,
            UUID userId,
            String refreshTokenHash,
            String userAgent,
            String ipAddress,
            Instant expiresAt,
            Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.refreshTokenHash = refreshTokenHash;
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.lastUsedAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getRefreshTokenHash() { return refreshTokenHash; }
    public String getUserAgent() { return userAgent; }
    public String getIpAddress() { return ipAddress; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getLastUsedAt() { return lastUsedAt; }
    public Instant getRevokedAt() { return revokedAt; }

    public boolean isActive(Instant now) {
        return revokedAt == null && now.isBefore(expiresAt);
    }

    /**
     * Rotate the refresh token: stamp a new hash + push lastUsedAt forward,
     * keep the same session row (so the session id stays stable across
     * refreshes).
     */
    public void rotate(String newRefreshTokenHash, Instant now, Instant newExpiresAt) {
        this.refreshTokenHash = newRefreshTokenHash;
        this.lastUsedAt = now;
        this.expiresAt = newExpiresAt;
    }

    public void revoke(Instant now) {
        this.revokedAt = now;
    }
}
