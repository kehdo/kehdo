package app.kehdo.backend.api.tone.dto;

/**
 * Wire shape for {@code GET /tones} per
 * {@code contracts/openapi/kehdo.v1.yaml}'s {@code Tone} schema.
 *
 * @param code        UPPER_SNAKE_CASE tone identifier from {@code ToneCode}
 * @param name        display label, e.g. "Warm"
 * @param emoji       single-glyph emoji for the tone chip
 * @param description one-line clarifier shown under the chip
 * @param isPro       true → only callable on PRO / UNLIMITED plans;
 *                    free tier (STARTER) gets the 8 isPro=false tones
 */
public record ToneDto(
        String code,
        String name,
        String emoji,
        String description,
        boolean isPro) {
}
