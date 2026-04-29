plugins {
    alias(libs.plugins.kehdo.android.library)
}

android {
    namespace = "app.kehdo.core.testing"
}

dependencies {
    api(project(":core:common"))
    api(libs.junit)
    api(libs.mockk)
    api(libs.truth)
    api(libs.turbine)
    api(libs.kotlinx.coroutines.test)
}
