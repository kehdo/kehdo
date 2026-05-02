plugins {
    alias(libs.plugins.kehdo.android.library)
    alias(libs.plugins.kehdo.android.hilt)
}

android {
    namespace = "app.kehdo.data.user"

    // Surface the kehdo.useFakeData gradle.properties flag so [UserModule]
    // can pick between Fake and Real bindings — same flag as
    // :data:conversation, so they switch together.
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        val useFakeData = (project.findProperty("kehdo.useFakeData") as? String)
            ?.toBooleanStrictOrNull() ?: true
        buildConfigField("boolean", "USE_FAKE_DATA", useFakeData.toString())
    }
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
