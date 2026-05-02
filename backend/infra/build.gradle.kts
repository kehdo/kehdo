plugins { id("java-library") }
dependencies {
    implementation(project(":common"))
    implementation("org.springframework.boot:spring-boot-starter:3.2.2")
    implementation("org.springframework.boot:spring-boot-starter-web:3.2.2")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.2.2")

    // AWS SDK v2 — speaks the S3 protocol so MinIO / Cloudflare R2 / real
    // AWS S3 all work with the same client, distinguished only by
    // endpointOverride + credentials (Phase 4 PR 11 — `feat/be/s3-storage`).
    implementation(platform("software.amazon.awssdk:bom:2.25.50"))
    implementation("software.amazon.awssdk:s3")

    testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.2")
}
