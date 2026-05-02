package app.kehdo.backend.infra.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

/**
 * Placeholder storage adapter. Returns a fake presigned URL pointing at a
 * non-routable host on upload, and a tiny canned 1×1 PNG on download — so
 * the OCR adapter (Phase 4 PR 6) and conversations controller can be
 * exercised before real storage lands in PR 12.
 *
 * <p>Active when no real {@link ScreenshotStorage} bean is registered —
 * Phase 4 PR 12's {@code S3ScreenshotStorage} will out-rank this on the
 * default profile. Tests under {@code test} profile keep this bean.</p>
 */
@Service
@Profile({"stub-storage", "test", "default", "local"})
public class StubScreenshotStorage implements ScreenshotStorage {

    /**
     * Smallest valid PNG (1×1 transparent) — base64-encoded so we can keep
     * it inline. Cloud Vision rejects empty / corrupt bytes; this is just
     * enough to make the SDK happy in dev. Real Vision would obviously
     * find no text in it; the OCR adapter handles "no text detected"
     * gracefully (returns an empty {@code OcrResult}).
     */
    private static final byte[] CANNED_PNG_BYTES = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=");

    private final Clock clock;
    private final Duration ttl;

    public StubScreenshotStorage(
            Clock clock,
            @Value("${kehdo.storage.s3.presigned-url-ttl-minutes:5}") long ttlMinutes) {
        this.clock = clock;
        this.ttl = Duration.ofMinutes(ttlMinutes);
    }

    @Override
    public PresignedUpload presignUpload(UUID conversationId) {
        String objectKey = "conversations/" + conversationId + "/screenshot.png";
        String url = "https://uploads.kehdo.invalid/" + objectKey + "?signature=stub";
        return new PresignedUpload(
                objectKey,
                url,
                clock.instant().plus(ttl));
    }

    @Override
    public byte[] download(String objectKey) {
        return CANNED_PNG_BYTES;
    }
}
