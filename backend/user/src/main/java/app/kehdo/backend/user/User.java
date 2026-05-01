package app.kehdo.backend.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * A kehdo user account.
 *
 * <p>Stored in {@code users} (see Flyway {@code V1__init_users_sessions.sql}).
 * IDs are UUIDv7 generated via {@code Ids.newId()} — never DB-side defaults.</p>
 *
 * <p>Soft-deleted by setting {@link #deletedAt}; a nightly job hard-deletes
 * rows older than 30 days. Service-layer queries must filter
 * {@code deletedAt IS NULL} unless explicitly listing trash.</p>
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 72)
    private String passwordHash;

    @Column(name = "display_name", length = 80)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan", nullable = false, length = 16)
    private UserPlan plan = UserPlan.STARTER;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    /** JPA requires a no-args constructor; do not use directly. */
    protected User() {}

    public User(
            UUID id,
            String email,
            String passwordHash,
            String displayName,
            UserPlan plan,
            Instant createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.plan = plan;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getDisplayName() { return displayName; }
    public UserPlan getPlan() { return plan; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getDeletedAt() { return deletedAt; }

    public boolean isActive() {
        return deletedAt == null;
    }

    public void rotatePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
        this.updatedAt = Instant.now();
    }

    public void rename(String displayName) {
        this.displayName = displayName;
        this.updatedAt = Instant.now();
    }

    public void changePlan(UserPlan plan) {
        this.plan = plan;
        this.updatedAt = Instant.now();
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
        this.updatedAt = this.deletedAt;
    }
}
