# CocktailCraft Libraries

This document lists the libraries used in the CocktailCraft application, with their versions and purposes.

All versions are managed in a single version catalog: **`libraries.toml`** at the repository root, wired up as the `libs` catalog in `settings.gradle.kts` (`versionCatalogs { create("libs") { from(files("libraries.toml")) } }`). The build files (`shared/build.gradle.kts`, `androidApp/build.gradle.kts`, `baselineprofile/build.gradle.kts`) reference dependencies only through the catalog — update versions there, not in the build files.

## Shared Module (Kotlin Multiplatform)

| Library | Version | Purpose |
|---------|---------|---------|
| Kotlin | 2.3.21 | Language and multiplatform toolchain |
| Kotlinx Coroutines | 1.11.0 | Asynchronous programming (core in commonMain; `-android` provides `Dispatchers.Main` on Android) |
| Ktor | 3.5.1 | HTTP client (core, content negotiation, logging, kotlinx-json serialization; Android/Darwin engines per platform) |
| Kotlinx Serialization | 1.11.0 | JSON (de)serialization |
| Kotlinx DateTime | 0.8.0 | Date and time handling |
| Koin | 4.2.2 | Dependency injection (`koin-core`, `koin-core-viewmodel`; `koin-android`/`koin-androidx-compose` on Android) |
| Multiplatform Settings | 1.3.0 | Cross-platform key-value storage |
| Kermit | 2.1.0 | Multiplatform logging |
| SKIE | 0.10.13 | Swift interop: Flow → AsyncSequence, suspend → async/await, enum/sealed-class bridging |
| AndroidX Lifecycle ViewModel | 2.10.0 | Multiplatform `ViewModel` base for the shared ViewModels (exposed as `api` so consumers see the supertype) |
| AndroidX Security Crypto | 1.1.0-alpha06 | Keystore-backed `EncryptedSharedPreferences` for the Android auth store |

## Android App

Compose library versions come from the **Compose BOM 2026.06.01** — the individual Compose artifacts (`ui`, `foundation`, `material3`, `material-icons-extended`, tooling) are declared without versions.

| Library | Version | Purpose |
|---------|---------|---------|
| Compose BOM | 2026.06.01 | Bill of materials pinning all Compose artifact versions |
| Activity Compose | 1.13.0 | Compose integration for Activities |
| Navigation Compose | 2.9.8 | Type-safe navigation between screens |
| Lifecycle (ViewModel/Runtime) | 2.10.0 | Lifecycle-aware ViewModel and runtime Compose integration |
| Coil | 2.7.0 | Image loading and caching for Compose |
| AppCompat | 1.6.1 | Base compatibility support |
| Glance (appwidget, material3) | 1.1.1 | Home-screen widgets |
| WorkManager | 2.9.0 | Background work (widget updates) |
| Koin (android, androidx-compose) | 4.2.2 | DI integration with Android and `koinViewModel()` |

## Testing

| Library | Version | Purpose |
|---------|---------|---------|
| kotlin-test | 2.3.21 | Multiplatform test assertions (commonTest) |
| JUnit 4 | 4.13.2 | Android unit test runner base |
| JUnit Jupiter (JUnit 5) | 5.10.1 | Modern unit testing (API, engine, params; platform launcher 1.10.1) |
| Mockk | 1.13.8 | Kotlin-friendly mocking (`mockk-android` for instrumented tests) |
| Turbine | 1.2.1 | Flow emission testing |
| Kotlinx Coroutines Test | 1.11.0 | Coroutine test dispatchers and scopes |
| Ktor Client Mock | 3.5.1 | `MockEngine` for exercising the real Ktor pipeline in API tests |
| Multiplatform Settings Test | 1.3.0 | In-memory `Settings` for shared tests |
| Koin Test | 4.2.2 | DI graph verification (`koin-test`, `koin-test-junit4`) |
| Robolectric | 4.11.1 | JVM-hosted Android unit tests |
| Espresso | 3.7.0 | Instrumented UI testing |
| Navigation Testing | 2.9.8 | Navigation component test utilities |
| AndroidX Test (core/rules/runner) | 1.7.0 | Instrumentation test infrastructure (`ext-junit` 1.3.0) |
| Arch Core Testing | 2.2.0 | `InstantTaskExecutorRule` for architecture components |
| Compose UI Test (junit4, manifest) | BOM | Compose UI testing (versions from the Compose BOM) |
| Benchmark (macro-junit4) | 1.5.0-alpha07 | Macrobenchmarks and baseline profile generation |
| UIAutomator | 2.3.0 | Device-level UI automation for benchmarks |
| Profile Installer | 1.4.1 | Installs the generated baseline profile for AOT compilation |

## Build Plugins

| Plugin | Version | Purpose |
|--------|---------|---------|
| Kotlin Multiplatform / JVM / Serialization | 2.3.21 | Kotlin compilation and serialization codegen |
| Kotlin Compose (`org.jetbrains.kotlin.plugin.compose`) | 2.3.21 | Compose compiler — ships with Kotlin, no separately pinned compiler-extension version |
| Android Gradle Plugin (application, library, kotlin-multiplatform-library) | 9.2.1 | Android builds; `compileSdk` (36) and `jvmTarget` (17) also live in the catalog |
| Baseline Profile (`androidx.baselineprofile`) | 1.5.0-alpha07 | Baseline profile generation and consumption |
| SKIE (`co.touchlab.skie`) | 0.10.13 | Swift interop for the iOS framework |
| Kotlin CocoaPods | 2.3.21 | Publishes the shared module as a CocoaPods framework |
