plugins {
    alias(libs.plugins.kehdo.android.feature)
}

android {
    namespace = "app.kehdo.feature.onboarding"
}

dependencies {
    // Onboarding is currently UI-only; no domain deps yet.
}
