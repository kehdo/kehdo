plugins {
    alias(libs.plugins.kehdo.android.library)
    alias(libs.plugins.kehdo.android.hilt)
}

android {
    namespace = "app.kehdo.data.conversation"

    // Surface the kehdo.useFakeData gradle.properties flag so the Hilt
    // module can pick between Fake and Real at injection time.
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
    implementation(project(":domain:conversation"))
    implementation(libs.okhttp)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
}
