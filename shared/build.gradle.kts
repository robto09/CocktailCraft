plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
    kotlin("native.cocoapods")
    id("co.touchlab.skie") version "0.10.4"
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

    lint {
        disable.add("MissingPermission")
    }
}

kotlin {
    jvmToolchain(17)
    
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    
    cocoapods {
        version = "1.0.0"
        summary = "CocktailCraft Shared Module"
        homepage = "https://github.com/cocktailcraft/shared"
        ios.deploymentTarget = "18.5"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "shared"
            isStatic = true
        }
    }



    sourceSets {
        val commonMain by getting {
            dependencies {
                // Ktor
                implementation("io.ktor:ktor-client-core:2.0.0")
                implementation("io.ktor:ktor-client-content-negotiation:2.0.0")
                implementation("io.ktor:ktor-client-logging:2.0.0")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.0.0")

                // Serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

                // DateTime
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")

                // Settings
                implementation("com.russhwolf:multiplatform-settings:1.1.1")

                // DI
                implementation("io.insert-koin:koin-core:3.5.6")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

                
                // Multiplatform logging
                implementation("co.touchlab:kermit:2.0.2")

                // No external caching library needed - using custom implementation
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-android:2.0.0")
                implementation("androidx.datastore:datastore-preferences:1.0.0")
                implementation("com.russhwolf:multiplatform-settings:1.1.1")
                implementation("com.russhwolf:multiplatform-settings-datastore:1.1.1")
                implementation("com.russhwolf:multiplatform-settings-coroutines:1.1.1")
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)

            dependencies {
                implementation("io.ktor:ktor-client-darwin:2.0.0")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
                implementation("io.insert-koin:koin-test:3.5.6")
                implementation("app.cash.turbine:turbine:0.13.0")
            }
        }

        val androidUnitTest by getting {
            dependencies {
                implementation("io.mockk:mockk:1.13.5")
            }
        }
    }
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}
