plugins { id("java-library") }
dependencies {
    implementation(project(":common"))
    implementation("org.springframework.boot:spring-boot-starter:3.2.2")
    implementation("org.springframework.boot:spring-boot-starter-web:3.2.2")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.2.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.2")
}
