plugins {
    alias(libs.plugins.kehdo.android.feature)
}

android {
    namespace = "app.kehdo.feature.home"
}

dependencies {
    implementation(project(":domain:auth"))
}
