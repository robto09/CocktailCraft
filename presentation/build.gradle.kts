// The shared ViewModels and their UiState classes, plus presentationModule
// (their Koin wiring). Sees the domain (use cases, repository interfaces)
// and :core:common, never :core:data: the repositories arrive through Koin.
plugins {
    id("cocktailcraft.kmp.library")
    // No resources of its own: copies :core:resources' strings bundle next to
    // the iOS test executable, which MR needs to initialize
    alias(libs.plugins.moko.resources)
}

kotlin {
    // The ViewModel integration tests open an in-memory database through
    // :core:data; the SQLDelight native driver needs sqlite3 at link time
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.withType<org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable>().configureEach {
            linkerOpts("-lsqlite3")
        }
    }

    sourceSets {
        commonMain.dependencies {
            // ViewModel state and inputs are domain models; brings
            // :core:common (ErrorHandler, NetworkMonitor) and :core:resources
            api(project(":core:domain"))
            // ThemeViewModel maps accent/color tokens
            implementation(project(":core:designsystem"))
            // Deliberately api(), not implementation() (AR-12): the shared
            // ViewModels extend androidx ViewModel, so the type must be on the
            // consumers' compile classpath; demoting it breaks androidApp.
            // Export-surface trimming happens via @HiddenFromObjC on
            // Kotlin-side helpers instead (see Result.kt, BaseNetworkMonitor).
            api(libs.androidx.lifecycle.viewmodel)
            implementation(libs.koin.core)
            implementation(libs.koin.core.viewmodel)
            implementation(libs.kermit)
        }

        androidMain.dependencies {
            // Provides the Android Dispatchers.Main implementation for viewModelScope
            implementation(libs.kotlinx.coroutines.android)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(project(":core:testing"))
            // Test scope only: the Profile/Theme/Review ViewModel tests run
            // against the real repositories (see testutil/DataLayer.kt). Main
            // code cannot see the data layer.
            implementation(project(":core:data"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.koin.core)
            implementation(libs.turbine)
            implementation(libs.multiplatform.settings.core)
            implementation(libs.multiplatform.settings.test)
        }
    }
}
