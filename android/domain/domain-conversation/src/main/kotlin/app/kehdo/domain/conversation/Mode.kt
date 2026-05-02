package app.kehdo.domain.conversation

/**
 * Coarse-grained tone grouping. Per ADR 0006, modes group tones (4 modes,
 * 18 tones planned). The user picks a mode first (low cognitive load),
 * then optionally drills into a specific tone within it.
 */
enum class Mode {
    CASUAL,
    FLIRTY,
    PROFESSIONAL,
    SINCERE
}
