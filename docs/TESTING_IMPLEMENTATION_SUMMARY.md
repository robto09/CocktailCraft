# Testing Framework Setup

## Overview
This document summarizes the testing setup for the CocktailCraft Kotlin Multiplatform project. Most coverage lives in the shared KMP modules' **`commonTest`** suites (repositories, use cases, ViewModels, cache, DI graph) — each module's tests sit next to its code — with platform suites on top.

> The test trees themselves are the up-to-date catalog — this page deliberately describes areas, not individual test files, so it doesn't drift when tests are added or renamed.

## Test Structure

```
├── core/common/src/commonTest/         # ErrorHandler
├── core/domain/src/commonTest/         # use cases, policies, validators
├── core/data/src/commonTest/           # repositories, cache, API, database, data DI
├── presentation/src/commonTest/        # the shared ViewModels
├── shared/src/{commonTest,androidHostTest,iosTest}/  # full DI graph, KoinHelper, Android strings
├── core/testing/src/commonMain/        # test-only module: fakes, test data, test bases
├── androidApp/src/test/                # Android JVM unit tests (Robolectric-capable)
├── androidApp/src/androidTest/         # Android instrumented/Compose UI tests
├── iosApp/CocktailCraftTests/          # iOS unit tests (XCTest)
└── iosApp/CocktailCraftUITests/        # iOS UI tests (XCUITest)
```

## What's Covered

### Shared KMP modules (`<module>/src/commonTest/kotlin/com/cocktailcraft/`)
- **`core/data` → `data/`, `di/`, `util/`**: repository implementations (auth, cart, orders, reviews, favorites, offline, search), `CocktailCache`, API response parsing and `CocktailApiImpl`, auth-storage migration and password hashing, the network retry policy, `DataModuleGraphTest` (the data layer's internal singletons resolve), `Result` helpers at the repository boundary
- **`core/domain` → `domain/`**: use case tests (e.g. `PlaceOrderUseCaseTest`, `ManageFavoritesUseCaseTest`), input validation, domain policies
- **`core/common` → `util/`**: `ErrorHandler` classification
- **`presentation` → `viewmodel/`**: tests for the `Shared*ViewModel`s; the Profile/Theme/Review ones run against the real repositories through `testutil/DataLayer.kt` (`:core:data` is a test-only dependency there)
- **`shared` → `di/`, `KoinHelperTest`, `resources/`**: `KoinDependencyGraphTest` (the full production graph must resolve), its iOS mirror for `KoinHelper`, and Android resolution of the shared strings
- **`core/testing` → `testutil/`**: hand-written fakes (`Fakes.kt`), test data, the main-dispatcher test base and an in-memory SQLite driver — tests construct ViewModels/use cases directly via constructor injection; there is no mock-based Koin test module

### Android
- **`androidApp/src/test`**: JVM unit tests (framework verification and Android-specific units)
- **`androidApp/src/androidTest`**: instrumented Compose tests driving the app against fake repositories (e.g. `MainScreenSharedVmTest` with `fakes/FakeRepositories.kt`)

### iOS
- **`iosApp/CocktailCraftTests`**: core unit tests, `SharedViewModelWrapper` behavior, widget bridge
- **`iosApp/CocktailCraftUITests`**: XCUITest flows (home, cart, favorites, deep links)

## Testing Technologies

**Shared**: kotlin-test, kotlinx-coroutines-test
**Android**: JUnit (4 + Jupiter), Compose UI Testing, Robolectric, Koin test
**iOS**: XCTest, XCUITest

## How to Run Tests

```bash
# Shared KMP modules (the bulk of the coverage), on the JVM and the iOS simulator
./gradlew testAndroidHostTest
./gradlew iosSimulatorArm64Test

# Android unit tests
./gradlew :androidApp:testDebugUnitTest

# Android instrumented/UI tests (requires emulator or device)
./gradlew :androidApp:connectedDebugAndroidTest

# iOS (from iosApp/, after xcodegen generate)
xcodebuild test -project CocktailCraft.xcodeproj -scheme CocktailCraft \
  -destination 'platform=iOS Simulator,name=iPhone 16'
```

## What This Gives You

- **Regression Prevention**: the shared suite exercises the business logic both platforms run
- **DI Safety**: Koin wiring breaks fail in CI via the dependency-graph test
- **Documentation**: tests serve as living documentation of expected behavior
- **Confidence**: safe refactoring and feature development

## Future Enhancements

1. **Integration Tests**: end-to-end user flows across multiple screens
2. **Performance Tests**: UI rendering performance and memory usage
3. **Accessibility Tests**: screen reader and accessibility compliance
4. **API Tests**: broader mock API response and error-scenario coverage
