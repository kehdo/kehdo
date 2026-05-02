package app.kehdo.backend.api.conversation.dto;

import app.kehdo.backend.conversation.ParsedMessage;

/**
 * Wire shape for one parsed message in a {@link ConversationDto} response.
 */
public record ParsedMessageDto(
        String speaker,
        String text,
        Double confidence) {

    public static ParsedMessageDto from(ParsedMessage m) {
        return new ParsedMessageDto(m.speaker().name(), m.text(), m.confidence());
    }
}
