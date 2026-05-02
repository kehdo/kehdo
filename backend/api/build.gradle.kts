plugins { id("java-library") }
dependencies {
    implementation(project(":common"))
    implementation(project(":auth"))
    implementation(project(":user"))
    implementation(project(":conversation"))
    implementation(project(":ai"))
    implementation(project(":infra"))

    implementation("org.springframework.boot:spring-boot-starter:3.2.2")
    implementation("org.springframework.boot:spring-boot-starter-web:3.2.2")
    implementation("org.springframework.boot:spring-boot-starter-validation:3.2.2")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.2.2")
    implementation("org.springframework.security:spring-security-core:6.2.1")

    testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.2")
}
