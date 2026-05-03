plugins {
    alias(libs.plugins.kehdo.android.library)
    alias(libs.plugins.kehdo.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "app.kehdo.core.network"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        // Debug API base URL — defaults to the Fly.io staging deployment so
        // every dev build talks to a real backend without anyone having to
        // run Spring Boot locally. Override with -Pkehdo.apiBaseUrl=...
        // to point at the local backend (http://10.0.2.2:8080/v1/) or any
        // other env. Trailing slash required by Retrofit.
        val debugApiBaseUrl = (project.findProperty("kehdo.apiBaseUrl") as? String)
            ?: "https://api.staging.kehdo.app/v1/"
        buildConfigField("String", "API_BASE_URL", "\"$debugApiBaseUrl\"")
    }

    buildTypes {
        getByName("release") {
            buildConfigField("String", "API_BASE_URL", "\"https://api.kehdo.app/v1/\"")
        }
    }
}

dependencies {
    api(project(":core:common"))
    api(project(":core:datastore"))
    api(project(":core:network-generated"))
    api(libs.retrofit)
    api(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
}
