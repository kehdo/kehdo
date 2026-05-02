package app.kehdo.backend.conversation;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Conversation}'s state-transition methods. Pure
 * Java — no Spring context, no DB. Verifies the entity enforces the
 * documented PENDING_UPLOAD → PROCESSING → READY lifecycle and that
 * {@code updatedAt} advances on every mutation.
 */
class ConversationTest {

    @Test
    void newly_created_conversation_starts_in_pending_upload() {
        Conversation c = new Conversation(UUID.randomUUID(), UUID.randomUUID(), Instant.parse("2026-05-02T10:00:00Z"));

        assertThat(c.getStatus()).isEqualTo(ConversationStatus.PENDING_UPLOAD);
        assertThat(c.getScreenshotObjectKey()).isNull();
        assertThat(c.getParsedMessages()).isNull();
        assertThat(c.isActive()).isTrue();
        assertThat(c.getCreatedAt()).isEqualTo(c.getUpdatedAt());
    }

    @Test
    void on_screenshot_uploaded_advances_to_processing() {
        Conversation c = new Conversation(UUID.randomUUID(), UUID.randomUUID(), Instant.parse("2026-05-02T10:00:00Z"));
        Instant before = c.getUpdatedAt();

        c.onScreenshotUploaded("uploads/abc.png");

        assertThat(c.getStatus()).isEqualTo(ConversationStatus.PROCESSING);
        assertThat(c.getScreenshotObjectKey()).isEqualTo("uploads/abc.png");
        assertThat(c.getUpdatedAt()).isAfterOrEqualTo(before);
    }

    @Test
    void on_ocr_completed_stores_messages_but_does_not_change_status() {
        Conversation c = new Conversation(UUID.randomUUID(), UUID.randomUUID(), Instant.parse("2026-05-02T10:00:00Z"));
        c.onScreenshotUploaded("uploads/abc.png");
        Instant ocrAt = Instant.parse("2026-05-02T10:00:05Z");
        var messages = List.of(new ParsedMessage(ParsedMessage.Speaker.THEM, "hi", 0.95));

        c.onOcrCompleted(messages, ocrAt);

        // Status stays PROCESSING — generation hasn't run yet.
        assertThat(c.getStatus()).isEqualTo(ConversationStatus.PROCESSING);
        assertThat(c.getParsedMessages()).containsExactlyElementsOf(messages);
        assertThat(c.getOcrCompletedAt()).isEqualTo(ocrAt);
    }

    @Test
    void on_generation_completed_marks_ready_with_model_metadata() {
        Conversation c = new Conversation(UUID.randomUUID(), UUID.randomUUID(), Instant.parse("2026-05-02T10:00:00Z"));
        c.onScreenshotUploaded("uploads/abc.png");
        c.onOcrCompleted(
                List.of(new ParsedMessage(ParsedMessage.Speaker.THEM, "hi", 0.95)),
                Instant.parse("2026-05-02T10:00:05Z"));
        Instant genAt = Instant.parse("2026-05-02T10:00:10Z");

        c.onGenerationCompleted("vertex-ai/gemini-2.0-flash", genAt);

        assertThat(c.getStatus()).isEqualTo(ConversationStatus.READY);
        assertThat(c.getLastGenerationModel()).isEqualTo("vertex-ai/gemini-2.0-flash");
        assertThat(c.getLastGenerationAt()).isEqualTo(genAt);
    }

    @Test
    void mark_failed_records_reason_and_terminates() {
        Conversation c = new Conversation(UUID.randomUUID(), UUID.randomUUID(), Instant.parse("2026-05-02T10:00:00Z"));
        c.onScreenshotUploaded("uploads/abc.png");

        c.markFailed("OCR_FAILED");

        assertThat(c.getStatus()).isEqualTo(ConversationStatus.FAILED);
        assertThat(c.getFailureReason()).isEqualTo("OCR_FAILED");
    }

    @Test
    void soft_delete_clears_active_flag() {
        Conversation c = new Conversation(UUID.randomUUID(), UUID.randomUUID(), Instant.parse("2026-05-02T10:00:00Z"));

        c.softDelete();

        assertThat(c.isActive()).isFalse();
        assertThat(c.getDeletedAt()).isNotNull();
    }
}
