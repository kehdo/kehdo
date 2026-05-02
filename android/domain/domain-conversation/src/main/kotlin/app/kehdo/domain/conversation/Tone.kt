package app.kehdo.domain.conversation

/**
 * Specific tone style within a Mode. The 12 V1 tones plus 6 planned
 * additions (per ADR 0006) are seeded server-side and fetched via
 * GET /v1/tones — the client never hardcodes the list.
 */
data class Tone(
    val code: String,
    val name: String,
    val emoji: String?,
    val description: String?,
    val mode: Mode,
    val isPro: Boolean,
    val sortOrder: Int
)
