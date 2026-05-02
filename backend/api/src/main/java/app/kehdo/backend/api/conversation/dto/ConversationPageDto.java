package app.kehdo.backend.api.conversation.dto;

import java.util.List;

/**
 * Wire shape for {@code GET /conversations}. Cursor-based pagination —
 * {@link #nextCursor()} is null on the last page.
 */
public record ConversationPageDto(
        List<ConversationDto> items,
        String nextCursor) {
}
