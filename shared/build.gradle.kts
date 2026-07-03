plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    kotlin("native.cocoapods")
    alias(libs.plugins.skie)
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

    sourceSets.all {
        // For @HiddenFromObjC on serializer companions
        languageSettings.optIn("kotlin.experimental.ExperimentalObjCRefinement")
    }

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

                // Multiplatform logging
                implementation(libs.kermit)

                // No external caching library needed - using custom implementation
            }
        }

        val androidMain by getting {
            dependencies {
                // Provides the Android Dispatchers.Main implementation for viewModelScope
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.ktor.client.android)
                implementation(libs.androidx.datastore.preferences)
                implementation(libs.multiplatform.settings.core)
                implementation(libs.multiplatform.settings.datastore)
                implementation(libs.multiplatform.settings.coroutines)
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
                implementation(libs.ktor.client.darwin)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.koin.test.core)
                implementation(libs.turbine)
            }
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(libs.mockk.core)
                implementation(libs.robolectric)
                implementation(libs.androidx.test.core)
                implementation(libs.androidx.test.ext.junit)

                // JUnit Jupiter for modern testing (Android only)
                implementation(libs.junit.jupiter.api)
                implementation(libs.junit.jupiter.engine)
                implementation(libs.junit.jupiter.params)
            }
        }


    }
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}
