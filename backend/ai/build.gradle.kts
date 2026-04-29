plugins { id("java-library") }
dependencies {
    implementation(project(":common"))
    implementation(project(":infra"))
    implementation("org.springframework.boot:spring-boot-starter:3.2.2")
    implementation("org.springframework.boot:spring-boot-starter-web:3.2.2")
    implementation("io.github.resilience4j:resilience4j-spring-boot3:2.2.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.2")
}
