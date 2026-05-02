package app.kehdo.data.conversation.mapper

import app.kehdo.core.network.api.dto.ConversationDto
import app.kehdo.core.network.api.dto.GenerateResponseDto
import app.kehdo.core.network.api.dto.ReplyDto
import app.kehdo.core.network.api.dto.ToneDto
import app.kehdo.domain.conversation.Conversation
import app.kehdo.domain.conversation.ConversationStatus
import app.kehdo.domain.conversation.Mode
import app.kehdo.domain.conversation.Reply
import app.kehdo.domain.conversation.Tone
import java.time.Instant
import java.time.format.DateTimeParseException

/**
 * Backend DTO → domain mapping. The contract is in
 * `/contracts/openapi/kehdo.v1.yaml` — these mappers exist so the
 * domain types stay free of any contract concerns (timestamp parsing,
 * unknown enum values, missing fields).
 *
 * Unknown / future status enum values fall back to FAILED so the UI
 * shows a useful error rather than crashing on an enum miss when the
 * backend rolls forward.
 */
internal object ConversationMappers {

    /**
     * Map [GenerateResponseDto] back into a domain [Conversation] in
     * `READY` state. The DTO doesn't carry the original conversation
     * status, but a successful `generate` response definitionally
     * means status flipped to READY.
     */
    fun fromGenerateResponse(dto: GenerateResponseDto): Conversation {
        val now = parseInstantOrNow(dto.generatedAt)
        return Conversation(
            id = dto.conversationId,
            status = ConversationStatus.READY,
            failureReason = null,
            toneCode = dto.tone,
            replies = dto.replies.map { reply -> reply.toDomain(dto.conversationId) },
            // Backend doesn't echo `createdAt` on generate — best we can
            // do is reuse `generatedAt` for both. The History endpoint
            // returns the real createdAt later.
            createdAt = now,
            updatedAt = now
        )
    }

    /** Map [ConversationDto] from `GET /conversations/{id}` to the domain. */
    fun fromConversation(dto: ConversationDto): Conversation {
        val createdAt = parseInstantOrNow(dto.createdAt)
        return Conversation(
            id = dto.id,
            status = parseStatus(dto.status),
            failureReason = null,
            toneCode = null,
            replies = emptyList(), // Replies come back via the generate response, not GET
            createdAt = createdAt,
            updatedAt = createdAt
        )
    }

    /**
     * Backend returns the catalog in display-order; we propagate that
     * via [Tone.sortOrder] so the UI doesn't need its own sort key.
     */
    fun fromTone(dto: ToneDto, sortOrder: Int): Tone = Tone(
        code = dto.code,
        name = dto.name,
        emoji = dto.emoji,
        description = dto.description,
        mode = modeFor(dto.code),
        isPro = dto.isPro,
        sortOrder = sortOrder
    )

    private fun ReplyDto.toDomain(conversationId: String): Reply {
        return Reply(
            id = id,
            conversationId = conversationId,
            rank = rank,
            text = text,
            toneCode = toneCode,
            isFavorited = false,
            isCopied = false,
            createdAt = System.currentTimeMillis()
        )
    }

    private fun parseStatus(raw: String): ConversationStatus = try {
        ConversationStatus.valueOf(raw)
    } catch (_: IllegalArgumentException) {
        // Unknown enum from a future backend rollout — show as failed
        // rather than crash the app.
        ConversationStatus.FAILED
    }

    private fun parseInstantOrNow(iso8601: String): Long = try {
        Instant.parse(iso8601).toEpochMilli()
    } catch (_: DateTimeParseException) {
        System.currentTimeMillis()
    }

    /**
     * Map an UPPER_SNAKE_CASE tone code to its display [Mode] grouping.
     * Mode is purely client-side categorization for the picker UI;
     * backend doesn't model it. Unknown / future codes fall back to
     * `CASUAL` so the UI never drops a tone.
     */
    private fun modeFor(code: String): Mode = when (code) {
        "CASUAL", "BRIEF", "PLAYFUL", "WITTY" -> Mode.CASUAL
        "FLIRTY", "SARCASTIC", "POETIC", "ENTHUSIASTIC" -> Mode.FLIRTY
        "DIRECT", "CONFIDENT", "FORMAL", "DIPLOMATIC", "CURIOUS" -> Mode.PROFESSIONAL
        "WARM", "THOUGHTFUL", "GRATEFUL", "APOLOGETIC", "EMPATHETIC" -> Mode.SINCERE
        else -> Mode.CASUAL
    }
}
