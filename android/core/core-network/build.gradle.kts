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
        // Debug → emulator's host loopback (local backend on :8080).
        buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8080/v1/\"")
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
