package app.kehdo.backend.api.tone;

import app.kehdo.backend.api.tone.dto.ToneDto;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Catalog invariants. The catalog is THE source of truth for the
 * {@code GET /tones} payload, so these checks guard the spec.
 */
class ToneCatalogTest {

    private static final Set<String> EXPECTED_CODES = Set.of(
            // Free
            "CASUAL", "WARM", "DIRECT", "THOUGHTFUL", "GRATEFUL",
            "APOLOGETIC", "CONFIDENT", "BRIEF",
            // Pro
            "FLIRTY", "WITTY", "PLAYFUL", "SARCASTIC", "FORMAL",
            "POETIC", "EMPATHETIC", "ENTHUSIASTIC", "DIPLOMATIC", "CURIOUS"
    );

    private final ToneCatalog catalog = new ToneCatalog();

    @Test
    void exposes_eighteen_tones() {
        assertThat(catalog.all()).hasSize(18);
    }

    @Test
    void code_set_matches_openapi_tone_code_enum() {
        Set<String> actual = catalog.all().stream().map(ToneDto::code).collect(java.util.stream.Collectors.toSet());
        assertThat(actual).isEqualTo(EXPECTED_CODES);
    }

    @Test
    void splits_eight_free_and_ten_pro() {
        List<ToneDto> free = catalog.all().stream().filter(t -> !t.isPro()).toList();
        List<ToneDto> pro = catalog.all().stream().filter(ToneDto::isPro).toList();

        assertThat(free).hasSize(8);
        assertThat(pro).hasSize(10);
    }

    @Test
    void every_tone_has_required_display_fields() {
        for (ToneDto tone : catalog.all()) {
            assertThat(tone.code()).isNotBlank();
            assertThat(tone.name()).isNotBlank();
            assertThat(tone.emoji()).isNotBlank();
            assertThat(tone.description()).isNotBlank();
        }
    }
}
