pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
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
