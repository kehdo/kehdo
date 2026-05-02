package app.kehdo.backend.user;

import java.time.Instant;

/**
 * Snapshot of a user's daily reply quota. Returned by
 * {@code GET /me/usage} and used by the Android home screen to render
 * "3/5 replies left today".
 *
 * @param dailyUsed   replies generated since the last reset (UTC midnight)
 * @param dailyLimit  ceiling for the user's plan; for {@code UNLIMITED}
 *                    we surface a sentinel high value rather than null
 *                    or {@code -1} so the wire shape stays
 *                    {@code integer} per the OpenAPI contract
 * @param resetAt     when the counter rolls over — next UTC midnight
 */
public record UserUsage(int dailyUsed, int dailyLimit, Instant resetAt) {

    /** Sentinel value used in {@link #dailyLimit} for the UNLIMITED plan. */
    public static final int UNLIMITED_LIMIT = 999_999;

    public boolean unlimited() {
        return dailyLimit >= UNLIMITED_LIMIT;
    }
}
