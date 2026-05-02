package app.kehdo.backend.api.conversation.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Response shape for {@code POST /conversations}, mirroring
 * {@code CreateConversationResponse} in the OpenAPI spec.
 */
public record CreateConversationResponse(
        UUID conversationId,
        String uploadUrl,
        Instant uploadExpiresAt) {
}
