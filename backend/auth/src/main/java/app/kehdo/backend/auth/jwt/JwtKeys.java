package app.kehdo.backend.auth.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Resolves the RSA keypair used to sign and verify access-token JWTs.
 *
 * <p>Resolution order:</p>
 * <ol>
 *   <li>If {@code kehdo.jwt.public-key-path} and {@code kehdo.jwt.private-key-path}
 *       both resolve to existing resources, load the PEM-encoded keys from
 *       them (production behavior).</li>
 *   <li>Otherwise, generate a fresh RSA-2048 keypair in memory and log a
 *       warning. Acceptable for local dev / tests; tokens become invalid
 *       on JVM restart.</li>
 * </ol>
 *
 * <p>PEM parsing accepts the standard {@code BEGIN PUBLIC KEY} (X.509) and
 * {@code BEGIN PRIVATE KEY} (PKCS8) formats — generate with
 * {@code openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048
 * -out jwt-private.pem} and {@code openssl rsa -pubout -in jwt-private.pem
 * -out jwt-public.pem}.</p>
 */
public final class JwtKeys {

    private static final Logger log = LoggerFactory.getLogger(JwtKeys.class);
    private static final ResourceLoader RESOURCE_LOADER = new DefaultResourceLoader();

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final boolean ephemeral;

    private JwtKeys(PrivateKey privateKey, PublicKey publicKey, boolean ephemeral) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.ephemeral = ephemeral;
    }

    public static JwtKeys load(JwtProperties props) {
        Resource publicResource = resolveIfExists(props.publicKeyPath());
        Resource privateResource = resolveIfExists(props.privateKeyPath());

        if (publicResource != null && privateResource != null) {
            try {
                PublicKey publicKey = parsePublicKey(read(publicResource));
                PrivateKey privateKey = parsePrivateKey(read(privateResource));
                log.info("JWT keys loaded from configured resources");
                return new JwtKeys(privateKey, publicKey, false);
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Failed to load JWT keys from configured resources", e);
            }
        }

        log.warn(
                "JWT key paths not found ({} / {}); generating an EPHEMERAL RSA-2048 keypair "
                        + "for this JVM. Tokens will be invalid after restart. Configure "
                        + "kehdo.jwt.{public,private}-key-path before deploying anywhere.",
                props.publicKeyPath(), props.privateKeyPath());
        KeyPair pair = generateEphemeral();
        return new JwtKeys(pair.getPrivate(), pair.getPublic(), true);
    }

    public PrivateKey privateKey() { return privateKey; }
    public PublicKey publicKey() { return publicKey; }
    public boolean isEphemeral() { return ephemeral; }

    private static Resource resolveIfExists(String location) {
        if (location == null || location.isBlank()) return null;
        Resource res = RESOURCE_LOADER.getResource(location);
        return res.exists() ? res : null;
    }

    private static String read(Resource resource) throws IOException {
        try (InputStream in = resource.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static PublicKey parsePublicKey(String pem) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] der = pemToDer(pem, "PUBLIC KEY");
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
    }

    private static PrivateKey parsePrivateKey(String pem) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] der = pemToDer(pem, "PRIVATE KEY");
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
    }

    private static byte[] pemToDer(String pem, String label) {
        String stripped = pem
                .replace("-----BEGIN " + label + "-----", "")
                .replace("-----END " + label + "-----", "")
                .replaceAll("\\s+", "");
        return Base64.getDecoder().decode(stripped);
    }

    private static KeyPair generateEphemeral() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            return kpg.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA not available — JVM is broken", e);
        }
    }
}
