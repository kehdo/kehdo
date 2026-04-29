plugins {
    alias(libs.plugins.kehdo.android.library)
    alias(libs.plugins.kehdo.android.compose)
}

android {
    namespace = "app.kehdo.core.ui"
}

dependencies {
    api(project(":core:common"))
    api(libs.compose.material3)
    api(libs.compose.material.icons.extended)
    implementation(libs.coil.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
