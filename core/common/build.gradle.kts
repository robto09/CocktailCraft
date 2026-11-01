// Cross-layer basics every other module needs: the Result/ErrorCode/
// DomainException error pipeline, ErrorClassifier's exception -> error-kind
// mapping, the NetworkMonitor contract, and UUID. No display copy: the
// user-facing wording (ErrorHandler) lives in :presentation.
// Package names predate the split (Result stays in domain.util), only the
// module boundary is new.
plugins {
    id("cocktailcraft.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // NetworkMonitor exposes StateFlow
            api(libs.kotlinx.coroutines.core)
            // ErrorClassifier classifies Ktor's network/HTTP exceptions and
            // serialization failures by type. implementation(), so Ktor stays
            // off the compile classpath of :core:domain and :presentation.
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
