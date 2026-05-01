@file:Suppress("UnstableApiUsage")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // Auto-provisions a JDK 17 toolchain when the host doesn't have one.
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "kehdo-android"

// App module
include(":app")

// Core modules
include(":core:common")
include(":core:ui")
include(":core:network")
include(":core:network-generated")
include(":core:database")
include(":core:datastore")
include(":core:analytics")
include(":core:testing")

// Domain modules (pure Kotlin)
include(":domain:auth")
include(":domain:conversation")
include(":domain:user")

// Data modules
include(":data:auth")
include(":data:conversation")
include(":data:user")

// Feature modules
include(":feature:onboarding")
include(":feature:auth")
include(":feature:home")
include(":feature:upload")
include(":feature:reply")
include(":feature:history")
include(":feature:profile")
include(":feature:paywall")

// Set custom project paths matching folder layout
project(":core:common").projectDir = file("core/core-common")
project(":core:ui").projectDir = file("core/core-ui")
project(":core:network").projectDir = file("core/core-network")
project(":core:network-generated").projectDir = file("core/core-network-generated")
project(":core:database").projectDir = file("core/core-database")
project(":core:datastore").projectDir = file("core/core-datastore")
project(":core:analytics").projectDir = file("core/core-analytics")
project(":core:testing").projectDir = file("core/core-testing")

project(":domain:auth").projectDir = file("domain/domain-auth")
project(":domain:conversation").projectDir = file("domain/domain-conversation")
project(":domain:user").projectDir = file("domain/domain-user")

project(":data:auth").projectDir = file("data/data-auth")
project(":data:conversation").projectDir = file("data/data-conversation")
project(":data:user").projectDir = file("data/data-user")

project(":feature:onboarding").projectDir = file("feature/feature-onboarding")
project(":feature:auth").projectDir = file("feature/feature-auth")
project(":feature:home").projectDir = file("feature/feature-home")
project(":feature:upload").projectDir = file("feature/feature-upload")
project(":feature:reply").projectDir = file("feature/feature-reply")
project(":feature:history").projectDir = file("feature/feature-history")
project(":feature:profile").projectDir = file("feature/feature-profile")
project(":feature:paywall").projectDir = file("feature/feature-paywall")
