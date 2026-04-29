plugins {
    alias(libs.plugins.kehdo.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "app.kehdo.core.network.generated"
}

// TODO: wire openapi-generator Gradle plugin here in Phase 0
// ./tools/generate-clients.sh writes Retrofit interfaces into src/main/kotlin/

dependencies {
    api(libs.retrofit)
    api(libs.kotlinx.serialization.json)
}
