package app.kehdo.backend.conversation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * A single reply suggestion produced by the LLM for a conversation.
 *
 * <p>Stored in {@code replies} (Flyway {@code V2__init_conversations_replies.sql}).
 * Replies are immutable — refining a reply produces a NEW row with a new
 * id, leaving the original for analytics + the user's "show me what I
 * had" navigation. {@link #toneCode} is a free-form string so adding new
 * tones doesn't require a migration; the canonical list lives in
 * {@code contracts/openapi/kehdo.v1.yaml#/components/schemas/ToneCode}
 * and (Phase 4 PR 5) {@code :api/tones}.</p>
 *
 * <p>Cascade-deleted with the parent conversation by the V2 FK.</p>
 */
@Entity
@Table(name = "replies")
public class Reply {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "conversation_id", nullable = false, updatable = false)
    private UUID conversationId;

    @Column(name = "rank", nullable = false)
    private int rank;

    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "tone_code", nullable = false, length = 32)
    private String toneCode;

    @Column(name = "model_used", nullable = false, length = 64)
    private String modelUsed;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** JPA requires a no-args constructor; do not use directly. */
    protected Reply() {}

    public Reply(
            UUID id,
            UUID conversationId,
            int rank,
            String text,
            String toneCode,
            String modelUsed,
            Instant createdAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.rank = rank;
        this.text = text;
        this.toneCode = toneCode;
        this.modelUsed = modelUsed;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getConversationId() { return conversationId; }
    public int getRank() { return rank; }
    public String getText() { return text; }
    public String getToneCode() { return toneCode; }
    public String getModelUsed() { return modelUsed; }
    public Instant getCreatedAt() { return createdAt; }
}
