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
        viewModelScope.launch {
            handleResultFlow(
                flow = getCocktailsUseCase(),
                onSuccess = { cocktails ->
                    _cocktails.value = cocktails
                },
                onError = { _ ->
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to load cocktails. Please try again.",
                recoveryAction = ErrorUtils.RecoveryAction("Retry") { loadCocktails() }
            )
        }
    }
    // ...
}
```

This approach allows us to:
- Keep a clean separation between shared and platform-specific code
- Provide a consistent API for both platforms
- Implement platform-specific behavior as needed
- Avoid dependency issues between modules

#### iOS Implementation

The iOS implementation follows similar patterns to Android but uses Swift-friendly approaches:

```kotlin
// shared/src/iosMain/kotlin/com/cocktailcraft/viewmodel/ios/IOSViewModels.kt
class IOSHomeViewModel(
    private val getCocktailsUseCase: GetCocktailsUseCase,
    private val searchCocktailsUseCase: SearchCocktailsUseCase,
    private val networkStatusUseCase: NetworkStatusUseCase,
    private val coroutineScope: CoroutineScope
) : IHomeViewModel {
    // State
    override val cocktails = MutableStateFlow<List<Cocktail>>(emptyList())
    override val isLoading = MutableStateFlow(false)
    override val error = MutableStateFlow<String?>(null)

    // Actions
    override fun loadCocktails() {
        coroutineScope.launch {
            isLoading.value = true
            try {
                getCocktailsUseCase()
                    .catch { e ->
                        error.value = e.message
                    }
                    .collect { result ->
                        when (result) {
                            is Result.Success -> cocktails.value = result.data
                            is Result.Error -> error.value = result.message
                        }
                    }
            } finally {
                isLoading.value = false
            }
        }
    }

    override fun searchCocktails(query: String) {
        coroutineScope.launch {
            isLoading.value = true
            try {
                searchCocktailsUseCase(query)
                    .catch { e ->
                        error.value = e.message
                    }
                    .collect { result ->
                        when (result) {
                            is Result.Success -> cocktails.value = result.data
                            is Result.Error -> error.value = result.message
                        }
                    }
            } finally {
                isLoading.value = false
            }
        }
    }
}

// Factory for creating iOS ViewModels
class IOSViewModelFactory(
    private val koin: Koin,
    private val coroutineScope: CoroutineScope
) {
    fun createHomeViewModel(): IOSHomeViewModel = IOSHomeViewModel(
        getCocktailsUseCase = koin.get(),
        searchCocktailsUseCase = koin.get(),
        networkStatusUseCase = koin.get(),
        coroutineScope = coroutineScope
    )

    fun createOrderViewModel(): IOSOrderViewModel = IOSOrderViewModel(
        placeOrderUseCase = koin.get(),
        manageOrdersUseCase = koin.get(),
        coroutineScope = coroutineScope
    )
}
```

Usage in Swift:

```swift
// HomeView.swift
class HomeViewModel: ObservableObject {
    private let viewModel: IOSHomeViewModel
    
    @Published var cocktails: [Cocktail] = []
    @Published var isLoading: Bool = false
    @Published var error: String? = nil
    
    init(factory: IOSViewModelFactory) {
        viewModel = factory.createHomeViewModel()
        
        // Collect state
        viewModel.cocktails.watch { [weak self] cocktails in
            self?.cocktails = cocktails
        }
        viewModel.isLoading.watch { [weak self] loading in
            self?.isLoading = loading
        }
        viewModel.error.watch { [weak self] error in
            self?.error = error
        }
    }
    
    func loadCocktails() {
        viewModel.loadCocktails()
    }
    
    func searchCocktails(query: String) {
        viewModel.searchCocktails(query: query)
    }
}
```

### Implementation Structure

The project uses a layered approach for ViewModels:

1. **Shared Interfaces** (in shared/commonMain):
   - Define the contract that all platforms must implement
   - Specify state properties and actions
   - Platform-agnostic, using Kotlin Multiplatform features

2. **Android Implementation** (in androidApp):
   - Extends BaseViewModel for common functionality
   - Implements shared interfaces
   - Uses coroutine-based logic with viewModelScope
   - Handles errors consistently with handleResultFlow
   - Interacts with use cases for business logic

3. **Future iOS Implementation** (to be added in shared/iosMain):
   - Will implement shared interfaces directly
   - Will use platform-specific patterns while maintaining the same contract

This structure ensures consistent behavior across platforms while allowing for platform-specific optimizations.

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
        viewModelScope.launch {
            handleResultFlow(
                flow = getCocktailsUseCase(),
                onSuccess = { cocktails ->
                    _cocktails.value = cocktails
                },
                onError = { _ ->
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to load cocktails. Please try again.",
                recoveryAction = ErrorUtils.RecoveryAction("Retry") { loadCocktails() }
            )
        }
    }
}
```

## Related Documentation

For more detailed information on implementing ViewModels, see:

1. [ViewModel Implementation Guide](./ViewModel_Implementation.md)
   - Coroutine handling in ViewModels
   - State management with StateFlow
   - Error handling
   - Resource cleanup
   - Cross-platform considerations
   - Best practices

2. [Dependency Injection Guide](./DependencyInjection.md)
   - Module organization
   - Dependency management
   - Testing with Koin
   - Platform-specific implementations

3. [Migration Guide](./DependencyInjectionMigration.md)
   - Updating existing ViewModels
   - Testing patterns
   - Common issues and solutions

## Conclusion

This dependency injection approach follows MVVM and Clean Architecture principles, with robust cross-platform support. It provides:

1. **Clear Separation of Concerns**:
   - UI layer is platform-specific
   - Business logic is shared
   - Data layer is platform-independent

2. **Cross-Platform Benefits**:
   - Shared interfaces ensure consistent behavior
   - Platform-specific optimizations are possible
   - Code reuse across platforms
   - Simplified testing strategy

3. **Development Efficiency**:
   - Reduced code duplication
   - Faster feature development
   - Consistent patterns across platforms
   - Easier maintenance

4. **Testing Advantages**:
   - Independent testing of each layer
   - Mock dependencies for unit tests
   - Shared test utilities
   - Platform-specific test support

5. **Future-Proofing**:
   - Easy to add new platforms
   - Scalable architecture
   - Maintainable codebase
   - Clear upgrade paths

## Coroutine-Based Logic

All ViewModels in the CocktailCraft app use a consistent pattern for handling asynchronous operations with coroutines:

1. **viewModelScope**: Each ViewModel uses the `viewModelScope` provided by AndroidX ViewModel, which is automatically canceled when the ViewModel is cleared.

2. **Standardized Flow Handling**: The `handleResultFlow` method from BaseViewModel is used to handle Result-wrapped Flows consistently:

```kotlin
viewModelScope.launch {
    handleResultFlow(
        flow = someUseCase.someOperation(),
        onSuccess = { result ->
            // Handle success
        },
        onError = { _ ->
            // Error handling is done by handleResultFlow
        },
        defaultErrorMessage = "User-friendly error message",
        recoveryAction = ErrorUtils.RecoveryAction("Retry") { someAction() }
    )
}
```

3. **Consistent Error Handling**: All ViewModels use the same error handling pattern, making the code more maintainable and providing a consistent user experience.

4. **Kotlin Conventions**: Unused parameters are renamed to underscore (_) following Kotlin conventions.

This approach provides a clean, maintainable architecture that scales well for cross-platform development and ensures consistent handling of asynchronous operations across the app.
