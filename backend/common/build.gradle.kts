plugins { id("java-library") }
dependencies {
    // Jackson annotations are exposed on the public API of `error.*` records,
    // so consumers can compile against them without redeclaring this dep.
    api("com.fasterxml.jackson.core:jackson-annotations:2.15.3")
    // UUID v7 generation — exposed via Ids.newId(), used by every module
    // that creates entities. `api` so consumers don't need to redeclare.
    api("com.github.f4b6a3:uuid-creator:5.3.7")
    implementation("org.springframework.boot:spring-boot-starter:3.2.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.2")
}
