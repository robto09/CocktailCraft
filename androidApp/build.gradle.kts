import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.baselineprofile)
}

// Release signing comes from an untracked keystore.properties at the repo root
// (see keystore.properties.example) or, on CI, from ANDROID_RELEASE_* env vars.
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun releaseSigningValue(property: String, envVar: String): String? =
    keystoreProperties.getProperty(property) ?: System.getenv(envVar)

val releaseStoreFile = releaseSigningValue("storeFile", "ANDROID_RELEASE_STORE_FILE")

android {
    namespace = "com.cocktailcraft.android"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.cocktailcraft.android"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        if (releaseStoreFile != null) {
            create("release") {
                storeFile = rootProject.file(releaseStoreFile)
                storePassword = releaseSigningValue("storePassword", "ANDROID_RELEASE_STORE_PASSWORD")
                keyAlias = releaseSigningValue("keyAlias", "ANDROID_RELEASE_KEY_ALIAS")
                keyPassword = releaseSigningValue("keyPassword", "ANDROID_RELEASE_KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Without release credentials the artifact stays unsigned (never
            // shippable by accident); -PdebugSignRelease restores the old
            // locally-installable debug-signed behavior.
            signingConfig = when {
                releaseStoreFile != null -> signingConfigs.getByName("release")
                providers.gradleProperty("debugSignRelease").orNull == "true" ->
                    signingConfigs.getByName("debug")
                else -> null.also {
                    logger.lifecycle(
                        "Release signing not configured (no keystore.properties or " +
                            "ANDROID_RELEASE_* env vars): release artifacts will be unsigned."
                    )
                }
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }

        // Enable JUnit 5
        unitTests.all {
            it.useJUnitPlatform()
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
        }
    }

    lint {
        // Localization guard: user-facing text belongs in strings.xml.
        // (Covers XML layouts — the widget previews; Compose literals are
        // not lint-visible, so reviews must hold that line.)
        error += "HardcodedText"
    }
}

dependencies {
    implementation(project(":shared"))
    // Installs the generated baseline profile for AOT compilation
    implementation(libs.androidx.profileinstaller)
    "baselineProfile"(project(":baselineprofile"))

    // Compose BOM
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    // androidTestImplementation(composeBom) // Commented out - no tests

    // Compose UI
    implementation(libs.compose.ui.core)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.toolingPreview)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material)
    implementation(libs.compose.material3)
    implementation(libs.compose.materialIconsExtended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)

    // Compose Navigation (type-safe routes need the serialization runtime)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    // ViewModel utilities for Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Kotlin Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // Coil
    implementation(libs.coil.compose)

    // Glance for Widgets
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    // WorkManager for widget updates
    implementation(libs.androidx.work.runtime.ktx)

    // Unit Testing
    testImplementation(libs.junit4)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
    // Gradle 9 no longer injects the platform launcher
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.mockk.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.koin.test.core)
    testImplementation(libs.koin.test.junit4)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.robolectric)

    // Compose UI Testing
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.compose.ui.test.manifest)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.navigation.testing)
    androidTestImplementation(libs.koin.test.core)
    androidTestImplementation(libs.mockk.android)

    // Debug implementations for testing
    debugImplementation(libs.compose.ui.test.manifest)
    debugImplementation(libs.compose.ui.tooling)
}