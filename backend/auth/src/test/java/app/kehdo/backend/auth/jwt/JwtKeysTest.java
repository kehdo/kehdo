package app.kehdo.backend.auth.jwt;

import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies the resolution order documented on {@link JwtProperties}:
 * inline PEM (env-supplied) wins over file paths, falls back to ephemeral.
 */
class JwtKeysTest {

    @Test
    void loads_inline_pem_when_both_pems_set_and_skips_resource_path() throws Exception {
        KeyPair pair = generate2048();
        String publicPem = pemEncode(pair.getPublic(), "PUBLIC KEY");
        String privatePem = pemEncode(pair.getPrivate(), "PRIVATE KEY");

        JwtProperties props = new JwtProperties(
                "https://api.kehdo.test", 300, 30,
                /* publicKeyPath  */ "classpath:does-not-exist-public.pem",
                /* privateKeyPath */ "classpath:does-not-exist-private.pem",
                /* publicKeyPem   */ publicPem,
                /* privateKeyPem  */ privatePem
        );

        JwtKeys keys = JwtKeys.load(props);

        // Inline PEM takes precedence — keys load even though the configured
        // file paths don't exist on disk.
        assertThat(keys.isEphemeral()).isFalse();
        assertThat(keys.publicKey()).isEqualTo(pair.getPublic());
        assertThat(keys.privateKey()).isEqualTo(pair.getPrivate());
    }

    @Test
    void empty_inline_pem_falls_through_to_ephemeral_when_no_files() {
        JwtProperties props = new JwtProperties(
                "https://api.kehdo.test", 300, 30, "", "", "", "");

        JwtKeys keys = JwtKeys.load(props);

        assertThat(keys.isEphemeral()).isTrue();
    }

    @Test
    void only_public_pem_set_falls_through_to_path_then_ephemeral() throws Exception {
        // Single side of the keypair isn't enough — both must be present
        // for the inline path to activate.
        KeyPair pair = generate2048();
        String publicPem = pemEncode(pair.getPublic(), "PUBLIC KEY");

        JwtProperties props = new JwtProperties(
                "https://api.kehdo.test", 300, 30, "", "", publicPem, null);

        JwtKeys keys = JwtKeys.load(props);

        // Falls through past inline (private missing) and past path
        // (paths empty) to ephemeral generation.
        assertThat(keys.isEphemeral()).isTrue();
    }

    @Test
    void malformed_inline_pem_throws_with_actionable_message() {
        JwtProperties props = new JwtProperties(
                "https://api.kehdo.test", 300, 30, "", "",
                "-----BEGIN PUBLIC KEY-----\nthis is not base64!@#\n-----END PUBLIC KEY-----",
                "-----BEGIN PRIVATE KEY-----\nalso garbage\n-----END PRIVATE KEY-----"
        );

        assertThatThrownBy(() -> JwtKeys.load(props))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("KEHDO_JWT_PUBLIC_KEY_PEM");
    }

    private static KeyPair generate2048() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        return kpg.generateKeyPair();
    }

    private static String pemEncode(PublicKey key, String label) {
        return wrap(Base64.getEncoder().encodeToString(key.getEncoded()), label);
    }

    private static String pemEncode(PrivateKey key, String label) {
        return wrap(Base64.getEncoder().encodeToString(key.getEncoded()), label);
    }

    private static String wrap(String base64, String label) {
        StringBuilder sb = new StringBuilder()
                .append("-----BEGIN ").append(label).append("-----\n");
        for (int i = 0; i < base64.length(); i += 64) {
            sb.append(base64, i, Math.min(i + 64, base64.length())).append('\n');
        }
        sb.append("-----END ").append(label).append("-----");
        return sb.toString();
    }
}
