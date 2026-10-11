plugins {
    // Targets, toolchain, SDK levels, opt-ins, host tests (build-logic, AR-10)
    id("cocktailcraft.kmp.library")
    alias(libs.plugins.skie)
    // No resources of its own: links :core:resources' strings bundle into the
    // framework and registers copyFrameworkResourcesToApp for Xcode
    alias(libs.plugins.moko.resources)
}

// Configure SKIE for enhanced Swift interop
skie {
    features {
        group {
            // Core interop features
            co.touchlab.skie.configuration.FlowInterop.Enabled(true)
            co.touchlab.skie.configuration.SuspendInterop.Enabled(true)

            // Enhanced enum support for better Swift integration
            co.touchlab.skie.configuration.EnumInterop.Enabled(true)

            // Sealed class support for Swift enum-like behavior
            co.touchlab.skie.configuration.SealedInterop.Enabled(true)

            // Default arguments support for cleaner Swift APIs
            co.touchlab.skie.configuration.DefaultArgumentInterop.Enabled(true)
        }
        // moko-resources is an external library, but since it reaches the
        // framework through the :core:resources project module SKIE would
        // generate default-argument overloads for its classes too — ~50
        // unused header entries (AR-12). Keep them off, as before the split.
        group("dev.icerock.moko") {
            co.touchlab.skie.configuration.DefaultArgumentInterop.Enabled(false)
        }
    }
}

kotlin {
    android {
        namespace = "com.cocktailcraft"
    }
    // The iOS app consumes this framework through Kotlin's direct Xcode
    // integration (no CocoaPods): the app target's "Compile Kotlin Framework"
    // build phase runs :shared:embedAndSignAppleFrameworkForXcode, which
    // builds the framework for the active Xcode configuration/SDK/arch and
    // drops it in build/xcode-frameworks/ (see iosApp/project.yml). The iOS
    // deployment target lives in project.yml.
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "shared"
            isStatic = true
            // Silences the "cannot infer a bundle ID" link warning.
            binaryOption("bundleId", "com.cocktailcraft.shared")
            // StringResource/StringDesc and their localized() helpers are
            // used directly from Swift (see Strings.swift in the iOS app).
            export(libs.moko.resources)
            // Modules whose types Swift names directly. Exporting keeps their
            // declarations unprefixed in the header (MR, DesignTokens, ...);
            // an unexported module's types would surface module-prefixed.
            export(project(":core:resources"))
            export(project(":core:designsystem"))
            export(project(":core:common"))
            export(project(":core:domain"))
            // The framework is static, so the SQLDelight native driver's
            // sqlite3 (and the K/N runtime's libc++) are not linked here: the
            // app links them itself via OTHER_LDFLAGS in project.yml.
        }
        // The test executable does link: the DI graph tests open an in-memory
        // database (the SQLDelight plugin, now on :core:data, used to add this)
        binaries.withType<org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable>().configureEach {
            linkerOpts("-lsqlite3")
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Shared strings (MR) and design tokens: part of the API Android
                // and Swift consume, so api() — export() requires it.
                api(project(":core:resources"))
                api(project(":core:designsystem"))
                // Result/ErrorCode, ErrorHandler, NetworkMonitor — used from Swift
                api(project(":core:common"))
                // Models, repository interfaces, validators — used from Swift
                api(project(":core:domain"))
                // Repository implementations, database, network, platform
                // drivers: wired into Koin here, invisible to both apps
                implementation(project(":core:data"))

                // DI
                implementation(libs.koin.core)
                implementation(libs.koin.core.viewmodel)
                // VMs extend the multiplatform androidx ViewModel — api so Android
                // consumers see the supertype for koinViewModel scoping
                // Deliberately api(), not implementation() (AR-12): the shared
                // ViewModels extend androidx ViewModel, so the type must be on the
                // consumers' compile classpath; demoting it breaks androidApp.
                // Export-surface trimming happens via @HiddenFromObjC on
                // Kotlin-side helpers instead (see Result.kt, BaseNetworkMonitor).
                api(libs.androidx.lifecycle.viewmodel)

                // Multiplatform logging
                implementation(libs.kermit)
            }
        }

        val androidMain by getting {
            dependencies {
                // Provides the Android Dispatchers.Main implementation for viewModelScope
                implementation(libs.kotlinx.coroutines.android)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(project(":core:testing"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.koin.test.core)
                implementation(libs.turbine)
                // MapSettings stand-ins for the platform Settings bindings
                implementation(libs.multiplatform.settings.core)
                implementation(libs.multiplatform.settings.test)
            }
        }

        val androidHostTest by getting {
            dependencies {
                implementation(libs.mockk.core)
                implementation(libs.robolectric)
                implementation(libs.androidx.test.core)
                implementation(libs.androidx.test.ext.junit)

                // JUnit Jupiter for modern testing (Android only)
                implementation(libs.junit.jupiter.api)
                implementation(libs.junit.jupiter.engine)
                implementation(libs.junit.jupiter.params)

                // SharedStringsTest maps a Ktor IOException to its copy
                implementation(libs.ktor.client.core)
            }
        }
    }
}
