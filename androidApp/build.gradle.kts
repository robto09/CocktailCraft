plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.cocktailcraft"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cocktailcraft"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Use our custom test runner for instrumented tests
        testInstrumentationRunner = "com.cocktailcraft.CocktailCraftTestRunner"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

// Add a configuration for dependency resolution
configurations.all {
    resolutionStrategy {
        // Force a specific version of Espresso to avoid conflicts
        force("androidx.test.espresso:espresso-core:3.5.0")
        force("androidx.test.espresso:espresso-idling-resource:3.5.0")
    }
}

dependencies {
    implementation(project(":shared"))
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.ui.test.junit4.android)

    // Compose
    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Material Icons Extended - provides additional icons
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Koin
    implementation("io.insert-koin:koin-android:3.4.0")
    implementation("io.insert-koin:koin-androidx-compose:3.4.0")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Add Coil for image loading
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Accompanist libraries for animations and system UI controller
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.accompanist.navigation.animation)

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.navigation:navigation-testing:2.7.7")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.22")
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    testImplementation("com.russhwolf:multiplatform-settings:1.1.1")

    // Instrumented test dependencies
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    // Use the version of Espresso that's compatible with Compose UI test
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.0")
    androidTestImplementation("org.mockito:mockito-android:5.3.1")
    androidTestImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}
