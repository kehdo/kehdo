plugins {
    alias(libs.plugins.kehdo.android.library)
    alias(libs.plugins.kehdo.android.hilt)
}

android {
    namespace = "app.kehdo.core.datastore"
}

dependencies {
    api(project(":core:common"))
    api(libs.datastore.preferences)
    implementation(libs.security.crypto)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
