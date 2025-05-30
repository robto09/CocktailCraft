pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("versions.toml"))
        }
    }
}

rootProject.name = "CocktailCraft"
include(":androidApp")
include(":shared")
