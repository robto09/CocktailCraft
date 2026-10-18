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
            export(project(":presentation"))
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
                // Everything Android and Swift consume — api(), which export()
                // also requires: the shared ViewModels and UiState
                // (:presentation), models/repository interfaces/validators
                // (:core:domain), Result/ErrorHandler/NetworkMonitor
                // (:core:common), strings (MR) and design tokens
                api(project(":presentation"))
                api(project(":core:domain"))
                api(project(":core:common"))
                api(project(":core:resources"))
                api(project(":core:designsystem"))
                // Repository implementations, database, network, platform
                // drivers: wired into Koin here, invisible to both apps
                implementation(project(":core:data"))

                // initKoin/KoinHelper assemble the per-layer Koin modules
                implementation(libs.koin.core)
                // configureLogging sets the global Kermit floor at app init
                implementation(libs.kermit)
            }
        }

        // iOS test mode and Debug hooks (iosMain/IosTestMode.kt, KoinIOS.kt):
        // the stand-in storage (an in-memory database, throwaway
        // NSUserDefaults suites) and the MockEngine HTTP client that
        // doInitKoinForTesting() binds over the platform ones, plus the
        // HttpSend hook behind the Debug "simulate network failure" toggle
        iosMain.dependencies {
            implementation(libs.multiplatform.settings.core)
            implementation(libs.sqldelight.native.driver)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.mock)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
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

                // SharedStringsTest maps a Ktor IOException to its copy
                implementation(libs.ktor.client.core)
            }
        }
    }
}
