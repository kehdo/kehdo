package app.kehdo.backend.api.conversation;

import app.kehdo.backend.ai.orchestrator.ContentBlockedException;
import app.kehdo.backend.ai.orchestrator.RefineOrchestrator;
import app.kehdo.backend.api.conversation.dto.ReplyDto;
import app.kehdo.backend.common.error.ApiException;
import app.kehdo.backend.conversation.Conversation;
import app.kehdo.backend.conversation.ConversationRepository;
import app.kehdo.backend.conversation.Reply;
import app.kehdo.backend.conversation.ReplyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReplyServiceTest {

    private static final Instant NOW = Instant.parse("2026-05-03T10:00:00Z");

    private ReplyRepository replyRepository;
    private ConversationRepository conversationRepository;
    private RefineOrchestrator orchestrator;
    private ReplyService service;

    @BeforeEach
    void setUp() {
        replyRepository = mock(ReplyRepository.class);
        conversationRepository = mock(ConversationRepository.class);
        orchestrator = mock(RefineOrchestrator.class);
        service = new ReplyService(
                replyRepository,
                conversationRepository,
                orchestrator,
                Clock.fixed(NOW, ZoneOffset.UTC));
        when(replyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void refine_persists_new_reply_with_refined_text_and_propagates_tone_and_model() {
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID replyId = UUID.randomUUID();

        Reply original = new Reply(
                replyId, conversationId, 1, "Sure! Tonight at 7?", "WARM",
                "vertex-ai/gemini-2.0-flash", Instant.parse("2026-05-03T09:00:00Z"));
        Conversation conversation = new Conversation(conversationId, userId, NOW);
        when(replyRepository.findById(replyId)).thenReturn(Optional.of(original));
        when(conversationRepository.findActiveByIdAndUser(conversationId, userId))
                .thenReturn(Optional.of(conversation));
        when(orchestrator.refine(any())).thenReturn(
                new RefineOrchestrator.RefineOutput("Got it — 7pm 🙂", "openai/gpt-4o-mini"));

        ReplyDto result = service.refine(userId, replyId, "shorter, add a smiley");

        assertThat(result.text()).isEqualTo("Got it — 7pm 🙂");
        // Tone preserved from the original reply, not re-derived.
        assertThat(result.toneCode()).isEqualTo("WARM");
        assertThat(result.id()).isNotEqualTo(replyId); // new row
        assertThat(result.rank()).isEqualTo(1);
    }

    @Test
    void refine_throws_not_found_when_reply_id_does_not_exist() {
        UUID userId = UUID.randomUUID();
        UUID replyId = UUID.randomUUID();
        when(replyRepository.findById(replyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.refine(userId, replyId, "shorter"))
                .isInstanceOf(ApiException.class)
                .extracting("code", "httpStatus")
                .containsExactly("NOT_FOUND", 404);
    }

    @Test
    void refine_throws_not_found_when_caller_does_not_own_the_conversation() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID replyId = UUID.randomUUID();

        Reply original = new Reply(
                replyId, conversationId, 1, "x", "WARM", "model", NOW);
        when(replyRepository.findById(replyId)).thenReturn(Optional.of(original));
        // The other user owns the conversation; our caller doesn't.
        when(conversationRepository.findActiveByIdAndUser(conversationId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.refine(userId, replyId, "shorter"))
                .isInstanceOf(ApiException.class)
                .extracting("code", "httpStatus")
                .containsExactly("NOT_FOUND", 404);
    }

    @Test
    void refine_throws_content_blocked_when_orchestrator_blocks() {
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID replyId = UUID.randomUUID();

        Reply original = new Reply(
                replyId, conversationId, 1, "x", "WARM", "model", NOW);
        Conversation conversation = new Conversation(conversationId, userId, NOW);
        when(replyRepository.findById(replyId)).thenReturn(Optional.of(original));
        when(conversationRepository.findActiveByIdAndUser(conversationId, userId))
                .thenReturn(Optional.of(conversation));
        when(orchestrator.refine(any())).thenThrow(new ContentBlockedException());

        assertThatThrownBy(() -> service.refine(userId, replyId, "shorter"))
                .isInstanceOf(ApiException.class)
                .extracting("code", "httpStatus")
                .containsExactly("CONTENT_BLOCKED", 422);
    }
}
