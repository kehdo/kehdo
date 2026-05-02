package app.kehdo.backend.api.conversation.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Wire shape for {@code POST /conversations/{id}/generate}.
 */
public record GenerateResponse(
        UUID conversationId,
        String tone,
        List<ReplyDto> replies,
        Instant generatedAt,
        String modelUsed) {
}
