package app.kehdo.backend.infra.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.util.UUID;

/**
 * Phase 4 PR 4 placeholder. Returns a fake presigned URL pointing at a
 * non-routable host so the conversations controller can be exercised
 * end-to-end before real storage lands in PR 12.
 *
 * <p>Active when no real {@link ScreenshotStorage} bean is registered —
 * Phase 4 PR 12's {@code S3ScreenshotStorage} will out-rank this on the
 * default profile. Tests run under the {@code test} profile which keeps
 * this bean.</p>
 */
@Service
@Profile({"stub-storage", "test", "default", "local"})
public class StubScreenshotStorage implements ScreenshotStorage {

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
}
