package app.kehdo.backend.auth.jwt;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final UUID USER_ID = UUID.fromString("018f23ab-1234-7000-8000-000000000000");
    private static final UUID SESSION_ID = UUID.fromString("018f23ab-aaaa-7000-bbbb-000000000000");

    private JwtProperties props;
    private JwtKeys keys;

    @BeforeEach
    void setUp() {
        // Empty paths force ephemeral key generation
        props = new JwtProperties("https://api.kehdo.test", 300, 30, "", "");
        keys = JwtKeys.load(props);
        assertThat(keys.isEphemeral()).isTrue();
    }

    @Test
    void issued_token_validates_back_to_same_claims() {
        Clock fixed = Clock.fixed(Instant.parse("2026-05-01T00:00:00Z"), ZoneOffset.UTC);
        JwtService service = new JwtService(props, keys, fixed);

        JwtService.IssuedAccessToken issued = service.issueAccess(USER_ID, SESSION_ID);
        JwtService.ValidatedAccessToken validated = service.validateAccess(issued.token());

        assertThat(validated.userId()).isEqualTo(USER_ID);
        assertThat(validated.sessionId()).isEqualTo(SESSION_ID);
        assertThat(validated.issuedAt()).isEqualTo(Instant.parse("2026-05-01T00:00:00Z"));
        assertThat(validated.expiresAt()).isEqualTo(Instant.parse("2026-05-01T00:05:00Z"));
        assertThat(issued.expiresAt())
                .isEqualTo(issued.issuedAt().plus(Duration.ofMinutes(5)));
    }

    @Test
    void expired_token_fails_validation() {
        Clock fixed = Clock.fixed(Instant.parse("2026-05-01T00:00:00Z"), ZoneOffset.UTC);
        JwtService issuer = new JwtService(props, keys, fixed);
        JwtService.IssuedAccessToken issued = issuer.issueAccess(USER_ID, SESSION_ID);

        // Validate from a clock 10 minutes in the future — token TTL is 5 min
        Clock future = Clock.fixed(Instant.parse("2026-05-01T00:10:00Z"), ZoneOffset.UTC);
        JwtService validator = new JwtService(props, keys, future);

        assertThatThrownBy(() -> validator.validateAccess(issued.token()))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void token_signed_by_different_key_fails_validation() {
        Clock fixed = Clock.fixed(Instant.parse("2026-05-01T00:00:00Z"), ZoneOffset.UTC);
        JwtService a = new JwtService(props, keys, fixed);
        JwtKeys otherKeys = JwtKeys.load(props);
        JwtService b = new JwtService(props, otherKeys, fixed);

        String tokenSignedByA = a.issueAccess(USER_ID, SESSION_ID).token();

        assertThatThrownBy(() -> b.validateAccess(tokenSignedByA))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void token_with_wrong_issuer_fails_validation() {
        Clock fixed = Clock.fixed(Instant.parse("2026-05-01T00:00:00Z"), ZoneOffset.UTC);
        JwtProperties propsForIssuerA = new JwtProperties("https://api.kehdo.test", 300, 30, "", "");
        JwtKeys sharedKeys = JwtKeys.load(propsForIssuerA);
        JwtService issuerA = new JwtService(propsForIssuerA, sharedKeys, fixed);

        JwtProperties propsForIssuerB = new JwtProperties("https://different.kehdo.test", 300, 30, "", "");
        JwtService validatorB = new JwtService(propsForIssuerB, sharedKeys, fixed);

        String tokenFromA = issuerA.issueAccess(USER_ID, SESSION_ID).token();

        assertThatThrownBy(() -> validatorB.validateAccess(tokenFromA))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void garbage_token_fails_validation() {
        Clock fixed = Clock.fixed(Instant.parse("2026-05-01T00:00:00Z"), ZoneOffset.UTC);
        JwtService service = new JwtService(props, keys, fixed);

        // Malformed-but-present input throws JwtException (jjwt's typed parse error)
        assertThatThrownBy(() -> service.validateAccess("not-a-jwt"))
                .isInstanceOf(JwtException.class);

        // Empty input fails earlier in jjwt's input-validation guard with
        // IllegalArgumentException — also a hard failure, just a different
        // exception type. The JWT filter only invokes validateAccess when the
        // header had a "Bearer " prefix followed by content, so this path is
        // defensive only.
        assertThatThrownBy(() -> service.validateAccess(""))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
