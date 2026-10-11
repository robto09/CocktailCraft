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
    versionCatalogs {
        create("libs") {
            from(files("libraries.toml"))
        }
    }
}

rootProject.name = "CocktailCraft"
include(":androidApp")
include(":shared")
include(":core:resources")
include(":core:designsystem")
include(":core:common")
include(":core:domain")
include(":core:testing")
include(":baselineprofile")
