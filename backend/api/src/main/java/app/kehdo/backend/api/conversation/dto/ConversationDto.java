package app.kehdo.backend.api.conversation.dto;

import app.kehdo.backend.conversation.Conversation;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Wire shape for {@code GET /conversations/{id}} and as items inside
 * {@link ConversationPageDto}. Mirrors the {@code Conversation} schema in
 * the OpenAPI spec — never exposes {@code screenshotObjectKey},
 * {@code failureReason}, or any of the internal timestamps.
 */
public record ConversationDto(
        UUID id,
        String status,
        List<ParsedMessageDto> messages,
        Instant createdAt) {

    public static ConversationDto from(Conversation c) {
        List<ParsedMessageDto> messages = c.getParsedMessages() == null
                ? List.of()
                : c.getParsedMessages().stream().map(ParsedMessageDto::from).toList();
        return new ConversationDto(
                c.getId(),
                c.getStatus().name(),
                messages,
                c.getCreatedAt());
    }
}
