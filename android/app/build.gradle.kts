plugins {
    alias(libs.plugins.kehdo.android.application)
    alias(libs.plugins.kehdo.android.compose)
    alias(libs.plugins.kehdo.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "app.kehdo.android"

    defaultConfig {
        applicationId = "app.kehdo.android"
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            manifestPlaceholders["BUILD_TYPE"] = "debug"
            manifestPlaceholders["SENTRY_DSN"] = ""
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
            manifestPlaceholders["BUILD_TYPE"] = "release"
            // Sentry DSN is injected by CI (e.g. via `-PSENTRY_DSN=...` or env). Empty disables Sentry.
            manifestPlaceholders["SENTRY_DSN"] =
                (project.findProperty("SENTRY_DSN") as String?) ?: ""
        }
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core modules
    implementation(project(":core:common"))
    implementation(project(":core:ui"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:analytics"))

    // Data modules — only the :app module wires data implementations
    implementation(project(":data:auth"))
    implementation(project(":data:conversation"))
    implementation(project(":data:user"))

    // Feature modules
    implementation(project(":feature:onboarding"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:home"))
    implementation(project(":feature:upload"))
    implementation(project(":feature:reply"))
    implementation(project(":feature:history"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:paywall"))

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)

    // Sentry
    implementation(libs.sentry.android)

    // Tests
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.espresso)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}
