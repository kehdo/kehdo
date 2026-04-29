plugins {
    alias(libs.plugins.kehdo.android.library)
    alias(libs.plugins.kehdo.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "app.kehdo.core.network"
}

dependencies {
    api(project(":core:common"))
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
