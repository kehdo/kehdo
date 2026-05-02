plugins { id("java-library") }
dependencies {
    implementation(project(":common"))
    implementation(project(":infra"))
    implementation("org.springframework.boot:spring-boot-starter:3.2.2")
    implementation("org.springframework.boot:spring-boot-starter-web:3.2.2")
    implementation("io.github.resilience4j:resilience4j-spring-boot3:2.2.0")

    // Google Cloud SDKs — OCR (PR 6) + Vertex AI Gemini (PR 7).
    // The libraries-bom keeps all google-cloud-* artifacts on a consistent
    // generation so we don't pin individual versions and risk diamond conflicts.
    implementation(platform("com.google.cloud:libraries-bom:26.39.0"))
    implementation("com.google.cloud:google-cloud-vision")
    implementation("com.google.cloud:google-cloud-vertexai")

    testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.2")
}
