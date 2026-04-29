plugins {
    alias(libs.plugins.kehdo.android.library)
    alias(libs.plugins.kehdo.android.hilt)
}

android {
    namespace = "app.kehdo.core.analytics"
}

dependencies {
    api(project(":core:common"))
    implementation(libs.mixpanel)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
