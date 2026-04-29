plugins {
    alias(libs.plugins.kehdo.android.library)
    alias(libs.plugins.kehdo.android.hilt)
}

android {
    namespace = "app.kehdo.data.user"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":domain:user"))

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
}
