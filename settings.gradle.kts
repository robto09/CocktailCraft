pluginManagement {
    // Convention plugins (cocktailcraft.kmp.library) for the KMP modules
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    // The `libs` catalog is gradle/libs.versions.toml, which Gradle picks up
    // by convention (and Dependabot reads from that path only)
}

rootProject.name = "CocktailCraft"
include(":androidApp")
include(":shared")
include(":presentation")
include(":core:resources")
include(":core:designsystem")
include(":core:common")
include(":core:domain")
include(":core:data")
include(":core:testing")
include(":baselineprofile")
