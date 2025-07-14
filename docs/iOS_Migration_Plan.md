# CocktailCraft iOS Migration Plan

This document outlines the detailed plan and tasks required to build the iOS version of the CocktailCraft application using the existing Kotlin Multiplatform shared module.

## Guiding Principles

- **Maximize Shared Code**: Business logic, data layers, and ViewModels will be shared.
- **Native UI/UX**: The UI will be built with SwiftUI to provide a native iOS experience that aligns with the design and structure of the Android app.
- **Clean Architecture + MVVM**: The iOS app will follow the same architectural principles as the Android app for consistency and maintainability.
- **Incremental Migration**: The process is broken down into phases to ensure the Android app remains functional throughout the migration.

## Key Libraries & Technologies

- **SwiftUI**: For building the user interface.
- **Koin**: For dependency injection, consistent with the shared module.
- **SKIE**: To generate Swift-friendly wrappers for the KMM shared module, simplifying the use of Kotlin features like Coroutines and Flow from Swift.
- **KMM-ViewModel**: A library from Touchlab for creating shared, multiplatform ViewModels that handle lifecycle and Coroutine scopes.

---

## Migration Tasks

### Phase 1: Project Analysis & Preparation

**Goal**: Prepare the project for iOS development by analyzing the existing structure and planning the refactoring of the shared module.

| Task ID | Task Description | Involved Directories/Files | Status | Notes |
| :--- | :--- | :--- | :--- | :--- |
| **1.1** | **Analyze Android-specific code for sharing** | `androidApp/src/main/java/com/cocktailcraft/` | ⏳ Pending | Identify all ViewModels, models, and utilities that can be moved to the `shared` module. |
| 1.1.1 | Identify Android dependencies in ViewModels | `androidApp/.../viewmodel/` | ⏳ Pending | `androidx.lifecycle.ViewModel` and `viewModelScope` are the primary dependencies to be replaced. |
| 1.1.2 | Identify sharable models and utilities | `androidApp/.../model/`, `androidApp/.../util/` | ⏳ Pending | `SortOption.kt` is a clear candidate. Utilities like `ErrorUtils.kt` are also good candidates for the shared module. |
| 1.1.3 | Finalize KMM ViewModel strategy | - | ✅ Completed | We will use the `kmm-viewmodel` library from Touchlab for a robust, lifecycle-aware, multiplatform ViewModel implementation. |
| 1.1.4 | Assess SKIE & Touchlab OSS libraries | - | ✅ Completed | **SKIE** will be used to greatly improve the Swift API of our shared module. **KMM-ViewModel** will be used for ViewModels. |

### Phase 2: Refactor Shared Module for iOS Compatibility

**Goal**: Move Android-specific logic to the shared module and make it platform-agnostic, ensuring the Android app remains fully functional.

| Task ID | Task Description | Involved Directories/Files | Status | Notes |
| :--- | :--- | :--- | :--- | :--- |
| **2.1** | **Enable iOS Target in Gradle** | `shared/build.gradle.kts` | ⏳ Pending | |
| 2.1.1 | Add iOS targets to `build.gradle.kts` | `shared/build.gradle.kts` | ⏳ Pending | Add `iosX64()`, `iosArm64()`, and `iosSimulatorArm64()` targets. |
| 2.1.2 | Add `kmm-viewmodel` dependency | `shared/build.gradle.kts` | ⏳ Pending | Add the dependency to `commonMain` source set. |
| 2.1.3 | Create `iosMain` source set | `shared/src/` | ⏳ Pending | Create the directory structure `shared/src/iosMain/kotlin/`. |
| **2.2** | **Refactor ViewModels to be Multiplatform** | `androidApp/.../viewmodel/`, `shared/src/commonMain/...` | ⏳ Pending | This is a critical step to share presentation logic. |
| 2.2.1 | Create a base `SharedViewModel` in `commonMain` | `shared/src/commonMain/kotlin/com/cocktailcraft/viewmodel/` | ⏳ Pending | This will likely be an `expect class` or an interface using the `kmm-viewmodel` library. |
| 2.2.2 | Move ViewModels to `shared` module | `androidApp/.../viewmodel/` -> `shared/.../viewmodel/` | ⏳ Pending | All ViewModels (`HomeViewModel`, `CartViewModel`, etc.) will be moved. |
| 2.2.3 | Update ViewModel inheritance | `shared/.../viewmodel/` | ⏳ Pending | ViewModels will extend the new multiplatform ViewModel base class instead of `androidx.lifecycle.ViewModel`. |
| 2.2.4 | Update Android app to use shared ViewModels | `androidApp/.../main/MainScreen.kt` | ⏳ Pending | The way ViewModels are instantiated in the UI will need to be updated. |
| **2.3** | **Move Agnostic Code to `shared`** | `androidApp/src/main/java/com/cocktailcraft/` | ⏳ Pending | |
| 2.3.1 | Move `SortOption.kt` model | `androidApp/.../model/` -> `shared/.../domain/model` | ⏳ Pending | |
| 2.3.2 | Move `ErrorUtils.kt` utility | `androidApp/.../util/` -> `shared/.../util` | ⏳ Pending | `ErrorUtils` is a perfect candidate for the shared module. |
| **2.4** | **Verify Android App Functionality** | `androidApp/` | ⏳ Pending | Crucial checkpoint before starting iOS development. |
| 2.4.1 | Build and run the `androidApp` | - | ⏳ Pending | Ensure the app compiles and runs on an emulator or device. |
| 2.4.2 | Perform regression testing on Android | - | ⏳ Pending | Manually test all features to confirm nothing broke during the refactor. |

### Phase 3: iOS Project Setup

**Goal**: Create the iOS application, configure it to use the shared module, and set up the basic project structure.

| Task ID | Task Description | Involved Directories/Files | Status | Notes |
| :--- | :--- | :--- | :--- | :--- |
| **3.1** | **Configure iOS Framework Generation** | `shared/build.gradle.kts` | ⏳ Pending | |
| 3.1.1 | Add CocoaPods or SPM integration | `shared/build.gradle.kts` | ⏳ Pending | We'll use CocoaPods for managing the dependency on the shared framework. |
| 3.1.2 | Integrate SKIE Gradle plugin | `shared/build.gradle.kts` | ⏳ Pending | This will significantly improve the developer experience on the Swift side. |
| **3.2** | **Create Xcode Project** | `iosApp/` | ⏳ Pending | |
| 3.2.1 | Create a new SwiftUI project in Xcode | `iosApp/` | ⏳ Pending | The project will be placed in a new top-level `iosApp` directory. |
| 3.2.2 | Configure `Podfile` for shared module | `iosApp/Podfile` | ⏳ Pending | Add `pod 'shared', :path => '../shared'`. |
| 3.2.3 | Install dependencies | `iosApp/` | ⏳ Pending | Run `pod install` from the `iosApp` directory. |
| **3.3** | **Setup Dependency Injection for iOS** | `iosApp/`, `shared/src/iosMain/...` | ⏳ Pending | |
| 3.3.1 | Implement `PlatformModule.kt` for iOS | `shared/src/iosMain/kotlin/com/cocktailcraft/di/` | ⏳ Pending | Provide iOS-specific implementations, like `NSUserDefaultsSettings` for Koin. |
| 3.3.2 | Initialize Koin in the iOS app | `iosApp/CocktailCraftApp.swift` | ⏳ Pending | Create a helper to start Koin and call it from the `App` struct's initializer. |

### Phase 4: iOS App Implementation

**Goal**: Build the SwiftUI user interface, connect it to the shared ViewModels, and implement all features for the iOS platform.

| Task ID | Task Description | Involved Directories/Files | Status | Notes |
| :--- | :--- | :--- | :--- | :--- |
| **4.1** | **Implement Core App Structure & Navigation** | `iosApp/` | ⏳ Pending | |
| 4.1.1 | Create `MainView.swift` (equivalent to `MainScreen`) | `iosApp/` | ⏳ Pending | This view will contain the `TabView` for bottom navigation. |
| 4.1.2 | Implement SwiftUI Navigation | `iosApp/` | ⏳ Pending | Use `NavigationStack` for navigation between screens. |
| 4.1.3 | Create ViewModel wrapper/helper for SwiftUI | `iosApp/` | ⏳ Pending | A helper to make it easy to use the KMM ViewModels in SwiftUI views. |
| **4.2** | **Implement Screens (SwiftUI)** | `iosApp/screens/` | ⏳ Pending | Create one screen at a time, mapping from the Android version. |
| 4.2.1 | Implement `HomeScreen` | `iosApp/screens/` | ⏳ Pending | |
| 4.2.2 | Implement `CocktailDetailScreen` | `iosApp/screens/` | ⏳ Pending | |
| 4.2.3 | Implement `CartScreen` | `iosApp/screens/` | ⏳ Pending | |
| 4.2.4 | Implement `FavoritesScreen` | `iosApp/screens/` | ⏳ Pending | |
| 4.2.5 | Implement `ProfileScreen` | `iosApp/screens/` | ⏳ Pending | |
| 4.2.6 | Implement `OrderListScreen` | `iosApp/screens/` | ⏳ Pending | |
| 4.2.7 | Implement `OfflineModeScreen` | `iosApp/screens/` | ⏳ Pending | |
| **4.3** | **Implement UI Components (SwiftUI)** | `iosApp/components/` | ⏳ Pending | Recreate the reusable components from the Android app in SwiftUI. |
| 4.3.1 | Implement `CocktailItemView.swift` | `iosApp/components/` | ⏳ Pending | |
| 4.3.2 | Implement `ErrorDialogView.swift` | `iosApp/components/` | ⏳ Pending | |
| 4.3.3 | ... (and so on for other components) | `iosApp/components/` | ⏳ Pending | |
| **4.4** | **Implement Platform-Specifics** | `shared/src/iosMain/` | ⏳ Pending | |
| 4.4.1 | Implement Image Loading for iOS | `iosApp/` | ⏳ Pending | Use a library like `Kingfisher` or SwiftUI's `AsyncImage`. |
| 4.4.2 | Implement `NetworkMonitor` for iOS | `shared/src/iosMain/kotlin/com/cocktailcraft/util/` | ⏳ Pending | Create the `actual` implementation for the `expect class`. |
| **4.5** | **Testing and Deployment** | `iosApp/` | ⏳ Pending | |
| 4.5.1 | Write Unit & UI Tests for iOS | `iosApp/Tests/` | ⏳ Pending | |
| 4.5.2 | Prepare for App Store deployment | `iosApp/` | ⏳ Pending | Archiving, code signing, and App Store Connect setup. | 