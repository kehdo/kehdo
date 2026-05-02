plugins {
    alias(libs.plugins.kehdo.jvm.library)
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    // javax.inject lets domain use-cases declare @Inject without pulling in Hilt
    // (which is Android-only). Hilt provides javax.inject transitively in :app.
    api(libs.javax.inject)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
