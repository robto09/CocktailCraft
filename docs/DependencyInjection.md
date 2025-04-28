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
- **BaseViewModel**: Extended to inherit from `KoinViewModel`
- **Updated ViewModels**: Refactored to use the new base classes

This approach provides a consistent pattern for dependency injection in ViewModels, making the code more maintainable and easier to understand.

## Module Structure

The DI system is organized into the following modules:

### 1. Domain Module (`domainModule`)
- Contains domain-level dependencies
- Includes use cases and domain configurations
- Located in `shared/src/commonMain/kotlin/com/cocktailcraft/di/DomainModule.kt`

### 2. Data Module (`dataModule`)
- Contains data-layer dependencies
- Includes repositories and caching mechanisms
- Located in `shared/src/commonMain/kotlin/com/cocktailcraft/di/DataModule.kt`

### 3. Network Module (`networkModule`)
- Contains network-related dependencies
- Includes API clients, HTTP configuration, and network monitoring
- Located in `shared/src/commonMain/kotlin/com/cocktailcraft/di/NetworkModule.kt`

### 4. Platform Module (`platformModule`)
- Contains platform-specific dependencies
- Implemented differently for Android and iOS
- Located in platform-specific source sets

### 5. Test Module (`testModule`)
- Contains mock implementations for testing
- Used only in test environments
- Located in `androidApp/src/test/java/com/cocktailcraft/di/TestModule.kt`

## ViewModel Dependency Injection

ViewModels use a consistent pattern for dependency injection:

1. All ViewModels extend `BaseViewModel` which extends `KoinViewModel`
2. `KoinViewModel` implements `KoinComponent` to enable Koin injection
3. ViewModels support both constructor injection (for testing) and Koin injection (for production)

Example:
```kotlin
class ThemeViewModel(
    private val authRepository: AuthRepository? = null
) : BaseViewModel() {

    // Use injected repository if not provided in constructor
    private val injectedAuthRepository: AuthRepository by inject()

    // Use the provided repository or the injected one
    private val repository: AuthRepository
        get() = authRepository ?: injectedAuthRepository

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
    // HTTP Client
    single {
        HttpClient {
            // HTTP client configuration...
        }
    }

    // API
    single<CocktailApi> { CocktailApiImpl(get()) }

    // Network monitoring
    single { NetworkMonitor(get()) }
}

// DataModule.kt
val dataModule = module {
    // JSON
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    // Cache
    single { CocktailCache(get(), get(), get()) }

    // Repositories
    single<CocktailRepository> {
        CocktailRepositoryImpl(
            api = get(),
            settings = get(),
            appConfig = get(),
            json = get(),
            networkMonitor = get()
        )
    }
    // Other repositories...
}

// DomainModule.kt
val domainModule = module {
    // Config
    single<AppConfig> { AppConfigImpl() }

    // Use Cases
    factory { PlaceOrderUseCase(orderRepository = get()) }
    factory { ToggleFavoriteUseCase(cocktailRepository = get()) }
}
```

### ViewModel Base Classes

The ViewModel architecture uses a hierarchy of base classes:

```kotlin
// KoinViewModel.kt
abstract class KoinViewModel : ViewModel(), KoinComponent

// BaseViewModel.kt
abstract class BaseViewModel : KoinViewModel() {
    // Common ViewModel functionality...
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
2. **Constructor Injection**: Prefer constructor injection for testing
3. **Interface-Based Injection**: Inject interfaces rather than concrete implementations
4. **Minimal Dependencies**: Keep dependencies minimal and focused
5. **Documentation**: Document non-obvious dependencies and injection patterns
6. **Module Organization**: Group related dependencies in the same module
7. **Testability**: Design with testing in mind, making it easy to replace real implementations with mocks
8. **Consistency**: Follow consistent patterns for dependency injection across the codebase

## Benefits of the New Approach

The improved dependency injection setup provides several benefits:

1. **Improved Testability**: Easier to mock dependencies for testing
2. **Better Separation of Concerns**: Each module has a clear responsibility
3. **Reduced Boilerplate**: Standardized patterns reduce repetitive code
4. **Clearer Architecture**: Dependencies are more explicit and better organized
5. **Easier Maintenance**: Smaller, focused modules are easier to maintain
6. **Better Scalability**: Easier to add new features without modifying existing modules
7. **Improved Readability**: Code is more organized and easier to understand
