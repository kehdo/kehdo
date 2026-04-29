plugins {
    alias(libs.plugins.kehdo.android.feature)
}

android {
    namespace = "app.kehdo.feature.paywall"
}

dependencies {
    implementation(project(":domain:auth"))
    implementation(project(":domain:conversation"))
    implementation(project(":domain:user"))
}
