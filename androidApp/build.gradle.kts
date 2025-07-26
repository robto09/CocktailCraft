plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android") version "2.51.1"
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.0"
}

android {
    namespace = "com.cocktailcraft.android"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.cocktailcraft.android"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
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
    hilt {
        enableAggregatingTask = true
    }
}

dependencies {
    implementation(project(":shared"))
    
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2023.05.01")
    implementation(composeBom)
    // androidTestImplementation(composeBom) // Commented out - no tests
    
    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.appcompat:appcompat:1.6.1")
    
    // Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.5.3")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    // ViewModel utilities for Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    
    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    kapt("com.google.dagger:hilt-compiler:2.51.1")
    
    // Koin
    implementation("io.insert-koin:koin-android:3.5.6")
    implementation("io.insert-koin:koin-androidx-compose:3.5.6")

    // Coil
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Accompanist libraries for animations and system UI controller
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.31.0-alpha")
    implementation("com.google.accompanist:accompanist-navigation-animation:0.31.0-alpha")
    
    // Testing - Commented out, no tests
    // testImplementation("androidx.compose.ui:ui-test-junit4")
}