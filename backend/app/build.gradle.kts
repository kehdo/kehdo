plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    java
}

// Override the Flyway version Spring Boot 3.2.2 brings (9.x) so we get the
// modular `flyway-database-postgresql` artifact required from Flyway 10+,
// per backend/CLAUDE.md which locks the migration tool at Flyway 10.x.
extra["flyway.version"] = "10.7.1"

dependencies {
    implementation(project(":api"))
    implementation(project(":auth"))
    implementation(project(":user"))
    implementation(project(":conversation"))
    implementation(project(":ai"))
    implementation(project(":infra"))
    implementation(project(":common"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.flywaydb:flyway-core")
    // flyway-database-postgresql is a separate artifact from Flyway 10+ and
    // isn't in spring-boot-dependencies 3.2.2's BOM; pin explicitly.
    implementation("org.flywaydb:flyway-database-postgresql:10.7.1")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testImplementation("org.testcontainers:postgresql:1.19.3")
}
