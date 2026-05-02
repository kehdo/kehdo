package app.kehdo.backend.api.me.dto;

import app.kehdo.backend.user.UserUsage;

import java.time.Instant;

/**
 * Wire shape for {@code GET /me/usage} per
 * {@code contracts/openapi/kehdo.v1.yaml}'s {@code UsageResponse} schema.
 */
public record UsageResponse(
        int dailyUsed,
        int dailyLimit,
        Instant resetAt) {

    public static UsageResponse from(UserUsage usage) {
        return new UsageResponse(usage.dailyUsed(), usage.dailyLimit(), usage.resetAt());
    }
}
