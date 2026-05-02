package app.kehdo.backend.api.conversation;

import app.kehdo.backend.ai.orchestrator.ContentBlockedException;
import app.kehdo.backend.ai.orchestrator.RefineOrchestrator;
import app.kehdo.backend.api.conversation.dto.ReplyDto;
import app.kehdo.backend.common.error.ApiException;
import app.kehdo.backend.conversation.Conversation;
import app.kehdo.backend.conversation.ConversationRepository;
import app.kehdo.backend.conversation.Reply;
import app.kehdo.backend.conversation.ReplyRepository;
import app.kehdo.backend.user.QuotaExceededException;
import app.kehdo.backend.user.QuotaService;
import app.kehdo.backend.user.User;
import app.kehdo.backend.user.UserPlan;
import app.kehdo.backend.user.UserRepository;
import app.kehdo.backend.user.UserUsage;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReplyServiceTest {

    private static final Instant NOW = Instant.parse("2026-05-03T10:00:00Z");

    private ReplyRepository replyRepository;
    private ConversationRepository conversationRepository;
    private RefineOrchestrator orchestrator;
    private UserRepository userRepository;
    private QuotaService quotaService;
    private ReplyService service;

    @BeforeEach
    void setUp() {
        replyRepository = mock(ReplyRepository.class);
        conversationRepository = mock(ConversationRepository.class);
        orchestrator = mock(RefineOrchestrator.class);
        userRepository = mock(UserRepository.class);
        quotaService = mock(QuotaService.class);
        service = new ReplyService(
                replyRepository,
                conversationRepository,
                orchestrator,
                userRepository,
                quotaService,
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
        when(userRepository.findActiveById(userId)).thenReturn(Optional.of(newUser(userId, UserPlan.PRO)));
        when(orchestrator.refine(any())).thenReturn(
                new RefineOrchestrator.RefineOutput("Got it — 7pm 🙂", "openai/gpt-4o-mini"));

        ReplyDto result = service.refine(userId, replyId, "shorter, add a smiley");

        assertThat(result.text()).isEqualTo("Got it — 7pm 🙂");
        // Tone preserved from the original reply, not re-derived.
        assertThat(result.toneCode()).isEqualTo("WARM");
        assertThat(result.id()).isNotEqualTo(replyId); // new row
        assertThat(result.rank()).isEqualTo(1);
        // Quota was charged.
        verify(quotaService).consumeOrThrow(userId, UserPlan.PRO);
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
    void refine_throws_402_when_quota_exceeded_and_skips_orchestrator() {
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID replyId = UUID.randomUUID();

        Reply original = new Reply(
                replyId, conversationId, 1, "x", "WARM", "model", NOW);
        Conversation conversation = new Conversation(conversationId, userId, NOW);
        when(replyRepository.findById(replyId)).thenReturn(Optional.of(original));
        when(conversationRepository.findActiveByIdAndUser(conversationId, userId))
                .thenReturn(Optional.of(conversation));
        when(userRepository.findActiveById(userId)).thenReturn(Optional.of(newUser(userId, UserPlan.STARTER)));
        doThrow(new QuotaExceededException(new UserUsage(5, 5, NOW.plusSeconds(3600))))
                .when(quotaService).consumeOrThrow(userId, UserPlan.STARTER);

        assertThatThrownBy(() -> service.refine(userId, replyId, "shorter"))
                .isInstanceOf(ApiException.class)
                .extracting("code", "httpStatus")
                .containsExactly("DAILY_QUOTA_EXCEEDED", 402);

        verify(orchestrator, never()).refine(any());
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
        when(userRepository.findActiveById(userId)).thenReturn(Optional.of(newUser(userId, UserPlan.PRO)));
        when(orchestrator.refine(any())).thenThrow(new ContentBlockedException());

        assertThatThrownBy(() -> service.refine(userId, replyId, "shorter"))
                .isInstanceOf(ApiException.class)
                .extracting("code", "httpStatus")
                .containsExactly("CONTENT_BLOCKED", 422);
    }

    private static User newUser(UUID id, UserPlan plan) {
        return new User(id, "u@example.com", "$2a$12$hash", "U", plan, NOW);
    }
}
