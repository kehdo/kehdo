package app.kehdo.backend.auth.token;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Generates and verifies refresh tokens.
 *
 * <p>Format: {@code rt_<64 hex chars>} (32 random bytes encoded as hex).
 * Storage: SHA-256 hex of the FULL token (prefix included), 64 chars.</p>
 *
 * <p>The raw token is shown to the user once (in the auth response body)
 * and never again. The DB only sees the hash.</p>
 */
public final class RefreshTokens {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final HexFormat HEX = HexFormat.of();
    private static final String PREFIX = "rt_";
    private static final int RAW_BYTES = 32;

    private RefreshTokens() {}

    /** Generate a fresh raw refresh token in {@code rt_<hex>} format. */
    public static String generate() {
        byte[] bytes = new byte[RAW_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return PREFIX + HEX.formatHex(bytes);
    }

    /** SHA-256 hex of the full token; 64 hex chars. */
    public static String hash(String rawToken) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] digest = sha256.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HEX.formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available — JVM is broken", e);
        }
    }
}
