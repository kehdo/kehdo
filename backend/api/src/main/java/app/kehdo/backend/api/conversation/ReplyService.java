package app.kehdo.backend.api.conversation;

import app.kehdo.backend.ai.orchestrator.ContentBlockedException;
import app.kehdo.backend.ai.orchestrator.RefineOrchestrator;
import app.kehdo.backend.api.conversation.dto.ReplyDto;
import app.kehdo.backend.common.Ids;
import app.kehdo.backend.common.error.ApiException;
import app.kehdo.backend.common.error.ErrorCode;
import app.kehdo.backend.conversation.Conversation;
import app.kehdo.backend.conversation.ConversationRepository;
import app.kehdo.backend.conversation.Reply;
import app.kehdo.backend.conversation.ReplyRepository;
import app.kehdo.backend.user.QuotaExceededException;
import app.kehdo.backend.user.QuotaService;
import app.kehdo.backend.user.User;
import app.kehdo.backend.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

/**
 * Reply-scoped use cases. Currently just refine; future PRs may add
 * "select" (interaction-signal data flywheel per AI rule 11) and
 * "delete" (manual reply removal).
 *
 * <p>Refine is intentionally split out of {@link ConversationService}
 * because it acts on a reply by id without needing the conversation
 * pipeline (no OCR, no speaker attribution). Same security model and
 * same daily quota though — refine counts against the same limit as
 * generate per the contract.</p>
 */
@Service
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final ConversationRepository conversationRepository;
    private final RefineOrchestrator refineOrchestrator;
    private final UserRepository userRepository;
    private final QuotaService quotaService;
    private final Clock clock;

    public ReplyService(
            ReplyRepository replyRepository,
            ConversationRepository conversationRepository,
            RefineOrchestrator refineOrchestrator,
            UserRepository userRepository,
            QuotaService quotaService,
            Clock clock) {
        this.replyRepository = replyRepository;
        this.conversationRepository = conversationRepository;
        this.refineOrchestrator = refineOrchestrator;
        this.userRepository = userRepository;
        this.quotaService = quotaService;
        this.clock = clock;
    }

    /**
     * Reworks the reply per the user's instructions and persists a NEW
     * reply row (replies are immutable; the original is kept for analytics
     * and so the user can navigate "show me what I had").
     */
    @Transactional
    public ReplyDto refine(UUID userId, UUID replyId, String instructions) {
        Reply original = replyRepository.findById(replyId)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.NOT_FOUND,
                        HttpStatus.NOT_FOUND.value(),
                        "Reply not found."));
        // Scope check: the conversation must be active and owned by the caller.
        // If they don't own it, surface NOT_FOUND rather than FORBIDDEN — we
        // never confirm the existence of resources the caller can't see.
        Conversation conversation = conversationRepository
                .findActiveByIdAndUser(original.getConversationId(), userId)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.NOT_FOUND,
                        HttpStatus.NOT_FOUND.value(),
                        "Reply not found."));

        // Quota check — refine counts against the same daily limit as generate.
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

        RefineOrchestrator.RefineOutput output;
        try {
            output = refineOrchestrator.refine(new RefineOrchestrator.RefineInput(
                    original.getText(),
                    original.getToneCode(),
                    instructions));
        } catch (ContentBlockedException blocked) {
            throw new ApiException(
                    "CONTENT_BLOCKED",
                    HttpStatus.UNPROCESSABLE_ENTITY.value(),
                    "Refined reply was blocked by the moderation policy.");
        }

        Reply refined = new Reply(
                Ids.newId(),
                conversation.getId(),
                /* rank */ 1,
                output.text(),
                original.getToneCode(),
                output.modelUsed(),
                clock.instant());
        Reply saved = replyRepository.save(refined);
        return ReplyDto.from(saved);
    }
}
