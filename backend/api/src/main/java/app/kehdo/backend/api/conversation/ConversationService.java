package app.kehdo.backend.api.conversation;

import app.kehdo.backend.ai.llm.LlmReply;
import app.kehdo.backend.ai.orchestrator.ContentBlockedException;
import app.kehdo.backend.ai.orchestrator.GenerationOrchestrator;
import app.kehdo.backend.ai.orchestrator.GenerationOutput;
import app.kehdo.backend.ai.orchestrator.GenerationRequest;
import app.kehdo.backend.api.conversation.dto.ConversationDto;
import app.kehdo.backend.api.conversation.dto.ConversationPageDto;
import app.kehdo.backend.api.conversation.dto.CreateConversationResponse;
import app.kehdo.backend.api.conversation.dto.GenerateResponse;
import app.kehdo.backend.api.conversation.dto.ReplyDto;
import app.kehdo.backend.common.Ids;
import app.kehdo.backend.common.error.ApiException;
import app.kehdo.backend.common.error.ErrorCode;
import app.kehdo.backend.conversation.Conversation;
import app.kehdo.backend.conversation.ConversationRepository;
import app.kehdo.backend.conversation.ConversationStatus;
import app.kehdo.backend.conversation.ParsedMessage;
import app.kehdo.backend.conversation.Reply;
import app.kehdo.backend.conversation.ReplyRepository;
import app.kehdo.backend.infra.storage.PresignedUpload;
import app.kehdo.backend.infra.storage.ScreenshotStorage;
import app.kehdo.backend.user.QuotaExceededException;
import app.kehdo.backend.user.QuotaService;
import app.kehdo.backend.user.User;
import app.kehdo.backend.user.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Orchestrates the {@code /conversations/*} use cases. Sits between the
 * controller (HTTP shape) and the persistence + AI layers.
 *
 * <p>Phase 4 PR 4: real persistence + stub-LLM generation. PR 6+ swaps
 * the OCR/LLM/moderation stubs behind {@link GenerationOrchestrator}
 * for cloud adapters; this service doesn't change.</p>
 */
@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ReplyRepository replyRepository;
    private final ScreenshotStorage screenshotStorage;
    private final GenerationOrchestrator generationOrchestrator;
    private final UserRepository userRepository;
    private final QuotaService quotaService;
    private final Clock clock;

    public ConversationService(
            ConversationRepository conversationRepository,
            ReplyRepository replyRepository,
            ScreenshotStorage screenshotStorage,
            GenerationOrchestrator generationOrchestrator,
            UserRepository userRepository,
            QuotaService quotaService,
            Clock clock) {
        this.conversationRepository = conversationRepository;
        this.replyRepository = replyRepository;
        this.screenshotStorage = screenshotStorage;
        this.generationOrchestrator = generationOrchestrator;
        this.userRepository = userRepository;
        this.quotaService = quotaService;
        this.clock = clock;
    }

    @Transactional
    public CreateConversationResponse create(UUID userId) {
        UUID id = Ids.newId();
        Conversation conversation = new Conversation(id, userId, clock.instant());
        PresignedUpload upload = screenshotStorage.presignUpload(id);
        conversation.assignUploadKey(upload.objectKey());
        Conversation saved = conversationRepository.save(conversation);
        return new CreateConversationResponse(saved.getId(), upload.uploadUrl(), upload.uploadExpiresAt());
    }

    @Transactional(readOnly = true)
    public ConversationPageDto list(UUID userId, int limit, String cursor) {
        Cursor decoded = decodeCursor(cursor);
        // Fetch limit+1 so we can detect whether more pages exist without
        // a separate count query. The +1th row is dropped from the
        // response and used as the next cursor.
        PageRequest page = PageRequest.of(0, limit + 1);
        List<Conversation> rows = decoded == null
                ? conversationRepository.findActivePageByUser(userId, page)
                : conversationRepository.findActivePageByUserAfterCursor(
                        userId, decoded.createdAt(), decoded.id(), page);

        boolean hasMore = rows.size() > limit;
        List<Conversation> trimmed = hasMore ? rows.subList(0, limit) : rows;
        String nextCursor = hasMore
                ? encodeCursor(trimmed.get(trimmed.size() - 1))
                : null;

        List<ConversationDto> items = trimmed.stream().map(ConversationDto::from).toList();
        return new ConversationPageDto(items, nextCursor);
    }

    /**
     * Cursor wire format: URL-safe base64 of "createdAtIso|id". Opaque to
     * the client — never parse it on the Android / iOS side. Stable as
     * long as the server keeps the same composite-key sort, which means
     * (createdAt DESC, id DESC) is part of the API contract now.
     */
    private record Cursor(Instant createdAt, UUID id) {}

    private static Cursor decodeCursor(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(raw);
            String[] parts = new String(decoded, StandardCharsets.UTF_8).split("\\|", 2);
            if (parts.length != 2) {
                throw new ApiException(
                        ErrorCode.BAD_REQUEST,
                        HttpStatus.BAD_REQUEST.value(),
                        "Invalid pagination cursor.");
            }
            return new Cursor(Instant.parse(parts[0]), UUID.fromString(parts[1]));
        } catch (IllegalArgumentException | DateTimeParseException malformed) {
            throw new ApiException(
                    ErrorCode.BAD_REQUEST,
                    HttpStatus.BAD_REQUEST.value(),
                    "Invalid pagination cursor.");
        }
    }

    private static String encodeCursor(Conversation last) {
        String raw = last.getCreatedAt().toString() + "|" + last.getId();
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    @Transactional(readOnly = true)
    public ConversationDto get(UUID userId, UUID conversationId) {
        return ConversationDto.from(loadOwned(userId, conversationId));
    }

    @Transactional
    public void delete(UUID userId, UUID conversationId) {
        Conversation conversation = loadOwned(userId, conversationId);
        conversation.softDelete();
        conversationRepository.save(conversation);
    }

    @Transactional
    public GenerateResponse generate(UUID userId, UUID conversationId, String tone, int count) {
        Conversation conversation = loadOwned(userId, conversationId);
        if (conversation.getScreenshotObjectKey() == null) {
            throw new ApiException(
                    "CONVERSATION_NOT_READY",
                    HttpStatus.CONFLICT.value(),
                    "Upload the screenshot before requesting replies.");
        }

        // Daily quota check (CLAUDE.md security rule "5/day free, 100/day paid").
        // Throws QuotaExceededException → mapped to 402 DAILY_QUOTA_EXCEEDED below.
        // Done before status transitions so a quota-blocked call doesn't leave
        // the conversation in PROCESSING with no generation behind it.
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.UNAUTHORIZED,
                        HttpStatus.UNAUTHORIZED.value(),
                        "Authentication required."));
        try {
            quotaService.consumeOrThrow(userId, user.getPlan());
        } catch (QuotaExceededException over) {
            throw new ApiException(
                    "DAILY_QUOTA_EXCEEDED",
                    HttpStatus.PAYMENT_REQUIRED.value(),
                    "Daily reply quota exceeded; upgrade to continue.");
        }

        if (conversation.getStatus() == ConversationStatus.PENDING_UPLOAD) {
            // First call after upload — flip the status before running.
            // Real upload-complete callback from the storage adapter is a
            // future enhancement.
            conversation.markProcessing();
        }

        GenerationOutput output;
        try {
            output = generationOrchestrator.run(new GenerationRequest(
                    conversation.getScreenshotObjectKey(),
                    tone,
                    count));
        } catch (ContentBlockedException blocked) {
            conversation.markFailed("CONTENT_BLOCKED");
            conversationRepository.save(conversation);
            throw new ApiException(
                    "CONTENT_BLOCKED",
                    HttpStatus.UNPROCESSABLE_ENTITY.value(),
                    "Generated replies were blocked by the moderation policy.");
        }

        Instant now = clock.instant();

        // Persist OCR result onto the conversation so subsequent generate
        // calls (different tone) can reuse it once Phase 4 PR 12 wires the
        // cache check on the orchestrator.
        List<ParsedMessage> messages = output.attributedMessages().stream()
                .map(line -> new ParsedMessage(
                        ParsedMessage.Speaker.valueOf(line.speaker().name()),
                        line.text(),
                        line.confidence()))
                .toList();
        conversation.onOcrCompleted(messages, now);
        conversation.onGenerationCompleted(output.modelUsed(), now);
        conversationRepository.save(conversation);

        List<Reply> persisted = output.replies().stream()
                .map(r -> persistReply(r, conversationId, tone, output.modelUsed(), now))
                .toList();

        List<ReplyDto> replyDtos = persisted.stream().map(ReplyDto::from).toList();
        return new GenerateResponse(conversationId, tone, replyDtos, now, output.modelUsed());
    }

    private Reply persistReply(LlmReply r, UUID conversationId, String tone, String modelUsed, Instant now) {
        Reply reply = new Reply(
                Ids.newId(),
                conversationId,
                r.rank(),
                r.text(),
                tone,
                modelUsed,
                now);
        return replyRepository.save(reply);
    }

    private Conversation loadOwned(UUID userId, UUID conversationId) {
        return conversationRepository.findActiveByIdAndUser(conversationId, userId)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.NOT_FOUND,
                        HttpStatus.NOT_FOUND.value(),
                        "Conversation not found."));
    }
}
