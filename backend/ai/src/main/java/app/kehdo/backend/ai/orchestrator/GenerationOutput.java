package app.kehdo.backend.ai.orchestrator;

import app.kehdo.backend.ai.llm.LlmReply;
import app.kehdo.backend.ai.speaker.SpeakerAttributor.AttributedLine;

import java.util.List;

/**
 * Result of a successful pipeline run.
 *
 * @param attributedMessages parsed + speaker-tagged conversation; the
 *                           controller maps these onto the persisted
 *                           {@code conversations.parsed_messages} JSONB column
 * @param replies            LLM-generated, moderation-approved replies;
 *                           always non-empty (the orchestrator throws
 *                           {@link ContentBlockedException} when every
 *                           reply was blocked)
 * @param modelUsed          identifier of the model that answered, persisted
 *                           per-reply on {@code replies.model_used}
 */
public record GenerationOutput(
        List<AttributedLine> attributedMessages,
        List<LlmReply> replies,
        String modelUsed) {
}
