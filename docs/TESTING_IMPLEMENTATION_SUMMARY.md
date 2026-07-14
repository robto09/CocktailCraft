# Testing Framework Setup

## Overview
This document summarizes the testing setup for the CocktailCraft Kotlin Multiplatform project. Most coverage lives in the **shared module's `commonTest`** suite (repositories, use cases, ViewModels, cache, DI graph), with platform suites on top.

> The test trees themselves are the up-to-date catalog — this page deliberately describes areas, not individual test files, so it doesn't drift when tests are added or renamed.

## Test Structure

```
├── shared/src/commonTest/              # Main suite: data, domain, viewmodel, di, util
├── androidApp/src/test/                # Android JVM unit tests (Robolectric-capable)
├── androidApp/src/androidTest/         # Android instrumented/Compose UI tests
├── iosApp/CocktailCraftTests/          # iOS unit tests (XCTest)
└── iosApp/CocktailCraftUITests/        # iOS UI tests (XCUITest)
```

## What's Covered

### Shared (`shared/src/commonTest/kotlin/com/cocktailcraft/`)
- **`data/`**: repository implementations (auth, cart, orders, reviews, favorites, offline, search), `CocktailCache`, API response parsing and `CocktailApiImpl`, auth-storage migration and password hashing
- **`domain/`**: use case tests (e.g. `PlaceOrderUseCaseTest`, `ManageFavoritesUseCaseTest`), input validation, domain policies
- **`viewmodel/`**: tests for all nine `Shared*ViewModel`s
- **`di/`**: `KoinDependencyGraphTest` (the real module graph must resolve) and the network retry policy
- **`util/`**: `ErrorHandler` classification, `Result` helpers
- **`testutil/`**: hand-written fakes (`Fakes.kt`), test data, and the main-dispatcher test base — tests construct ViewModels/use cases directly via constructor injection; there is no mock-based Koin test module

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
# Shared module (the bulk of the coverage)
./gradlew :shared:testAndroidHostTest

# Android unit tests
./gradlew :androidApp:testDebugUnitTest

# Android instrumented/UI tests (requires emulator or device)
./gradlew :androidApp:connectedDebugAndroidTest

# iOS (from iosApp/, after pod install / xcodegen generate)
xcodebuild test -workspace CocktailCraft.xcworkspace -scheme CocktailCraft \
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
