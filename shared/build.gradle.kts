plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
}

android {
    namespace = "com.cocktailcraft"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
    
    androidTarget()
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }
    
    applyDefaultHierarchyTemplate()

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
                implementation(libs.multiplatform.settings)

                // DI
                implementation(libs.koin.core)
                
                // Connectivity
                implementation(libs.connectivity.core)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.ktor.client.android)
                implementation(libs.androidx.datastore.preferences)
                implementation(libs.multiplatform.settings)
                implementation("com.russhwolf:multiplatform-settings-datastore:1.1.1")
                implementation("com.russhwolf:multiplatform-settings-coroutines:1.1.1")
                implementation(libs.connectivity.android)
            }
        }

        val iosMain by getting {
            dependencies {
                implementation(libs.ktor.client.darwin)
                implementation(libs.connectivity.apple)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.koin.test)
                implementation(libs.turbine)
            }
        }
        
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.mockk)
            }
        }
    }
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}
