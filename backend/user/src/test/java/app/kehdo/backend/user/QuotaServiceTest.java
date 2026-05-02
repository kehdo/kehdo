package app.kehdo.backend.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QuotaServiceTest {

    /** Fixed instant: 2026-05-03T10:00:00Z, well before next UTC midnight. */
    private static final Instant NOW = Instant.parse("2026-05-03T10:00:00Z");
    /** Configured caps for the tests; service reads via @Value normally. */
    private static final int FREE_LIMIT = 5;
    private static final int PRO_LIMIT = 100;

    private StringRedisTemplate redis;
    private ValueOperations<String, String> ops;
    private UserRepository userRepository;
    private QuotaService service;

    @BeforeEach
    void setUp() {
        redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> mockedOps = mock(ValueOperations.class);
        ops = mockedOps;
        when(redis.opsForValue()).thenReturn(ops);
        userRepository = mock(UserRepository.class);
        service = new QuotaService(
                redis,
                userRepository,
                Clock.fixed(NOW, ZoneOffset.UTC),
                FREE_LIMIT,
                PRO_LIMIT);
    }

    @Test
    void current_returns_zero_when_no_redis_key_yet_with_starter_limit() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findActiveById(userId))
                .thenReturn(Optional.of(newUser(userId, UserPlan.STARTER)));
        when(ops.get(any())).thenReturn(null);

        UserUsage usage = service.current(userId);

        assertThat(usage.dailyUsed()).isZero();
        assertThat(usage.dailyLimit()).isEqualTo(FREE_LIMIT);
        assertThat(usage.resetAt()).isEqualTo(Instant.parse("2026-05-04T00:00:00Z"));
        assertThat(usage.unlimited()).isFalse();
    }

    @Test
    void current_reads_existing_count_for_pro_user() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findActiveById(userId))
                .thenReturn(Optional.of(newUser(userId, UserPlan.PRO)));
        when(ops.get(any())).thenReturn("42");

        UserUsage usage = service.current(userId);

        assertThat(usage.dailyUsed()).isEqualTo(42);
        assertThat(usage.dailyLimit()).isEqualTo(PRO_LIMIT);
    }

    @Test
    void current_surfaces_unlimited_sentinel_for_unlimited_plan() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findActiveById(userId))
                .thenReturn(Optional.of(newUser(userId, UserPlan.UNLIMITED)));
        when(ops.get(any())).thenReturn("999");

        UserUsage usage = service.current(userId);

        assertThat(usage.unlimited()).isTrue();
        assertThat(usage.dailyLimit()).isEqualTo(UserUsage.UNLIMITED_LIMIT);
    }

    @Test
    void consume_increments_counter_and_sets_ttl_on_first_call() {
        UUID userId = UUID.randomUUID();
        when(ops.get(any())).thenReturn(null); // no prior count today
        when(ops.increment(any())).thenReturn(1L);

        service.consumeOrThrow(userId, UserPlan.STARTER);

        verify(ops).increment(any());
        // First-of-day call sets the TTL; verify the key was given an expiry.
        verify(redis).expire(any(), any(java.time.Duration.class));
    }

    @Test
    void consume_does_not_set_ttl_when_counter_already_exists() {
        UUID userId = UUID.randomUUID();
        when(ops.get(any())).thenReturn("3");
        when(ops.increment(any())).thenReturn(4L);

        service.consumeOrThrow(userId, UserPlan.STARTER);

        verify(redis, org.mockito.Mockito.never()).expire(any(), any(java.time.Duration.class));
    }

    @Test
    void consume_throws_when_counter_at_or_above_starter_limit() {
        UUID userId = UUID.randomUUID();
        when(ops.get(any())).thenReturn(String.valueOf(FREE_LIMIT));

        assertThatThrownBy(() -> service.consumeOrThrow(userId, UserPlan.STARTER))
                .isInstanceOf(QuotaExceededException.class)
                .satisfies(ex -> {
                    QuotaExceededException q = (QuotaExceededException) ex;
                    assertThat(q.getUsage().dailyUsed()).isEqualTo(FREE_LIMIT);
                    assertThat(q.getUsage().dailyLimit()).isEqualTo(FREE_LIMIT);
                });

        verify(ops, org.mockito.Mockito.never()).increment(any());
    }

    @Test
    void consume_never_throws_for_unlimited_plan_even_at_high_count() {
        UUID userId = UUID.randomUUID();
        when(ops.get(any())).thenReturn("100000"); // wildly past STARTER + PRO caps
        when(ops.increment(any())).thenReturn(100001L);

        service.consumeOrThrow(userId, UserPlan.UNLIMITED);
        // Reaches here without throwing; counter still ticks for analytics.
        verify(ops).increment(any());
    }

    @Test
    void quota_key_is_per_user_per_utc_day() {
        UUID userId = UUID.fromString("01939e6c-0001-7a4f-8000-000000000001");
        when(ops.get(any())).thenReturn(null);
        when(ops.increment(any())).thenReturn(1L);

        service.consumeOrThrow(userId, UserPlan.STARTER);

        // Key format: quota:reply:<uuid>:<yyyy-MM-dd>
        String expectedKey = "quota:reply:01939e6c-0001-7a4f-8000-000000000001:2026-05-03";
        verify(ops).increment(eq(expectedKey));
    }

    private static User newUser(UUID id, UserPlan plan) {
        return new User(id, "u@example.com", "$2a$12$hash", "U", plan, NOW);
    }
}
