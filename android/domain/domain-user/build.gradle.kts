plugins {
    alias(libs.plugins.kehdo.jvm.library)
}

dependencies {
    api(project(":core:common"))
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
}
