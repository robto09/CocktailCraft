# Dependency Injection in CocktailCraft

This document outlines the dependency injection (DI) approach used in the CocktailCraft app.

## Overview

CocktailCraft uses Koin for dependency injection. The DI setup has been organized to improve testability and separation of concerns.

## Recent Improvements

The dependency injection system has been significantly improved with the following changes:

### 1. Module Reorganization

We've refactored the single large `appModule` into smaller, focused modules:

- **NetworkModule**: Contains all network-related dependencies (HTTP client, API interfaces, network monitoring)
- **DataModule**: Contains data-layer dependencies (repositories, caching)
- **DomainModule**: Contains domain-layer dependencies (use cases, app configuration)

This modular approach provides several benefits:
- Better separation of concerns
- Easier maintenance
- Improved testability
- Clearer dependency boundaries
- More focused modules with single responsibilities

### 2. Testing Improvements

We've added dedicated testing support:

- **TestModule**: Provides mock implementations for testing
- **BaseKoinTest**: A base class for tests that handles Koin setup and teardown
- **Example Test**: Demonstrated how to use Koin for testing with `KoinThemeViewModelTest`

These improvements make it easier to write tests that use Koin for dependency injection, reducing boilerplate code and making tests more consistent.

### 3. ViewModel Architecture

We've standardized the ViewModel architecture:

- **KoinViewModel**: A base class that implements `KoinComponent`
- **BaseViewModel**: Extended to inherit from `KoinViewModel` and provides common functionality
- **Updated ViewModels**: Refactored to use the new base classes and consistent coroutine patterns

The BaseViewModel now provides standardized methods for handling asynchronous operations:

- **handleResultFlow**: A method for handling Result-wrapped Flows with consistent error handling
- **executeWithErrorHandling**: A method for executing suspend functions with error handling
- **executeWithErrorHandlingFlow**: A method for executing operations that return Flows

This approach provides a consistent pattern for dependency injection and asynchronous operations in ViewModels, making the code more maintainable and easier to understand.

## Module Structure

The DI system is organized into the following modules:

### 1. Domain Module (`domainModule`)
- Contains domain-level dependencies
- Includes domain models, interfaces, and business rules
- Located in `shared/src/commonMain/kotlin/com/cocktailcraft/di/DomainModule.kt`

### 2. UseCase Module (`useCaseModule`)
- Contains all use case implementations
- Orchestrates data flow between repositories and ViewModels
- Located in `shared/src/commonMain/kotlin/com/cocktailcraft/di/UseCaseModule.kt`

### 3. Data Module (`dataModule`)
- Contains data-layer dependencies
- Includes repositories and caching mechanisms
- Located in `shared/src/commonMain/kotlin/com/cocktailcraft/di/DataModule.kt`

### 4. Network Module (`networkModule`)
- Contains network-related dependencies
- Includes API clients, HTTP configuration, and network monitoring
- Located in `shared/src/commonMain/kotlin/com/cocktailcraft/di/NetworkModule.kt`

### 5. Platform Module (`platformModule`)
- Contains platform-specific dependencies
- Implemented differently for Android and iOS
- Located in platform-specific source sets

### 6. ViewModel Module (`viewModelModule`)
- Contains ViewModel factory methods and dependencies
- Platform-specific implementations
- Located in `androidApp/src/main/java/com/cocktailcraft/di/ViewModelModule.kt`

### 7. Test Module (`testModule`)
- Contains mock implementations for testing
- Used only in test environments
- Located in `androidApp/src/test/java/com/cocktailcraft/di/TestModule.kt`

## ViewModel Dependency Injection

ViewModels use a consistent pattern for dependency injection:

1. All ViewModels extend `BaseViewModel` which extends `KoinViewModel`
2. ViewModels receive dependencies via constructor injection
3. ViewModels use a consistent pattern for handling asynchronous operations

Example:
```kotlin
class ThemeViewModel(
    private val themeUseCase: ThemeUseCase
) : BaseViewModel(), IThemeViewModel {

    // State exposed to the UI
    private val _isDarkMode = MutableStateFlow(false)
    override val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Load theme preferences
    private fun loadThemePreference() {
        viewModelScope.launch {
            handleResultFlow(
                flow = themeUseCase.getUserPreferences(),
                onSuccess = { preferences ->
                    _isDarkMode.value = preferences.darkMode
                },
                onError = { _ ->
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to load theme preferences",
                showAsEvent = false
            )
        }
    }

    // ViewModel implementation...
}
```

## Testing with Koin

For testing with Koin:

1. Extend `BaseKoinTest` which handles Koin setup and teardown
2. Use the `testModule` which provides mock implementations
3. Inject dependencies using Koin or provide them via constructor

Example:
```kotlin
class KoinThemeViewModelTest : BaseKoinTest() {

    private lateinit var viewModel: ThemeViewModel
    private val authRepository: AuthRepository by inject()

    @Before
    override fun setUp() {
        super.setUp() // Initialize Koin
        // Test setup...
    }

    @Test
    fun `test something`() {
        // Test implementation...
    }
}
```

## Implementation Details

### Module Implementation

Each module is implemented as a Koin module with clear responsibilities:

```kotlin
// NetworkModule.kt
val networkModule = module {
    // HTTP Client with improved configuration
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(get())
            }
            install(Logging) {
                level = LogLevel.HEADERS
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }
    }

    // API with error handling
    single<CocktailApi> {
        CocktailApiImpl(
            client = get(),
            errorHandler = get()
        )
    }

    // Enhanced network monitoring
    single {
        NetworkMonitor(
            context = get(),
            scope = get()
        )
    }
}

// UseCaseModule.kt
val useCaseModule = module {
    // Search and filtering
    factory {
        AdvancedFilterUseCase(
            repository = get(),
            errorHandler = get()
        )
    }
    factory {
        GetFilterOptionsUseCase(
            repository = get(),
            cache = get()
        )
    }

    // Order management
    factory {
        PlaceOrderUseCase(
            orderRepository = get(),
            cartRepository = get(),
            errorHandler = get()
        )
    }

    // User interactions
    factory {
        ToggleFavoriteUseCase(
            repository = get(),
            authRepository = get()
        )
    }
}

// ViewModelModule.kt
val viewModelModule = module {
    // Home screen with advanced filtering
    viewModel {
        HomeViewModel(
            advancedFilterUseCase = get(),
            getFilterOptionsUseCase = get(),
            networkStatusUseCase = get()
        )
    }

    // Order management
    viewModel {
        OrderViewModel(
            placeOrderUseCase = get(),
            manageOrdersUseCase = get()
        )
    }

    // Theme handling
    viewModel {
        ThemeViewModel(
            themeUseCase = get()
        )
    }
}

// DataModule.kt
val dataModule = module {
    // Enhanced JSON configuration
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true
            encodeDefaults = true
        }
    }

    // Improved caching with TTL
    single {
        CocktailCache(
            settings = get(),
            json = get(),
            timeProvider = get()
        )
    }

    // Repositories with error handling
    single<CocktailRepository> {
        CocktailRepositoryImpl(
            api = get(),
            cache = get(),
            errorHandler = get(),
            networkMonitor = get(),
            dispatchers = get()
        )
    }

    single<OrderRepository> {
        OrderRepositoryImpl(
            api = get(),
            cache = get(),
            errorHandler = get()
        )
    }
}

// DomainModule.kt
val domainModule = module {
    // Enhanced configuration
    single<AppConfig> {
        AppConfigImpl(
            environment = get(),
            settings = get()
        )
    }

    // Error handling
    single {
        ErrorHandler(
            networkMonitor = get(),
            logger = get()
        )
    }

    // Dispatchers
    single {
        DispatcherProvider(
            io = Dispatchers.IO,
            main = Dispatchers.Main,
            default = Dispatchers.Default
        )
    }
}
```

### ViewModel Base Classes

The ViewModel architecture uses a hierarchy of base classes:

```kotlin
// KoinViewModel.kt
abstract class KoinViewModel : ViewModel(), KoinComponent

// BaseViewModel.kt
abstract class BaseViewModel : KoinViewModel() {
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<ErrorUtils.UserFriendlyError?>(null)
    val error: StateFlow<ErrorUtils.UserFriendlyError?> = _error.asStateFlow()

    // Error events
    private val _errorEvent = MutableSharedFlow<ErrorUtils.UserFriendlyError>()
    val errorEvent: SharedFlow<ErrorUtils.UserFriendlyError> = _errorEvent.asSharedFlow()

    // Handle Result Flow with automatic error handling
    protected open fun <T> handleResultFlow(
        flow: Flow<Result<T>>,
        onSuccess: (T) -> Unit,
        onError: ((ErrorUtils.UserFriendlyError) -> Unit)? = null,
        onLoading: (() -> Unit)? = null,
        defaultErrorMessage: String = "Something went wrong. Please try again.",
        showAsEvent: Boolean = false,
        showLoading: Boolean = true,
        recoveryAction: ErrorUtils.RecoveryAction? = null
    ) {
        // Implementation...
    }

    // Other helper methods...
}
```

### Test Module Implementation

The test module provides mock implementations for testing:

```kotlin
// TestModule.kt
val testModule = module {
    // Mock repositories
    single<CocktailRepository> { mock() }
    single<CartRepository> { mock() }
    single<AuthRepository> { mock() }
    single<OrderRepository> { mock() }

    // Mock use cases
    factory { PlaceOrderUseCase(orderRepository = get()) }
    factory { ToggleFavoriteUseCase(cocktailRepository = get()) }

    // Other mock dependencies...
}
```

## Best Practices

1. **Separation of Concerns**: Keep modules focused on specific layers or functionality
2. **Constructor Injection**: Use constructor injection for dependencies
3. **Interface-Based Injection**: Inject interfaces rather than concrete implementations
4. **Minimal Dependencies**: Keep dependencies minimal and focused
5. **Documentation**: Document non-obvious dependencies and injection patterns
6. **Module Organization**: Group related dependencies in the same module
7. **Testability**: Design with testing in mind, making it easy to replace real implementations with mocks
8. **Consistency**: Follow consistent patterns for dependency injection across the codebase
9. **Cross-Platform Compatibility**:
   - Use shared interfaces for ViewModels
   - Keep platform-specific code in appropriate source sets
   - Use expect/actual declarations for platform-specific implementations
10. **Error Handling**:
    - Use the standardized ErrorHandler for consistent error management
    - Implement recovery actions for common error scenarios
    - Provide user-friendly error messages
11. **Coroutine Best Practices**:
    - Always use `viewModelScope.launch` for coroutines
    - Use `handleResultFlow` for handling Result-wrapped Flows
    - Use DispatcherProvider for testable coroutine dispatchers
    - Rename unused parameters to underscore (_) to follow Kotlin conventions
12. **Caching and Performance**:
    - Implement TTL (Time To Live) for cached data
    - Use appropriate scope for coroutines
    - Consider memory usage when caching data

## Benefits of the New Approach

The improved dependency injection setup provides several benefits:

1. **Improved Testability**: Easier to mock dependencies for testing
2. **Better Separation of Concerns**: Each module has a clear responsibility
3. **Reduced Boilerplate**: Standardized patterns reduce repetitive code
4. **Clearer Architecture**: Dependencies are more explicit and better organized
5. **Easier Maintenance**: Smaller, focused modules are easier to maintain
6. **Better Scalability**: Easier to add new features without modifying existing modules
7. **Improved Readability**: Code is more organized and easier to understand
8. **Consistent Error Handling**: Standardized error handling across all ViewModels
9. **Efficient Coroutine Usage**: Consistent patterns for using coroutines and handling asynchronous operations
10. **Better User Experience**: Consistent error handling and recovery options
