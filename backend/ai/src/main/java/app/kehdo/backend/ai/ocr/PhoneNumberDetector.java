package app.kehdo.backend.ai.ocr;

import java.util.regex.Pattern;

/**
 * Detects strings that look like phone numbers, so the OCR adapter can
 * drop them per {@code backend/CLAUDE.md} AI rule 9: <em>"Phone numbers
 * must never be stored as {@code contact_profiles.contact_name}"</em>.
 *
 * <p>Heuristic, not a parser — we'd rather false-positive on a number-y
 * string and miss a contact name than save someone's mobile as their
 * handle. The contact-name pipeline is best-effort anyway.</p>
 *
 * <p>Approach: strip the standard phone-formatting chars
 * ({@code + - space ( ) .}) from the input and check that what's left
 * is between 7 and 15 ASCII digits. Catches every real-world phone
 * format (E.164, US, Indian, EU) and rejects names that happen to
 * contain digits ({@code "Apt 4B"}, {@code "Mum (call)"}).</p>
 */
public final class PhoneNumberDetector {

    private static final Pattern PHONE_FORMAT_CHARS = Pattern.compile("[+\\-\\s().]");
    private static final Pattern ALL_DIGITS = Pattern.compile("\\d+");

    /** Min E.164 plausible length is 7 (small countries); max is 15 per spec. */
    private static final int MIN_DIGITS = 7;
    private static final int MAX_DIGITS = 15;

    private PhoneNumberDetector() {}

    public static boolean looksLikePhoneNumber(String candidate) {
        if (candidate == null || candidate.isBlank()) return false;
        String stripped = PHONE_FORMAT_CHARS.matcher(candidate.trim()).replaceAll("");
        if (stripped.isEmpty()) return false;
        if (!ALL_DIGITS.matcher(stripped).matches()) return false;
        return stripped.length() >= MIN_DIGITS && stripped.length() <= MAX_DIGITS;
    }
}
