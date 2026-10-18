// The data layer: repository implementations, the Ktor remote source with
// its DTOs/mappers, the SQLDelight database (offline cache + cart, orders,
// favorites, reviews), Settings and the secure auth store, the platform
// NetworkMonitors, and the Koin modules wiring them (networkModule,
// dataModule, dataPlatformModule()). Only :shared depends on it, with
// implementation(): neither the apps nor :presentation see these types, and
// the framework does not export them.
plugins {
    id("cocktailcraft.kmp.library")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
    // No resources of its own: copies :core:resources' strings bundle next to
    // the iOS test executable, which MR needs to initialize
    alias(libs.plugins.moko.resources)
}

// Local storage: the offline cocktail cache (see CocktailCache) and the
// user's cart, orders, favorites and reviews (see LocalDatabase). Database
// name, package, schema, migrations and snapshots are unchanged from the
// single-module layout, so existing installs open their database as-is.
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

kotlin {
    android {
        lint {
            // AndroidNetworkMonitor: ACCESS_NETWORK_STATE is declared by androidApp
            disable.add("MissingPermission")
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Implements the domain's repository interfaces and AppConfig
            api(project(":core:domain"))

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.multiplatform.settings.core)
            implementation(libs.koin.core)
            implementation(libs.kermit)
            // Query.asFlow() for the reactive cart/orders/favorites/reviews reads
            implementation(libs.sqldelight.coroutines.extensions)
        }

        androidMain.dependencies {
            // OkHttp engine: its client is shared with Coil (one connection
            // pool and TLS session cache for the API and image traffic)
            implementation(libs.ktor.client.okhttp)
            // Keystore-backed EncryptedSharedPreferences for the auth store
            implementation(libs.androidx.security.crypto)
            implementation(libs.sqldelight.android.driver)
        }

        // iosMain comes from the default Kotlin hierarchy template
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native.driver)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(project(":core:testing"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.koin.test.core)
            implementation(libs.turbine)
            implementation(libs.multiplatform.settings.test)
            // MockEngine so CocktailApiImplTest exercises the real Ktor
            // pipeline (AR-5)
            implementation(libs.ktor.client.mock)
        }
    }
}
