plugins { id("java-library") }
dependencies {
    implementation(project(":common"))
    implementation(project(":user"))

    implementation("org.springframework.boot:spring-boot-starter:3.2.2")
    implementation("org.springframework.boot:spring-boot-starter-web:3.2.2")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.2.2")
    // BCrypt PasswordEncoder + crypto utilities; the full security starter
    // (filter chain, etc.) lives in :app and isn't needed here.
    implementation("org.springframework.security:spring-security-crypto:6.2.1")
    // Authentication primitives + SecurityContextHolder for the
    // JwtAuthenticationFilter. spring-security-web brings the OncePerRequestFilter
    // base class.
    implementation("org.springframework.security:spring-security-core:6.2.1")
    implementation("org.springframework.security:spring-security-web:6.2.1")

    // RS256 JWT issuance + validation
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")

    testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.2")
}
