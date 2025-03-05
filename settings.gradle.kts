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
            from(files("libraries.toml"))
        }
    }
}

rootProject.name = "BreadApp2"
include(":androidApp")
include(":shared")
