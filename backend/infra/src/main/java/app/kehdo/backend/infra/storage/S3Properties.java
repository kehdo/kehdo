package app.kehdo.backend.infra.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Strongly-typed view of the {@code kehdo.storage.s3.*} block in
 * {@code application.yml}. Read by {@link S3StorageConfig}.
 *
 * @param endpoint                 base URL of the S3-compatible service.
 *                                 For real AWS S3 this is null/empty
 *                                 (the SDK derives it from region); for
 *                                 MinIO / Cloudflare R2 it's an explicit
 *                                 host like {@code http://localhost:9000}.
 * @param region                   AWS region. Required even for MinIO —
 *                                 the SDK uses it to compute presigned-URL
 *                                 signatures. {@code us-east-1} is fine
 *                                 for MinIO.
 * @param bucket                   bucket name screenshots live in
 * @param accessKey                S3 access key
 * @param secretKey                S3 secret key
 * @param presignedUrlTtlMinutes   how long a presigned PUT URL is valid;
 *                                 short by design — the client uploads
 *                                 immediately after receiving it
 * @param forcePathStyle           {@code true} for MinIO (and required
 *                                 for old-style hostnames); {@code false}
 *                                 for AWS S3 / Cloudflare R2 which use
 *                                 virtual-hosted-style URLs by default
 */
@ConfigurationProperties(prefix = "kehdo.storage.s3")
public record S3Properties(
        String endpoint,
        String region,
        String bucket,
        String accessKey,
        String secretKey,
        long presignedUrlTtlMinutes,
        boolean forcePathStyle) {
}
