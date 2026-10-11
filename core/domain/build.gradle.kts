// The domain layer: models, repository interfaces (ports), use cases and
// validators, plus domainModule (the use cases' Koin wiring). Depends on
// nothing app-specific below it — no Ktor, SQLDelight, Settings or platform
// drivers; :core:data implements its repository interfaces and
// :presentation consumes its use cases.
plugins {
    id("cocktailcraft.kmp.library")
    // Domain models are @Serializable (the local database stores some as JSON)
    alias(libs.plugins.kotlin.serialization)
    // No resources of its own: copies :core:resources' strings bundle next to
    // the iOS test executable, which MR needs to initialize
    alias(libs.plugins.moko.resources)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Result/ErrorCode appear in every repository signature; also
            // brings :core:resources (StringDesc in DeliveryPolicy) and coroutines
            api(project(":core:common"))
            // User's default accent color is a design token
            implementation(project(":core:designsystem"))
            api(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kermit)
            implementation(libs.koin.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(project(":core:testing"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
