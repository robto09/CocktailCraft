// Convention plugins for the KMP library modules (AR-10). The plugin APIs are
// compileOnly here: the real AGP/KGP classes come from the root build, which
// puts them on the classpath via `apply false` in the root build.gradle.kts.
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../libraries.toml"))
        }
    }
}

rootProject.name = "build-logic"
include(":convention")
