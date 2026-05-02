package app.kehdo.data.conversation.mapper

import app.kehdo.core.network.api.dto.ConversationDto
import app.kehdo.core.network.api.dto.GenerateResponseDto
import app.kehdo.core.network.api.dto.ReplyDto
import app.kehdo.core.network.api.dto.ToneDto
import app.kehdo.domain.conversation.ConversationStatus
import app.kehdo.domain.conversation.Mode
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.Instant

class ConversationMappersTest {

    @Test
    fun `fromGenerateResponse maps replies in order with conversation id propagated`() {
        val dto = GenerateResponseDto(
            conversationId = "conv-1",
            tone = "WARM",
            replies = listOf(
                ReplyDto(id = "r1", rank = 1, text = "Hi there", toneCode = "WARM"),
                ReplyDto(id = "r2", rank = 2, text = "Sounds good", toneCode = "WARM"),
                ReplyDto(id = "r3", rank = 3, text = "Of course", toneCode = "WARM")
            ),
            generatedAt = "2026-05-03T08:00:00Z",
            modelUsed = "vertex-ai/gemini-2.0-flash"
        )

        val conversation = ConversationMappers.fromGenerateResponse(dto)

        assertThat(conversation.id).isEqualTo("conv-1")
        assertThat(conversation.status).isEqualTo(ConversationStatus.READY)
        assertThat(conversation.toneCode).isEqualTo("WARM")
        assertThat(conversation.replies).hasSize(3)
        assertThat(conversation.replies.map { it.text })
            .containsExactly("Hi there", "Sounds good", "Of course").inOrder()
        assertThat(conversation.replies.first().conversationId).isEqualTo("conv-1")
        assertThat(conversation.replies.first().isCopied).isFalse()
        assertThat(conversation.replies.first().isFavorited).isFalse()
        assertThat(conversation.createdAt)
            .isEqualTo(Instant.parse("2026-05-03T08:00:00Z").toEpochMilli())
    }

    @Test
    fun `fromGenerateResponse falls back to now when generatedAt is malformed`() {
        val before = System.currentTimeMillis()
        val dto = GenerateResponseDto(
            conversationId = "conv-1",
            tone = "WARM",
            replies = emptyList(),
            generatedAt = "not-a-timestamp"
        )

        val conversation = ConversationMappers.fromGenerateResponse(dto)
        val after = System.currentTimeMillis()

        // Falls back to System.currentTimeMillis() — should be in [before, after]
        assertThat(conversation.createdAt).isAtLeast(before)
        assertThat(conversation.createdAt).isAtMost(after)
    }

    @Test
    fun `fromConversation parses each known status`() {
        ConversationStatus.entries.forEach { status ->
            val dto = ConversationDto(
                id = "conv-1",
                status = status.name,
                createdAt = "2026-05-03T08:00:00Z"
            )
            val conversation = ConversationMappers.fromConversation(dto)
            assertThat(conversation.status).isEqualTo(status)
        }
    }

    @Test
    fun `fromConversation falls back to FAILED on unknown status`() {
        val dto = ConversationDto(
            id = "conv-1",
            status = "FUTURE_STATUS",
            createdAt = "2026-05-03T08:00:00Z"
        )

        val conversation = ConversationMappers.fromConversation(dto)

        // Unknown status from a future backend shouldn't crash — show FAILED.
        assertThat(conversation.status).isEqualTo(ConversationStatus.FAILED)
    }

    @Test
    fun `fromTone propagates fields and assigns sort order from list index`() {
        val dto = ToneDto(
            code = "WARM",
            name = "Warm",
            emoji = "🙏",
            description = "Sincere, kind",
            isPro = false
        )

        val tone = ConversationMappers.fromTone(dto, sortOrder = 7)

        assertThat(tone.code).isEqualTo("WARM")
        assertThat(tone.emoji).isEqualTo("🙏")
        assertThat(tone.description).isEqualTo("Sincere, kind")
        assertThat(tone.isPro).isFalse()
        assertThat(tone.sortOrder).isEqualTo(7)
    }

    @Test
    fun `fromTone groups all 18 backend tones into client-side modes`() {
        val byMode = listOf(
            // Mode.CASUAL
            "CASUAL" to Mode.CASUAL,
            "BRIEF" to Mode.CASUAL,
            "PLAYFUL" to Mode.CASUAL,
            "WITTY" to Mode.CASUAL,
            // Mode.FLIRTY
            "FLIRTY" to Mode.FLIRTY,
            "SARCASTIC" to Mode.FLIRTY,
            "POETIC" to Mode.FLIRTY,
            "ENTHUSIASTIC" to Mode.FLIRTY,
            // Mode.PROFESSIONAL
            "DIRECT" to Mode.PROFESSIONAL,
            "CONFIDENT" to Mode.PROFESSIONAL,
            "FORMAL" to Mode.PROFESSIONAL,
            "DIPLOMATIC" to Mode.PROFESSIONAL,
            "CURIOUS" to Mode.PROFESSIONAL,
            // Mode.SINCERE
            "WARM" to Mode.SINCERE,
            "THOUGHTFUL" to Mode.SINCERE,
            "GRATEFUL" to Mode.SINCERE,
            "APOLOGETIC" to Mode.SINCERE,
            "EMPATHETIC" to Mode.SINCERE
        )

        // Sanity check we covered all 18 — if the contract grows past 18,
        // this assertion forces us to update the mapping.
        assertThat(byMode).hasSize(18)

        byMode.forEach { (code, expected) ->
            val tone = ConversationMappers.fromTone(
                ToneDto(code = code, name = code, emoji = "", description = null, isPro = false),
                sortOrder = 0
            )
            assertThat(tone.mode).isEqualTo(expected)
        }
    }

    @Test
    fun `unknown future tone code falls back to CASUAL mode rather than crashing`() {
        val tone = ConversationMappers.fromTone(
            ToneDto(code = "FUTURE_TONE", name = "Future", emoji = "🆕", description = null, isPro = true),
            sortOrder = 99
        )

        // We'd rather show the new tone in the wrong group than drop it
        // entirely — graceful degradation when backend rolls forward.
        assertThat(tone.mode).isEqualTo(Mode.CASUAL)
    }
}
