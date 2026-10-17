// Test doubles shared by several modules' test suites (commonTest only —
// never a main dependency): fake repositories and NetworkMonitor over the
// domain/common contracts, test data builders, MainDispatcherTest, and a
// schema-agnostic in-memory SQLite driver. Data-layer doubles
// (FakeCocktailApi, the LocalDatabase helpers) stay in :core:data's own
// tests because they need its internals.
plugins {
    id("cocktailcraft.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // The fakes implement the domain's repository interfaces
            api(project(":core:domain"))
            // MainDispatcherTest's @BeforeTest/@AfterTest and test dispatcher
            api(kotlin("test"))
            api(libs.kotlinx.coroutines.test)
            api(libs.sqldelight.runtime)
        }
        androidMain.dependencies {
            // The JVM actuals of kotlin.test's annotations — the JUnit4 flavor
            // the Android host tests already run on
            api(kotlin("test-junit"))
            // In-memory SQLite on the host JVM
            implementation(libs.sqldelight.sqlite.driver)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
    }
}
