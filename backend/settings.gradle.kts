rootProject.name = "kehdo-backend"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

include(
    ":app",
    ":api",
    ":auth",
    ":user",
    ":conversation",
    ":ai",
    ":infra",
    ":common",
    ":contracts-openapi"
)

// Enable build cache + parallel builds
gradle.startParameter.isBuildCacheEnabled = true
