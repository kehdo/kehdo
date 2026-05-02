package app.kehdo.backend.infra.storage;

import java.time.Instant;

/**
 * Result of {@link ScreenshotStorage#presignUpload}. The client PUTs the
 * screenshot bytes directly to {@link #uploadUrl} with
 * {@code Content-Type: image/*} before {@code uploadExpiresAt}.
 *
 * @param objectKey       opaque storage key the backend persists on
 *                        {@code conversations.screenshot_object_key}; never
 *                        exposed to the client
 * @param uploadUrl       short-lived signed PUT URL the client uploads to
 * @param uploadExpiresAt absolute expiry of the signed URL
 */
public record PresignedUpload(
        String objectKey,
        String uploadUrl,
        Instant uploadExpiresAt) {
}
