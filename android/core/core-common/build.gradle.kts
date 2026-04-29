plugins {
    alias(libs.plugins.kehdo.android.library)
}

android {
    namespace = "app.kehdo.core.common"
}

dependencies {
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
