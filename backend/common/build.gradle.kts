plugins { id("java-library") }
dependencies {
    // Jackson annotations are exposed on the public API of `error.*` records,
    // so consumers can compile against them without redeclaring this dep.
    api("com.fasterxml.jackson.core:jackson-annotations:2.15.3")
    implementation("org.springframework.boot:spring-boot-starter:3.2.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.2")
}
