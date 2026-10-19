// Cross-layer basics every other module needs: the Result/ErrorCode/
// DomainException error pipeline, ErrorHandler's exception ->
// user-facing error mapping, the NetworkMonitor contract, and UUID.
// Package names predate the split (Result stays in domain.util), only the
// module boundary is new.
plugins {
    id("cocktailcraft.kmp.library")
    // No resources of its own: copies :core:resources' strings bundle next to
    // the iOS test executable, which MR needs to initialize
    alias(libs.plugins.moko.resources)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // ErrorHandler's copy is StringDesc/MR; NetworkMonitor exposes StateFlow
            api(project(":core:resources"))
            api(libs.kotlinx.coroutines.core)
            // ErrorHandler classifies Ktor's network/HTTP exceptions and
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
