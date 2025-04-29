# Dependency Injection with MVVM + Clean Architecture

This document explains how dependency injection is implemented in the CocktailCraft app, following MVVM and Clean Architecture principles, with consideration for future iOS compatibility.

## Evolution of Dependency Injection in CocktailCraft

### Previous Approach

In the previous implementation:
- ViewModels used Koin's `by inject()` to directly inject dependencies
- There was no clear separation between platform-specific and shared code
- ViewModels directly accessed repositories in some cases
- There was no standardized pattern for cross-platform compatibility

```kotlin
// Old approach
class HomeViewModel : BaseViewModel() {
    // Direct injection using by inject()
    private val cocktailRepository: CocktailRepository by inject()

    // ViewModel implementation...
}
```

### Current Approach

The current implementation follows these principles:
- ViewModels receive dependencies via constructor parameters
- Clear separation between UI, domain, and data layers
- ViewModels only depend on use cases, not repositories
- Shared interfaces for cross-platform compatibility
- Platform-specific implementations with delegation pattern

## Architecture Overview

The CocktailCraft app follows a layered architecture:

1. **UI Layer** (Platform-specific)
   - Android: Jetpack Compose UI components
   - iOS: SwiftUI components (future)

2. **ViewModel Layer** (Platform-specific implementations with shared interfaces)
   - Manages UI state
   - Handles user actions
   - Communicates with domain layer through use cases

3. **Domain Layer** (Shared)
   - Business logic
   - Use cases
   - Repository interfaces
   - Domain models

4. **Data Layer** (Shared)
   - Repository implementations
   - Remote data sources (API)
   - Local data sources (Storage)
   - DTOs and mappers

## Dependency Injection with Koin

The app uses Koin for dependency injection, with modules organized by layer:

### Module Structure

```
shared/src/commonMain/kotlin/com/cocktailcraft/di/
├── AppModule.kt          # Main module that combines all other modules
├── CommonModule.kt       # Common dependencies (JSON, logging, etc.)
├── DataModule.kt         # Repository implementations and data sources
├── DomainModule.kt       # Domain models, interfaces, and business rules
├── NetworkModule.kt      # Network-related dependencies (HTTP client, API)
├── PlatformModule.kt     # Platform-specific dependencies (expect/actual)
└── UseCaseModule.kt      # Use cases that orchestrate data flow
```

### Platform-Specific Modules

```
shared/src/androidMain/kotlin/com/cocktailcraft/di/
└── PlatformModule.kt     # Android-specific implementations

shared/src/iosMain/kotlin/com/cocktailcraft/di/
└── PlatformModule.kt     # iOS-specific implementations
```

### ViewModel Dependency Injection

ViewModels are injected in the Android app using the `viewModelModule`:

```kotlin
// androidApp/src/main/java/com/cocktailcraft/di/ViewModelModule.kt
val viewModelModule = module {
    // Home screen
    viewModel {
        HomeViewModel(
            getCocktailsUseCase = get(),
            searchCocktailsUseCase = get(),
            networkStatusUseCase = get()
        )
    }

    // Other ViewModels...
}
```

## Cross-Platform ViewModel Architecture

To support both Android and iOS, we use a shared interface approach:

### Shared Interfaces

```kotlin
// shared/src/commonMain/kotlin/com/cocktailcraft/viewmodel/SharedViewModels.kt
interface IHomeViewModel {
    // State
    val cocktails: StateFlow<List<Cocktail>>
    // ...

    // Actions
    fun loadCocktails()
    // ...
}

// Other interfaces...
```

### Platform-Specific Implementations

#### Android Implementation

The Android implementation uses a two-layer approach:

1. **Android-specific ViewModels** in the Android app module:

```kotlin
// androidApp/src/main/java/com/cocktailcraft/viewmodel/HomeViewModel.kt
class HomeViewModel(
    private val getCocktailsUseCase: GetCocktailsUseCase,
    private val searchCocktailsUseCase: SearchCocktailsUseCase,
    private val networkStatusUseCase: NetworkStatusUseCase
) : BaseViewModel(), IHomeViewModel {
    // Implementation...
}
```

2. **Shared Module ViewModels** that implement the shared interfaces:

```kotlin
// shared/src/androidMain/kotlin/com/cocktailcraft/viewmodel/android/AndroidViewModels.kt
class HomeViewModel(
    private val getCocktailsUseCase: GetCocktailsUseCase,
    private val searchCocktailsUseCase: SearchCocktailsUseCase,
    private val networkStatusUseCase: NetworkStatusUseCase
) : IHomeViewModel {
    // State
    override val cocktails = MutableStateFlow<List<Cocktail>>(emptyList())
    override val hasMoreData = MutableStateFlow(true)
    override val isLoadingMore = MutableStateFlow(false)
    // ...

    // Actions
    override fun loadCocktails() {
        // Implementation using use cases
    }
    // ...
}
```

This approach allows us to:
- Keep a clean separation between shared and platform-specific code
- Provide a consistent API for both platforms
- Implement platform-specific behavior as needed
- Avoid dependency issues between modules

#### iOS Implementation (Future)

```kotlin
// shared/src/iosMain/kotlin/com/cocktailcraft/viewmodel/IOSViewModelFactory.kt
class IOSHomeViewModel(
    private val getCocktailsUseCase: GetCocktailsUseCase,
    private val searchCocktailsUseCase: SearchCocktailsUseCase,
    private val networkStatusUseCase: NetworkStatusUseCase
) : IHomeViewModel {
    // Implementation...
}
```

For iOS, we implement the shared interfaces directly, as there's no need for the delegation pattern used in Android.

### ViewModel Factory

To create ViewModels in a platform-agnostic way, we use a factory pattern:

```kotlin
// shared/src/commonMain/kotlin/com/cocktailcraft/viewmodel/ViewModelFactory.kt
interface ViewModelFactory {
    fun createHomeViewModel(): IHomeViewModel
    // Other factory methods...
}

expect fun createPlatformViewModelFactory(): ViewModelFactory
```

With platform-specific implementations:

```kotlin
// shared/src/androidMain/kotlin/com/cocktailcraft/viewmodel/AndroidViewModelFactory.kt
actual fun createPlatformViewModelFactory(): ViewModelFactory {
    return AndroidViewModelFactory()
}

// shared/src/iosMain/kotlin/com/cocktailcraft/viewmodel/IOSViewModelFactory.kt
actual fun createPlatformViewModelFactory(): ViewModelFactory {
    return IOSViewModelFactory()
}
```

## Benefits of This Approach

1. **Clean Architecture Boundaries**: Clear separation between UI, domain, and data layers
2. **Testability**: Easy to mock dependencies for testing
3. **Reusability**: Shared business logic between platforms
4. **Maintainability**: Changes to one layer don't affect others
5. **Scalability**: Easy to add new features or platforms

## Usage Example

### In Android

```kotlin
// In a Compose screen
val viewModel: HomeViewModel = viewModel()

// Use the ViewModel
val cocktails by viewModel.cocktails.collectAsState()
```

### In iOS (Future)

```swift
// In a SwiftUI view
let viewModelFactory = createPlatformViewModelFactory()
let viewModel = viewModelFactory.createHomeViewModel()

// Use the ViewModel
let cocktails = viewModel.cocktails
```

## Migration Guide

If you're updating existing ViewModels to follow this pattern, here are the steps:

1. **Create a Shared Interface**
   - Define the state properties and actions in a shared interface
   - Place it in the `shared/src/commonMain/kotlin/com/cocktailcraft/viewmodel` package

2. **Update the Android ViewModel**
   - Change from using `by inject()` to constructor parameters
   - Implement the shared interface
   - Keep initialization logic in the `init` block

3. **Create a Shared Module Implementation**
   - Create an implementation in `shared/src/androidMain/kotlin/com/cocktailcraft/viewmodel/android`
   - Implement the shared interface directly
   - Use the provided use cases to implement the functionality
   - Use `MutableStateFlow` for state properties

4. **Update the ViewModelModule**
   - Update the Koin module to provide the ViewModel with constructor parameters

5. **Update the ViewModelFactory**
   - Add methods to create the ViewModel in both Android and iOS factories

## Handling Initialization Logic

Special attention should be paid to initialization logic in ViewModels:

- **Android ViewModels**: Keep initialization logic in the `init` block
- **Shared Module Implementations**: Implement initialization logic directly in the class
- **iOS ViewModels**: Implement similar initialization logic directly

For example, in the Android implementation:

```kotlin
class HomeViewModel(
    private val getCocktailsUseCase: GetCocktailsUseCase,
    private val searchCocktailsUseCase: SearchCocktailsUseCase,
    private val networkStatusUseCase: NetworkStatusUseCase
) : IHomeViewModel {
    // State
    override val cocktails = MutableStateFlow<List<Cocktail>>(emptyList())

    init {
        // Initialize state
        loadCocktails()
    }

    override fun loadCocktails() {
        // Implementation using use cases
    }
}
```

## Conclusion

This dependency injection approach follows MVVM and Clean Architecture principles, with a focus on cross-platform compatibility. It provides a clear separation of concerns, makes the code more testable, and prepares the app for future iOS development.

The direct implementation approach in the shared module allows us to:

1. **Avoid Module Dependencies**: The shared module doesn't need to reference the Android module
2. **Simplify Testing**: Each implementation can be tested independently
3. **Ensure Consistency**: The same interface is implemented on both platforms
4. **Enable Platform-Specific Optimizations**: Each platform can optimize its implementation

This approach provides a clean, maintainable architecture that scales well for cross-platform development.
