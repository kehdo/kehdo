package app.kehdo.backend.infra.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * Provisions {@link S3Client} + {@link S3Presigner} only when
 * {@code kehdo.storage.provider=s3}. The same beans drive AWS S3, MinIO,
 * and Cloudflare R2 — what differs is the {@code endpoint} +
 * {@code forcePathStyle} configuration.
 *
 * <p>Local dev uses MinIO from {@code infra/docker-compose.yml}:
 * <pre>
 *   kehdo:
 *     storage:
 *       provider: s3
 *       s3:
 *         endpoint: http://localhost:9000
 *         force-path-style: true
 *         region: us-east-1
 *         bucket: kehdo-screenshots-dev
 *         access-key: minioadmin
 *         secret-key: minioadmin
 * </pre>
 *
 * <p>Production (AWS S3 or R2) just changes the endpoint + credentials
 * via env vars; the code stays identical.</p>
 */
@Configuration
@ConditionalOnProperty(name = "kehdo.storage.provider", havingValue = "s3")
@EnableConfigurationProperties(S3Properties.class)
public class S3StorageConfig {

    @Bean(destroyMethod = "close")
    public S3Client s3Client(S3Properties props) {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(props.region()))
                .credentialsProvider(creds(props));
        if (hasEndpoint(props)) {
            builder.endpointOverride(URI.create(props.endpoint()));
        }
        if (props.forcePathStyle()) {
            builder.serviceConfiguration(pathStyle());
        }
        return builder.build();
    }

    @Bean(destroyMethod = "close")
    public S3Presigner s3Presigner(S3Properties props) {
        S3Presigner.Builder builder = S3Presigner.builder()
                .region(Region.of(props.region()))
                .credentialsProvider(creds(props));
        if (hasEndpoint(props)) {
            builder.endpointOverride(URI.create(props.endpoint()));
        }
        if (props.forcePathStyle()) {
            builder.serviceConfiguration(pathStyle());
        }
        return builder.build();
    }

    private static StaticCredentialsProvider creds(S3Properties props) {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(props.accessKey(), props.secretKey()));
    }

    private static boolean hasEndpoint(S3Properties props) {
        return props.endpoint() != null && !props.endpoint().isBlank();
    }

    private static S3Configuration pathStyle() {
        return S3Configuration.builder().pathStyleAccessEnabled(true).build();
    }
}
