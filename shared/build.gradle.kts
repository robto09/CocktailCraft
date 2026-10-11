plugins {
    // Targets, toolchain, SDK levels, opt-ins, host tests (build-logic, AR-10)
    id("cocktailcraft.kmp.library")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.skie)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.moko.resources)
}

// Local storage: the offline cocktail cache (see CocktailCache) and the
// user's cart, orders, favorites and reviews (see LocalDatabase)
sqldelight {
    databases {
        create("CocktailCraftDatabase") {
            packageName.set("com.cocktailcraft.data.cache.db")
            // Snapshots of each released schema version live here. Any change
            // to the .sq files must ship with a matching .sqm migration, or
            // verifySqlDelightMigration fails (it runs as part of `check`).
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases"))
            verifyMigrations.set(true)
        }
    }
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

        lint {
            disable.add("MissingPermission")
        }
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
            // The framework is static, so the SQLDelight native driver's
            // sqlite3 (and the K/N runtime's libc++) are not linked here: the
            // app links them itself via OTHER_LDFLAGS in project.yml.
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Ktor
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.serialization.kotlinx.json)

                // Serialization
                implementation(libs.kotlinx.serialization.json)

                // Coroutines
                implementation(libs.kotlinx.coroutines.core)

                // DateTime
                implementation(libs.kotlinx.datetime)

                // Settings
                implementation(libs.multiplatform.settings.core)

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
            // Shared strings (MR) and design tokens: part of the API Android
            // and Swift consume, so api() — export() requires it.
            api(project(":core:resources"))
            api(project(":core:designsystem"))

                // Multiplatform logging
                implementation(libs.kermit)

                // Query.asFlow() for the reactive cart/orders/favorites/reviews reads
                implementation(libs.sqldelight.coroutines.extensions)
            }
        }

        val androidMain by getting {
            dependencies {
                // Provides the Android Dispatchers.Main implementation for viewModelScope
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.ktor.client.android)
                implementation(libs.multiplatform.settings.core)
                // Keystore-backed EncryptedSharedPreferences for the auth store
                implementation(libs.androidx.security.crypto)
                implementation(libs.sqldelight.android.driver)
            }
        }

        // iosMain comes from the default Kotlin hierarchy template
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native.driver)
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.koin.test.core)
                implementation(libs.turbine)
                implementation(libs.multiplatform.settings.test)
                // MockEngine so CocktailApiImplTest exercises the real Ktor
                // pipeline (AR-5)
                implementation(libs.ktor.client.mock)
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

                // In-memory SQLite for the cache and repository tests
                implementation(libs.sqldelight.sqlite.driver)
            }
        }


    }
}
