package app.kehdo.backend.user;

/**
 * Thrown by {@link QuotaService#consumeOrThrow} when the user has hit
 * their daily reply ceiling. The controller-side mapping turns this
 * into {@code 402 DAILY_QUOTA_EXCEEDED} per
 * {@code contracts/openapi/kehdo.v1.yaml}'s {@code QuotaExceeded}
 * response — the upgrade-CTA signal for the Android client.
 */
public class QuotaExceededException extends RuntimeException {

    private final UserUsage usage;

    public QuotaExceededException(UserUsage usage) {
        super("Daily reply quota exceeded: " + usage.dailyUsed() + "/" + usage.dailyLimit());
        this.usage = usage;
    }

    public UserUsage getUsage() {
        return usage;
    }
}
