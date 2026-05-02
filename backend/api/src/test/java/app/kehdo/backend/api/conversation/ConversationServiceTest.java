package app.kehdo.backend.api.conversation;

import app.kehdo.backend.ai.llm.LlmReply;
import app.kehdo.backend.ai.orchestrator.ContentBlockedException;
import app.kehdo.backend.ai.orchestrator.GenerationOrchestrator;
import app.kehdo.backend.ai.orchestrator.GenerationOutput;
import app.kehdo.backend.ai.orchestrator.GenerationRequest;
import app.kehdo.backend.ai.speaker.SpeakerAttributor.AttributedLine;
import app.kehdo.backend.ai.speaker.SpeakerAttributor.AttributedLine.Speaker;
import app.kehdo.backend.api.conversation.dto.ConversationDto;
import app.kehdo.backend.api.conversation.dto.ConversationPageDto;
import app.kehdo.backend.api.conversation.dto.CreateConversationResponse;
import app.kehdo.backend.api.conversation.dto.GenerateResponse;
import app.kehdo.backend.common.error.ApiException;
import app.kehdo.backend.conversation.Conversation;
import app.kehdo.backend.conversation.ConversationRepository;
import app.kehdo.backend.conversation.ConversationStatus;
import app.kehdo.backend.conversation.Reply;
import app.kehdo.backend.conversation.ReplyRepository;
import app.kehdo.backend.infra.storage.PresignedUpload;
import app.kehdo.backend.infra.storage.ScreenshotStorage;
import app.kehdo.backend.user.QuotaExceededException;
import app.kehdo.backend.user.QuotaService;
import app.kehdo.backend.user.User;
import app.kehdo.backend.user.UserPlan;
import app.kehdo.backend.user.UserRepository;
import app.kehdo.backend.user.UserUsage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConversationServiceTest {

    private static final Instant NOW = Instant.parse("2026-05-02T18:30:00Z");

    private ConversationRepository conversationRepository;
    private ReplyRepository replyRepository;
    private ScreenshotStorage storage;
    private GenerationOrchestrator orchestrator;
    private UserRepository userRepository;
    private QuotaService quotaService;
    private ConversationService service;

    @BeforeEach
    void setUp() {
        conversationRepository = mock(ConversationRepository.class);
        replyRepository = mock(ReplyRepository.class);
        storage = mock(ScreenshotStorage.class);
        orchestrator = mock(GenerationOrchestrator.class);
        userRepository = mock(UserRepository.class);
        quotaService = mock(QuotaService.class);
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        service = new ConversationService(
                conversationRepository,
                replyRepository,
                storage,
                orchestrator,
                userRepository,
                quotaService,
                clock);
        when(conversationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(replyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void create_persists_conversation_in_pending_upload_with_assigned_object_key() {
        UUID userId = UUID.randomUUID();
        when(storage.presignUpload(any())).thenAnswer(inv -> new PresignedUpload(
                "conversations/" + inv.getArgument(0) + "/screenshot.png",
                "https://uploads.kehdo.invalid/x?signature=stub",
                NOW.plusSeconds(300)));

        CreateConversationResponse resp = service.create(userId);

        assertThat(resp.conversationId()).isNotNull();
        assertThat(resp.uploadUrl()).contains("uploads.kehdo.invalid");
        assertThat(resp.uploadExpiresAt()).isEqualTo(NOW.plusSeconds(300));
    }

    @Test
    void list_returns_first_page_with_no_cursor_when_results_fit_in_one_page() {
        UUID userId = UUID.randomUUID();
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        when(conversationRepository.findActivePageByUser(eq(userId), any(Pageable.class)))
                .thenReturn(List.of(
                        conv(a, userId, NOW),
                        conv(b, userId, NOW.minusSeconds(60))));

        ConversationPageDto page = service.list(userId, 20, null);

        assertThat(page.items()).hasSize(2);
        assertThat(page.items()).extracting(ConversationDto::id).containsExactly(a, b);
        // No more rows than asked → no nextCursor.
        assertThat(page.nextCursor()).isNull();
    }

    @Test
    void list_emits_next_cursor_when_more_rows_exist() {
        UUID userId = UUID.randomUUID();
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();
        // Service asks for limit+1 (3) to detect "more pages"; we return 3.
        when(conversationRepository.findActivePageByUser(eq(userId), any(Pageable.class)))
                .thenReturn(List.of(
                        conv(a, userId, NOW),
                        conv(b, userId, NOW.minusSeconds(60)),
                        conv(c, userId, NOW.minusSeconds(120))));

        ConversationPageDto page = service.list(userId, 2, null);

        // Trim to the requested limit.
        assertThat(page.items()).hasSize(2);
        assertThat(page.items()).extracting(ConversationDto::id).containsExactly(a, b);
        // Cursor anchors on the last returned row (b), not the look-ahead row (c).
        assertThat(page.nextCursor()).isNotNull();
        String decoded = new String(
                Base64.getUrlDecoder().decode(page.nextCursor()),
                StandardCharsets.UTF_8);
        assertThat(decoded).isEqualTo(NOW.minusSeconds(60) + "|" + b);
    }

    @Test
    void list_with_valid_cursor_calls_keyset_query_and_skips_first_page() {
        UUID userId = UUID.randomUUID();
        UUID seen = UUID.randomUUID();
        UUID nextRow = UUID.randomUUID();
        Instant cursorTs = NOW.minusSeconds(60);
        String cursor = Base64.getUrlEncoder().withoutPadding().encodeToString(
                (cursorTs + "|" + seen).getBytes(StandardCharsets.UTF_8));
        when(conversationRepository.findActivePageByUserAfterCursor(
                eq(userId), eq(cursorTs), eq(seen), any(Pageable.class)))
                .thenReturn(List.of(conv(nextRow, userId, NOW.minusSeconds(120))));

        ConversationPageDto page = service.list(userId, 20, cursor);

        assertThat(page.items()).hasSize(1);
        assertThat(page.items().get(0).id()).isEqualTo(nextRow);
        assertThat(page.nextCursor()).isNull();
        // The non-cursor query should NOT have been used.
        verify(conversationRepository, never())
                .findActivePageByUser(any(UUID.class), any(Pageable.class));
    }

    @Test
    void list_throws_400_for_malformed_cursor() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> service.list(userId, 20, "not-base64!@#"))
                .isInstanceOf(ApiException.class)
                .extracting("code", "httpStatus")
                .containsExactly("BAD_REQUEST", 400);
    }

    @Test
    void list_throws_400_when_cursor_decodes_but_payload_is_garbage() {
        UUID userId = UUID.randomUUID();
        String garbageCursor = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("not-iso|not-uuid".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> service.list(userId, 20, garbageCursor))
                .isInstanceOf(ApiException.class)
                .extracting("code", "httpStatus")
                .containsExactly("BAD_REQUEST", 400);
    }

    @Test
    void get_returns_owned_conversation() {
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        Conversation owned = new Conversation(conversationId, userId, NOW);
        when(conversationRepository.findActiveByIdAndUser(conversationId, userId))
                .thenReturn(Optional.of(owned));

        ConversationDto dto = service.get(userId, conversationId);

        assertThat(dto.id()).isEqualTo(conversationId);
        assertThat(dto.status()).isEqualTo(ConversationStatus.PENDING_UPLOAD.name());
    }

    @Test
    void get_throws_not_found_when_not_owned_or_missing() {
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        when(conversationRepository.findActiveByIdAndUser(conversationId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(userId, conversationId))
                .isInstanceOf(ApiException.class)
                .extracting("code", "httpStatus")
                .containsExactly("NOT_FOUND", 404);
    }

    @Test
    void delete_soft_deletes_owned_conversation() {
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        Conversation owned = new Conversation(conversationId, userId, NOW);
        when(conversationRepository.findActiveByIdAndUser(conversationId, userId))
                .thenReturn(Optional.of(owned));

        service.delete(userId, conversationId);

        assertThat(owned.isActive()).isFalse();
    }

    @Test
    void generate_throws_conversation_not_ready_when_no_object_key() {
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        Conversation owned = new Conversation(conversationId, userId, NOW);
        // no assignUploadKey() — screenshotObjectKey stays null
        when(conversationRepository.findActiveByIdAndUser(conversationId, userId))
                .thenReturn(Optional.of(owned));

        assertThatThrownBy(() -> service.generate(userId, conversationId, "WARM", 2))
                .isInstanceOf(ApiException.class)
                .extracting("code", "httpStatus")
                .containsExactly("CONVERSATION_NOT_READY", 409);
    }

    @Test
    void generate_throws_402_when_quota_exceeded_and_skips_orchestrator() {
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        Conversation owned = new Conversation(conversationId, userId, NOW);
        owned.assignUploadKey("conversations/x/screenshot.png");
        when(conversationRepository.findActiveByIdAndUser(conversationId, userId))
                .thenReturn(Optional.of(owned));
        User user = newUser(userId, UserPlan.STARTER);
        when(userRepository.findActiveById(userId)).thenReturn(Optional.of(user));
        doThrow(new QuotaExceededException(new UserUsage(5, 5, NOW.plusSeconds(3600))))
                .when(quotaService).consumeOrThrow(userId, UserPlan.STARTER);

        assertThatThrownBy(() -> service.generate(userId, conversationId, "WARM", 2))
                .isInstanceOf(ApiException.class)
                .extracting("code", "httpStatus")
                .containsExactly("DAILY_QUOTA_EXCEEDED", 402);

        // Orchestrator should not have been touched — the LLM call was prevented.
        verify(orchestrator, never()).run(any());
        // Conversation should not have transitioned away from PENDING_UPLOAD either.
        assertThat(owned.getStatus()).isEqualTo(ConversationStatus.PENDING_UPLOAD);
    }

    @Test
    void generate_runs_pipeline_persists_replies_and_marks_ready() {
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        Conversation owned = new Conversation(conversationId, userId, NOW);
        owned.assignUploadKey("conversations/x/screenshot.png");
        when(conversationRepository.findActiveByIdAndUser(conversationId, userId))
                .thenReturn(Optional.of(owned));
        when(userRepository.findActiveById(userId)).thenReturn(Optional.of(newUser(userId, UserPlan.PRO)));
        when(orchestrator.run(any(GenerationRequest.class))).thenReturn(new GenerationOutput(
                List.of(new AttributedLine(Speaker.THEM, "hi", 0.9)),
                List.of(new LlmReply(1, "hey there"), new LlmReply(2, "yo")),
                "stub/canned-v1"));

        GenerateResponse resp = service.generate(userId, conversationId, "WARM", 2);

        assertThat(resp.tone()).isEqualTo("WARM");
        assertThat(resp.modelUsed()).isEqualTo("stub/canned-v1");
        assertThat(resp.replies()).hasSize(2);
        assertThat(resp.replies()).extracting("text").containsExactly("hey there", "yo");
        assertThat(owned.getStatus()).isEqualTo(ConversationStatus.READY);
        assertThat(owned.getLastGenerationModel()).isEqualTo("stub/canned-v1");
        // Quota was consumed exactly once.
        verify(quotaService).consumeOrThrow(userId, UserPlan.PRO);
    }

    @Test
    void generate_marks_failed_and_throws_content_blocked_when_all_replies_blocked() {
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        Conversation owned = new Conversation(conversationId, userId, NOW);
        owned.assignUploadKey("conversations/x/screenshot.png");
        when(conversationRepository.findActiveByIdAndUser(conversationId, userId))
                .thenReturn(Optional.of(owned));
        when(userRepository.findActiveById(userId)).thenReturn(Optional.of(newUser(userId, UserPlan.STARTER)));
        when(orchestrator.run(any(GenerationRequest.class)))
                .thenThrow(new ContentBlockedException());

        assertThatThrownBy(() -> service.generate(userId, conversationId, "WARM", 2))
                .isInstanceOf(ApiException.class)
                .extracting("code", "httpStatus")
                .containsExactly("CONTENT_BLOCKED", 422);
        assertThat(owned.getStatus()).isEqualTo(ConversationStatus.FAILED);
    }

    private static User newUser(UUID id, UserPlan plan) {
        return new User(id, "u@example.com", "$2a$12$hash", "U", plan, NOW);
    }

    private static Conversation conv(UUID id, UUID userId, Instant createdAt) {
        return new Conversation(id, userId, createdAt);
    }
}
