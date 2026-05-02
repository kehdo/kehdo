package app.kehdo.backend.infra.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Clock;
import java.time.Duration;
import java.util.UUID;

/**
 * Real S3 / MinIO / R2 implementation of {@link ScreenshotStorage}.
 * Active when {@code kehdo.storage.provider=s3}; otherwise
 * {@link StubScreenshotStorage} runs.
 *
 * <p>{@code presignUpload} mints a short-lived PUT URL the client can
 * upload to directly, without the bytes ever crossing the backend.
 * {@code download} pulls the bytes once they're up — that's the call the
 * OCR adapter (Phase 4 PR 6) makes when generate runs.</p>
 */
@Service
@ConditionalOnProperty(name = "kehdo.storage.provider", havingValue = "s3")
public class S3ScreenshotStorage implements ScreenshotStorage {

    private static final Logger log = LoggerFactory.getLogger(S3ScreenshotStorage.class);

    private final S3Client s3;
    private final S3Presigner presigner;
    private final S3Properties props;
    private final Clock clock;

    public S3ScreenshotStorage(S3Client s3, S3Presigner presigner, S3Properties props, Clock clock) {
        this.s3 = s3;
        this.presigner = presigner;
        this.props = props;
        this.clock = clock;
    }

    @Override
    public PresignedUpload presignUpload(UUID conversationId) {
        String objectKey = "conversations/" + conversationId + "/screenshot.png";
        Duration ttl = Duration.ofMinutes(props.presignedUrlTtlMinutes());

        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(props.bucket())
                .key(objectKey)
                .contentType("image/png")
                .build();
        PutObjectPresignRequest req = PutObjectPresignRequest.builder()
                .signatureDuration(ttl)
                .putObjectRequest(put)
                .build();

        var presigned = presigner.presignPutObject(req);
        return new PresignedUpload(
                objectKey,
                presigned.url().toString(),
                clock.instant().plus(ttl));
    }

    @Override
    public byte[] download(String objectKey) {
        GetObjectRequest req = GetObjectRequest.builder()
                .bucket(props.bucket())
                .key(objectKey)
                .build();
        try (ResponseInputStream<GetObjectResponse> stream = s3.getObject(req)) {
            return stream.readAllBytes();
        } catch (NoSuchKeyException missing) {
            // Caller (ConversationService.generate) maps this to 409
            // CONVERSATION_NOT_READY — client called /generate before
            // actually PUT-ing the screenshot to the presigned URL.
            log.warn("S3 object {} not found in bucket {}", objectKey, props.bucket());
            throw new IllegalStateException("Screenshot not found in storage: " + objectKey, missing);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Failed reading screenshot " + objectKey + ": " + e.getMessage(), e);
        }
    }
}
