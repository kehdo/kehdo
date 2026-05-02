package app.kehdo.backend.api.conversation.dto;

import app.kehdo.backend.conversation.Reply;

import java.util.UUID;

/**
 * Wire shape for one ranked reply in a {@link GenerateResponse}.
 */
public record ReplyDto(
        UUID id,
        int rank,
        String text,
        String toneCode) {

    public static ReplyDto from(Reply r) {
        return new ReplyDto(r.getId(), r.getRank(), r.getText(), r.getToneCode());
    }
}
