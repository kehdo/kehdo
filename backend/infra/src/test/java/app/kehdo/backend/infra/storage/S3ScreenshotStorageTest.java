package app.kehdo.backend.infra.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class S3ScreenshotStorageTest {

    private static final Instant NOW = Instant.parse("2026-05-03T12:00:00Z");

    private S3Client s3;
    private S3Presigner presigner;
    private S3ScreenshotStorage storage;

    @BeforeEach
    void setUp() throws Exception {
        s3 = mock(S3Client.class);
        presigner = mock(S3Presigner.class);
        S3Properties props = new S3Properties(
                "http://localhost:9000",
                "us-east-1",
                "kehdo-screenshots-dev",
                "minioadmin",
                "minioadmin",
                /* presignedUrlTtlMinutes */ 5,
                /* forcePathStyle */ true);
        storage = new S3ScreenshotStorage(s3, presigner, props,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void presign_upload_constructs_object_key_from_conversation_id_and_returns_signed_url() throws Exception {
        UUID conversationId = UUID.fromString("01939e6c-0001-7a4f-8000-000000000001");
        URL fakeUrl = new URL("https://localhost:9000/kehdo-screenshots-dev/conversations/" +
                conversationId + "/screenshot.png?X-Amz-Signature=abc");
        PresignedPutObjectRequest presigned = mock(PresignedPutObjectRequest.class);
        when(presigned.url()).thenReturn(fakeUrl);
        when(presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presigned);

        PresignedUpload upload = storage.presignUpload(conversationId);

        assertThat(upload.objectKey())
                .isEqualTo("conversations/" + conversationId + "/screenshot.png");
        assertThat(upload.uploadUrl()).contains("X-Amz-Signature=abc");
        // Expiry = NOW + 5 minutes (the configured TTL).
        assertThat(upload.uploadExpiresAt()).isEqualTo(NOW.plusSeconds(300));

        // Verify the presign call carries the right bucket + key + content-type.
        ArgumentCaptor<PutObjectPresignRequest> captor =
                ArgumentCaptor.forClass(PutObjectPresignRequest.class);
        verify(presigner).presignPutObject(captor.capture());
        var put = captor.getValue().putObjectRequest();
        assertThat(put.bucket()).isEqualTo("kehdo-screenshots-dev");
        assertThat(put.key()).isEqualTo("conversations/" + conversationId + "/screenshot.png");
        assertThat(put.contentType()).isEqualTo("image/png");
    }

    @Test
    void download_returns_bytes_from_s3_get_object() {
        byte[] payload = new byte[]{1, 2, 3, 4, 5};
        ResponseInputStream<GetObjectResponse> stream = new ResponseInputStream<>(
                GetObjectResponse.builder().build(),
                AbortableInputStream.create(new ByteArrayInputStream(payload)));
        when(s3.getObject(any(GetObjectRequest.class))).thenReturn(stream);

        byte[] result = storage.download("conversations/x/screenshot.png");

        assertThat(result).isEqualTo(payload);
        ArgumentCaptor<GetObjectRequest> captor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3).getObject(captor.capture());
        assertThat(captor.getValue().bucket()).isEqualTo("kehdo-screenshots-dev");
        assertThat(captor.getValue().key()).isEqualTo("conversations/x/screenshot.png");
    }

    @Test
    void download_throws_illegal_state_when_object_missing() {
        when(s3.getObject(any(GetObjectRequest.class))).thenThrow(
                NoSuchKeyException.builder()
                        .awsErrorDetails(AwsErrorDetails.builder().errorCode("NoSuchKey").build())
                        .build());

        assertThatThrownBy(() -> storage.download("missing/key.png"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Screenshot not found in storage");
    }
}
