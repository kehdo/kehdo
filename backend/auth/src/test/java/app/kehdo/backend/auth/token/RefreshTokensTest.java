package app.kehdo.backend.auth.token;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokensTest {

    @Test
    void generated_token_has_rt_prefix_and_64_hex_chars() {
        String token = RefreshTokens.generate();

        assertThat(token).startsWith("rt_");
        assertThat(token.substring(3)).matches("[0-9a-f]{64}");
        assertThat(token).hasSize(67); // 3 prefix + 64 hex
    }

    @Test
    void generate_returns_unique_values() {
        String a = RefreshTokens.generate();
        String b = RefreshTokens.generate();
        String c = RefreshTokens.generate();

        assertThat(a).isNotEqualTo(b).isNotEqualTo(c);
        assertThat(b).isNotEqualTo(c);
    }

    @Test
    void hash_is_deterministic_for_same_input() {
        String token = RefreshTokens.generate();

        String hash1 = RefreshTokens.hash(token);
        String hash2 = RefreshTokens.hash(token);

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void hash_produces_64_hex_chars() {
        String hash = RefreshTokens.hash(RefreshTokens.generate());

        assertThat(hash).matches("[0-9a-f]{64}");
        assertThat(hash).hasSize(64);
    }

    @Test
    void hash_differs_for_different_tokens() {
        String hashA = RefreshTokens.hash(RefreshTokens.generate());
        String hashB = RefreshTokens.hash(RefreshTokens.generate());

        assertThat(hashA).isNotEqualTo(hashB);
    }
}
