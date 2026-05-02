package app.kehdo.backend.ai.ocr;

import org.junit.jupiter.api.Test;

import static app.kehdo.backend.ai.ocr.PhoneNumberDetector.looksLikePhoneNumber;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards CLAUDE.md AI rule 9: phone numbers must never end up as a
 * contact name. False positives (rejecting a real name as "phone-y") are
 * acceptable — false negatives (saving a phone number as a name) are not.
 */
class PhoneNumberDetectorTest {

    @Test
    void recognises_common_international_formats() {
        assertThat(looksLikePhoneNumber("+91 98765 43210")).isTrue();
        assertThat(looksLikePhoneNumber("+1 (555) 123-4567")).isTrue();
        assertThat(looksLikePhoneNumber("+44 20 7946 0958")).isTrue();
    }

    @Test
    void recognises_local_formats() {
        assertThat(looksLikePhoneNumber("9876543210")).isTrue();
        assertThat(looksLikePhoneNumber("555-123-4567")).isTrue();
        assertThat(looksLikePhoneNumber("(555) 123 4567")).isTrue();
    }

    @Test
    void rejects_real_names() {
        assertThat(looksLikePhoneNumber("Priya Sharma")).isFalse();
        assertThat(looksLikePhoneNumber("Alex")).isFalse();
        assertThat(looksLikePhoneNumber("Amma 🩷")).isFalse();
        assertThat(looksLikePhoneNumber("M&M")).isFalse();
    }

    @Test
    void rejects_strings_with_text_around_numbers() {
        // "Mum (call)" — only 'call' has digits removed but original is mostly text.
        assertThat(looksLikePhoneNumber("Mum (call)")).isFalse();
        assertThat(looksLikePhoneNumber("Apt 4B")).isFalse();
    }

    @Test
    void rejects_too_short_or_too_long_digit_runs() {
        // <7 digits — could be a flat number, year, etc.
        assertThat(looksLikePhoneNumber("12345")).isFalse();
        // >15 digits — no real-world phone number is that long.
        assertThat(looksLikePhoneNumber("12345678901234567890")).isFalse();
    }

    @Test
    void rejects_blank_or_null() {
        assertThat(looksLikePhoneNumber(null)).isFalse();
        assertThat(looksLikePhoneNumber("")).isFalse();
        assertThat(looksLikePhoneNumber("   ")).isFalse();
    }
}
