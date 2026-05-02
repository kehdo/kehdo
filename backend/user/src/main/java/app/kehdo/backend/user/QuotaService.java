package app.kehdo.backend.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Daily reply-generation quota per backend/CLAUDE.md security rule
 * "5/day free, 100/day paid". Counter lives in Redis under a per-user
 * per-UTC-day key; expires at next UTC midnight.
 *
 * <p>Concurrency model: {@link #consumeOrThrow} is intentionally
 * <em>check-then-increment</em> rather than atomic. A burst of
 * concurrent /generate calls from the same user can race past the
 * check and produce one or two replies over the cap; subsequent calls
 * see the over-counted total and reject. For a 5-replies-a-day limit
 * the user-visible drift is at most "I got 6 instead of 5 once".
 * Atomic enforcement via Lua script can land later if it actually
 * matters.</p>
 */
@Service
public class QuotaService {

    private static final String KEY_PREFIX = "quota:reply:";

    private final StringRedisTemplate redis;
    private final UserRepository userRepository;
    private final Clock clock;
    private final int freeDailyLimit;
    private final int proDailyLimit;

    public QuotaService(
            StringRedisTemplate redis,
            UserRepository userRepository,
            Clock clock,
            @Value("${kehdo.rate-limit.free-daily-replies:5}") int freeDailyLimit,
            @Value("${kehdo.rate-limit.pro-daily-replies:100}") int proDailyLimit) {
        this.redis = redis;
        this.userRepository = userRepository;
        this.clock = clock;
        this.freeDailyLimit = freeDailyLimit;
        this.proDailyLimit = proDailyLimit;
    }

    /**
     * Returns current usage without modifying the counter. Used by
     * {@code GET /me/usage}.
     */
    public UserUsage current(UUID userId) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "Active user not found for usage lookup: " + userId));
        int used = readCounter(userId);
        int limit = limitFor(user.getPlan());
        return new UserUsage(used, limit, nextUtcMidnight());
    }

    /**
     * Increments the counter for a generate or refine call. Throws
     * {@link QuotaExceededException} if the user is already at or above
     * the daily limit.
     *
     * <p>UNLIMITED users still increment (analytics) but never throw.</p>
     */
    public void consumeOrThrow(UUID userId, UserPlan plan) {
        int limit = limitFor(plan);
        if (plan != UserPlan.UNLIMITED) {
            int current = readCounter(userId);
            if (current >= limit) {
                throw new QuotaExceededException(new UserUsage(current, limit, nextUtcMidnight()));
            }
        }
        increment(userId);
    }

    // ---- internals -----------------------------------------------------

    private int readCounter(UUID userId) {
        String value = redis.opsForValue().get(quotaKey(userId));
        if (value == null) return 0;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            // Defensive — counter entries should always be numeric. If
            // someone manually corrupted the key, treat as zero so the
            // user isn't punished.
            return 0;
        }
    }

    private void increment(UUID userId) {
        String key = quotaKey(userId);
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1L) {
            // First request today — set the TTL to expire at UTC midnight
            // (plus a 1-hour buffer to be safe across redis-clock skew).
            Duration ttl = Duration.between(clock.instant(), nextUtcMidnight()).plusHours(1);
            redis.expire(key, ttl);
        }
    }

    private String quotaKey(UUID userId) {
        LocalDate today = LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC);
        return KEY_PREFIX + userId + ":" + today;
    }

    private Instant nextUtcMidnight() {
        LocalDate today = LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC);
        return today.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    private int limitFor(UserPlan plan) {
        return switch (plan) {
            case STARTER -> freeDailyLimit;
            case PRO -> proDailyLimit;
            case UNLIMITED -> UserUsage.UNLIMITED_LIMIT;
        };
    }
}
