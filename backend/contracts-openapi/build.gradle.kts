plugins { id("java-library") }
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:3.2.2")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.20")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
}
