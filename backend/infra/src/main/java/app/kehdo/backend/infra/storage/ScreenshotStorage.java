package app.kehdo.backend.infra.storage;

import java.util.UUID;

/**
 * Object-storage adapter for screenshot uploads + downloads. Used by the
 * conversations controller (Phase 4 PR 4) to mint short-lived presigned
 * PUT URLs, and by the OCR adapter (Phase 4 PR 6) to read bytes back into
 * Cloud Vision.
 *
 * <p>Implementations:
 * <ul>
 *   <li>{@code StubScreenshotStorage} — Phase 4 PR 4 — returns a fake URL
 *       and serves a tiny canned PNG for download so the OCR adapter can
 *       be exercised before real storage credentials.</li>
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

    /**
     * Fetch the raw image bytes the client uploaded. Used by the OCR
     * adapter to feed the screenshot into Cloud Vision.
     *
     * @throws IllegalStateException when the object is missing — callers
     *         (conversations service) map that to
     *         {@code 409 CONVERSATION_NOT_READY}.
     */
    byte[] download(String objectKey);
}
