package app.kehdo.backend.infra.storage;

import java.util.UUID;

/**
 * Object-storage adapter for screenshot uploads. Used by the conversations
 * controller (Phase 4 PR 4) to mint short-lived presigned PUT URLs.
 *
 * <p>Implementations:
 * <ul>
 *   <li>{@code StubScreenshotStorage} — Phase 4 PR 4 — returns a fake URL
 *       so the controller can be wired before real storage credentials.</li>
 *   <li>{@code S3ScreenshotStorage} — Phase 4 PR 12 — real S3 / R2 /
 *       MinIO; uses the {@code kehdo.storage.s3.*} config block.</li>
 * </ul>
 */
public interface ScreenshotStorage {

    /**
     * Mint a presigned PUT URL for the given conversation. The returned
     * {@link PresignedUpload#objectKey()} is what the backend persists;
     * the client only ever sees {@link PresignedUpload#uploadUrl()}.
     */
    PresignedUpload presignUpload(UUID conversationId);
}
