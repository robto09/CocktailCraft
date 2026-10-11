plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.kotlinMultiplatformLibrary)
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
    }
}

kotlin {
    jvmToolchain(libs.versions.jvmTarget.get().toInt())

    sourceSets.all {
        // For @HiddenFromObjC on serializer companions
        languageSettings.optIn("kotlin.experimental.ExperimentalObjCRefinement")
        // kotlin.time.Clock/Instant (kotlinx-datetime 0.7+) — stable in Kotlin 2.3
        languageSettings.optIn("kotlin.time.ExperimentalTime")
    }

    android {
        namespace = "com.cocktailcraft"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = 24

        // Host-side (JVM) unit tests, equivalent of the old androidUnitTest
        withHostTestBuilder {}.configure {
            // moko-resources strings resolve through the generated Android R
            // class, so host tests need the merged resources
            isIncludeAndroidResources = true
        }

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
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
            // Silences the "cannot infer a bundle ID" link warning.
            binaryOption("bundleId", "com.cocktailcraft.shared")
            // StringResource/StringDesc and their localized() helpers are
            // used directly from Swift (see Strings.swift in the iOS app).
            export(libs.moko.resources)
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
            // Shared strings: the generated MR accessors (StringResource) and
            // StringDesc are part of the shared API, and the framework
            // exports them for Swift.
            api(libs.moko.resources)

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

// Single source for the app's user-facing strings: base (English) and
// per-language strings.xml/plurals.xml under src/commonMain/moko-resources.
// Generates MR.strings / MR.plurals, Android string resources in this
// module's R class, and Localizable.strings(dict) in a resource bundle that
// the iOS app copies in (copyFrameworkResourcesToApp, run by the "Compile
// Kotlin Framework" phase in iosApp/project.yml). See README "Localization".
multiplatformResources {
    resourcesPackage.set("com.cocktailcraft.resources")
    iosBaseLocalizationRegion.set("en")
}
