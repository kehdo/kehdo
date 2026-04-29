plugins {
    alias(libs.plugins.kehdo.android.feature)
}

android {
    namespace = "app.kehdo.feature.auth"
}

dependencies {
    implementation(project(":domain:auth"))
    implementation(project(":domain:conversation"))
    implementation(project(":domain:user"))
}
