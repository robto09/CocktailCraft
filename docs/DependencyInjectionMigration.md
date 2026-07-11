# Dependency Injection Migration Guide

This document provides guidance on migrating existing ViewModels and tests to use the new dependency injection approach.

## Migrating ViewModels

### Before

```kotlin
class ExampleViewModel(
    private val repository: ExampleRepository? = null
) : ViewModel(), KoinComponent {

    // Use injected repository if not provided in constructor
    private val injectedRepository: ExampleRepository by inject()

    // Use the provided repository or the injected one
    private val repository: ExampleRepository
        get() = repository ?: injectedRepository
        
    // ViewModel implementation...
}
```

### After

```kotlin
class ExampleViewModel(
    private val repository: ExampleRepository? = null
) : BaseViewModel() {

    // Use injected repository if not provided in constructor
    private val injectedRepository: ExampleRepository by inject()

    // Use the provided repository or the injected one
    private val repository: ExampleRepository
        get() = repository ?: injectedRepository
        
    // ViewModel implementation...
}
```

## Migrating Tests

### Before

```kotlin
class ExampleViewModelTest {
    
    private lateinit var viewModel: ExampleViewModel
    private val repository: ExampleRepository = mock()
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ExampleViewModel(repository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    // Test methods...
}
```

### After

```kotlin
class ExampleViewModelTest : BaseKoinTest() {
    
    private lateinit var viewModel: ExampleViewModel
    private val repository: ExampleRepository by inject()
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    override fun setUp() {
        super.setUp() // Initialize Koin
        Dispatchers.setMain(testDispatcher)
        
        // Configure mocks
        whenever(repository.someMethod()).thenReturn(expectedValue)
        
        viewModel = ExampleViewModel()
    }
    
    @After
    override fun tearDown() {
        Dispatchers.resetMain()
        super.tearDown() // Clean up Koin
    }
    
    // Test methods...
}
```

## Step-by-Step Migration Process

1. **Update ViewModel Imports**:
   - Remove `import androidx.lifecycle.ViewModel`
   - Remove `import org.koin.core.component.KoinComponent`
   - Add `import org.koin.core.component.inject` if not already present

2. **Update ViewModel Inheritance**:
   - Change `ViewModel(), KoinComponent` to `BaseViewModel()`

3. **Update Tests**:
   - Make test class extend `BaseKoinTest`
   - Override `setUp()` and `tearDown()` methods
   - Call `super.setUp()` at the beginning of `setUp()`
   - Call `super.tearDown()` at the end of `tearDown()`
   - Replace manual mocking with Koin injection using `by inject()`
   - Configure mock behavior in `setUp()`

4. **Verify**:
   - Run tests to ensure they still pass
   - Check for any missing dependencies or configuration issues

## Common Issues and Solutions

### Issue: Missing Dependencies in Tests

If tests fail with missing dependencies, ensure the `testModule` provides all required dependencies:

```kotlin
// Add to TestModule.kt
single<MissingDependency> { mock() }
```

### Issue: Conflicting Modules

If you encounter conflicts between test modules and production modules:

```kotlin
// In your test setup
stopKoin() // Ensure Koin is stopped before starting
startKoin {
    modules(testModule)
}
```

### Issue: ViewModel Not Using Mocked Dependencies

Ensure you're using the ViewModel constructor without parameters to force Koin injection:

```kotlin
// Incorrect
viewModel = ExampleViewModel(repository) // Uses provided repository, not Koin

// Correct
viewModel = ExampleViewModel() // Uses Koin-injected repository
```
