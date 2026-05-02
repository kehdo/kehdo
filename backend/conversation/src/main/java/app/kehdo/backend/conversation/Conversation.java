package app.kehdo.backend.conversation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * One uploaded screenshot, tracked through the OCR + LLM pipeline.
 *
 * <p>Stored in {@code conversations} (Flyway {@code V2__init_conversations_replies.sql}).
 * IDs are UUIDv7 generated via {@code Ids.newId()} — never DB-side defaults.
 * Soft-deleted by setting {@link #deletedAt}; hard-deleted by a nightly
 * job after 30 days.</p>
 *
 * <p>{@link #parsedMessages} is persisted as JSONB rather than a child
 * table so the OCR pipeline can write the whole array atomically and
 * reads don't need a join. We don't query inside it.</p>
 *
 * <p>Per {@code backend/CLAUDE.md}'s dependency rules, this module never
 * imports {@code :user}; the link to a user is a raw {@link #userId} UUID.
 * The DB-level FK is enforced by V2 migration.</p>
 */
@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private ConversationStatus status = ConversationStatus.PENDING_UPLOAD;

    @Column(name = "screenshot_object_key", length = 255)
    private String screenshotObjectKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "parsed_messages", columnDefinition = "jsonb")
    private List<ParsedMessage> parsedMessages;

    @Column(name = "ocr_completed_at")
    private Instant ocrCompletedAt;

    @Column(name = "last_generation_model", length = 64)
    private String lastGenerationModel;

    @Column(name = "last_generation_at")
    private Instant lastGenerationAt;

    @Column(name = "failure_reason", length = 64)
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    /** JPA requires a no-args constructor; do not use directly. */
    protected Conversation() {}

    public Conversation(UUID id, UUID userId, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    // ---- state transitions ---------------------------------------------------

    /**
     * Marks the screenshot as uploaded and transitions to {@link
     * ConversationStatus#PROCESSING}. Caller is the controller after
     * confirming the object exists in storage (or just trusting the
     * client's signal — TBD by Phase 4 PR 12).
     */
    public void onScreenshotUploaded(String objectKey) {
        this.screenshotObjectKey = objectKey;
        this.status = ConversationStatus.PROCESSING;
        touch();
    }

    public void onOcrCompleted(List<ParsedMessage> messages, Instant at) {
        this.parsedMessages = messages;
        this.ocrCompletedAt = at;
        touch();
    }

    public void onGenerationCompleted(String modelUsed, Instant at) {
        this.status = ConversationStatus.READY;
        this.lastGenerationModel = modelUsed;
        this.lastGenerationAt = at;
        touch();
    }

    public void markFailed(String reason) {
        this.status = ConversationStatus.FAILED;
        this.failureReason = reason;
        touch();
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
        this.updatedAt = this.deletedAt;
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public boolean isActive() {
        return deletedAt == null;
    }

    // ---- accessors -----------------------------------------------------------

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public ConversationStatus getStatus() { return status; }
    public String getScreenshotObjectKey() { return screenshotObjectKey; }
    public List<ParsedMessage> getParsedMessages() { return parsedMessages; }
    public Instant getOcrCompletedAt() { return ocrCompletedAt; }
    public String getLastGenerationModel() { return lastGenerationModel; }
    public Instant getLastGenerationAt() { return lastGenerationAt; }
    public String getFailureReason() { return failureReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getDeletedAt() { return deletedAt; }
}
