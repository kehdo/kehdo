plugins {
    alias(libs.plugins.kehdo.android.library)
    alias(libs.plugins.kehdo.android.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "app.kehdo.core.database"
}

dependencies {
    api(project(":core:common"))
    api(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
