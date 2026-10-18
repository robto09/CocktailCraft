# CocktailCraft Libraries

This document lists the libraries used in the CocktailCraft application, with their versions and purposes.

All versions are managed in a single version catalog: **`libraries.toml`** at the repository root, wired up as the `libs` catalog in `settings.gradle.kts` (`versionCatalogs { create("libs") { from(files("libraries.toml")) } }`). The build files (`shared/`, `presentation/` and `core/*/build.gradle.kts`, `androidApp/build.gradle.kts`, `baselineprofile/build.gradle.kts`) and the `build-logic` convention plugin (AR-10) reference dependencies and SDK/JVM levels only through the catalog — update versions there, not in the build files.

## Shared Modules (Kotlin Multiplatform)

Used across `:shared` and the `:core:*`/`:presentation` modules it is split into (see README "Module Structure" for which module uses what).

| Library | Version | Purpose |
|---------|---------|---------|
| Kotlin | 2.3.21 | Language and multiplatform toolchain |
| Kotlinx Coroutines | 1.11.0 | Asynchronous programming (core in commonMain; `-android` provides `Dispatchers.Main` on Android) |
| Ktor | 3.5.1 | HTTP client (core, content negotiation, logging, kotlinx-json serialization; Android/Darwin engines per platform) |
| Kotlinx Serialization | 1.11.0 | JSON (de)serialization |
| Kotlinx DateTime | 0.8.0 | Date and time handling |
| Koin | 4.2.2 | Dependency injection (`koin-core`, `koin-core-viewmodel`; `koin-android`/`koin-androidx-compose` on Android) |
| Multiplatform Settings | 1.3.0 | Cross-platform key-value storage (auth data, in the secure store; older versions' cart/orders/favorites/reviews are read once to import them into SQLite) |
| SQLDelight | 2.4.1 | SQLite storage for the offline cocktail cache, cart, orders, favorites and reviews (`android-driver` on Android, `native-driver` on iOS; the iOS app links `sqlite3` itself via `OTHER_LDFLAGS` in `iosApp/project.yml`; `coroutines-extensions` for reactive `Query.asFlow()` reads) |
| Kermit | 2.2.0 | Multiplatform logging |
| moko-resources | 0.27.1 | Single source for the app's strings: generates `MR.strings`/`MR.plurals`, Android string resources and the iOS strings bundle from `core/resources/src/commonMain/moko-resources` (exported to Swift; see README "Localization") |
| SKIE | 0.10.13 | Swift interop: Flow → AsyncSequence, suspend → async/await, enum/sealed-class bridging |
| AndroidX Lifecycle ViewModel | 2.10.0 | Multiplatform `ViewModel` base for the shared ViewModels (exposed as `api` so consumers see the supertype) |
| Tink (`tink-android`) | 1.23.0 | Encrypts the Android auth store: AES-256-GCM over the whole store file, with the keyset wrapped by an Android Keystore key (`AuthStore.kt` in `:core:data`) |
| AndroidX Security Crypto | 1.1.0-alpha06 | Legacy reader only: imports the `EncryptedSharedPreferences` file older versions kept the auth store in, once, then deletes it. Deprecated library; remove it as described in `LegacySecurePrefs.kt` |

## Android App

Compose library versions come from the **Compose BOM 2026.06.01** — the individual Compose artifacts (`ui`, `foundation`, `material3`, `material-icons-extended`, tooling) are declared without versions.

| Library | Version | Purpose |
|---------|---------|---------|
| Compose BOM | 2026.06.01 | Bill of materials pinning all Compose artifact versions |
| Activity Compose | 1.13.0 | Compose integration for Activities |
| Navigation Compose | 2.9.8 | Type-safe navigation between screens |
| Lifecycle (ViewModel/Runtime) | 2.10.0 | Lifecycle-aware ViewModel and runtime Compose integration |
| Coil | 2.7.0 | Image loading and caching for Compose |
| AppCompat | 1.8.0 | Base compatibility support |
| Glance (appwidget, material3) | 1.2.0 | Home-screen widgets |
| WorkManager | 2.12.0 | Background work (widget updates) |
| Koin (android, androidx-compose) | 4.2.2 | DI integration with Android and `koinViewModel()` |

## iOS App (Swift Package Manager)

Declared under `packages:` in `iosApp/project.yml` and pinned by the committed
`Package.resolved`. The shared Kotlin code reaches Xcode as the `shared` framework built by the
`:shared` umbrella module through Kotlin's direct integration
(`:shared:embedAndSignAppleFrameworkForXcode`), not a package manager.

| Library | Version | Purpose |
|---------|---------|---------|
| Kingfisher | 7.12.0 (up to next major) | Image loading with disk/memory caching |
| DebugSwift | 1.7.13.1 (pinned by commit) | In-app QA debugging toolkit — Debug builds only (see [DebugSwift.md](DebugSwift.md)) |

## Testing

| Library | Version | Purpose |
|---------|---------|---------|
| kotlin-test | 2.3.21 | Multiplatform test assertions (commonTest) |
| JUnit 4 | 4.13.2 | Android unit test runner base |
| JUnit Jupiter (JUnit 5) | 5.14.4 | Android app unit tests (API, engine, params, vintage engine for Robolectric; platform launcher 1.14.4) |
| Mockk | 1.14.11 | Kotlin-friendly mocking (`mockk-android` for instrumented tests) |
| Turbine | 1.2.1 | Flow emission testing |
| Kotlinx Coroutines Test | 1.11.0 | Coroutine test dispatchers and scopes |
| Ktor Client Mock | 3.5.1 | `MockEngine` for exercising the real Ktor pipeline in API tests |
| Multiplatform Settings Test | 1.3.0 | In-memory `Settings` for shared tests |
| SQLDelight SQLite (JDBC) driver | 2.4.1 | In-memory database for the cache and repository tests on the Android host (iOS tests use the native driver's in-memory mode) |
| Koin Test | 4.2.2 | DI graph verification (`koin-test`, `koin-test-junit4`) |
| Robolectric | 4.17 | JVM-hosted Android unit tests |
| Espresso | 3.7.0 | Instrumented UI testing |
| Navigation Testing | 2.9.8 | Navigation component test utilities |
| AndroidX Test (core/rules/runner) | 1.7.0 | Instrumentation test infrastructure (`ext-junit` 1.3.0) |
| Arch Core Testing | 2.2.0 | `InstantTaskExecutorRule` for architecture components |
| Compose UI Test (junit4, manifest) | BOM | Compose UI testing (versions from the Compose BOM) |
| Benchmark (macro-junit4) | 1.5.0 | Macrobenchmarks and baseline profile generation |
| UIAutomator | 2.4.0 | Device-level UI automation for benchmarks |
| Profile Installer | 1.4.1 | Installs the generated baseline profile for AOT compilation |

## Build Plugins

| Plugin | Version | Purpose |
|--------|---------|---------|
| Kotlin Multiplatform / Serialization | 2.3.21 | Kotlin compilation and serialization codegen |
| Kotlin Compose (`org.jetbrains.kotlin.plugin.compose`) | 2.3.21 | Compose compiler — ships with Kotlin, no separately pinned compiler-extension version |
| Android Gradle Plugin (application, test, kotlin-multiplatform-library) | 9.2.1 | Android builds; `compileSdk` (36) and `jvmTarget` (17) also live in the catalog |
| Baseline Profile (`androidx.baselineprofile`) | 1.5.0 | Baseline profile generation and consumption |
| SKIE (`co.touchlab.skie`) | 0.10.13 | Swift interop for the iOS framework |
| moko-resources (`dev.icerock.mobile.multiplatform-resources`) | 0.27.1 | Generates the shared strings (`MR`) for Android and iOS; `copyFrameworkResourcesToApp` copies the iOS bundle into the app |
| detekt (`io.gitlab.arturbosch.detekt`, via build-logic) | 1.23.8 | Kotlin static analysis; `detekt-formatting` 1.23.8 wraps ktlint (check-only) — see README "Static analysis" |
| Compose rules for detekt (`io.nlopez.compose.rules:detekt`) | 0.4.28 | Jetpack Compose lint rules (last line built for detekt 1.23) |
