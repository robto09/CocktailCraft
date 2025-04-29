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
    private val exampleUseCase: ExampleUseCase
) : BaseViewModel(), IExampleViewModel {

    // State exposed to the UI
    private val _data = MutableStateFlow<List<ExampleData>>(emptyList())
    override val data: StateFlow<List<ExampleData>> = _data.asStateFlow()
    
    init {
        loadData()
    }
    
    override fun loadData() {
        viewModelScope.launch {
            handleResultFlow(
                flow = exampleUseCase.getData(),
                onSuccess = { data ->
                    _data.value = data
                },
                onError = { _ ->
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to load data. Please try again.",
                recoveryAction = ErrorUtils.RecoveryAction("Retry") { loadData() }
            )
        }
    }
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
   - Add `import androidx.lifecycle.viewModelScope`
   - Add `import kotlinx.coroutines.launch`
   - Add `import kotlinx.coroutines.flow.MutableStateFlow`
   - Add `import kotlinx.coroutines.flow.StateFlow`
   - Add `import kotlinx.coroutines.flow.asStateFlow`

2. **Update ViewModel Inheritance**:
   - Change `ViewModel(), KoinComponent` to `BaseViewModel(), IYourViewModel`
   - Create an interface for your ViewModel in the shared module if it doesn't exist

3. **Update ViewModel Implementation**:
   - Replace direct repository access with use cases
   - Use constructor injection instead of `by inject()`
   - Use `viewModelScope.launch` for all asynchronous operations
   - Use `handleResultFlow` for handling Result-wrapped Flows
   - Rename unused parameters to underscore (_) following Kotlin conventions

4. **Update Tests**:
   - Make test class extend `BaseKoinTest`
   - Override `setUp()` and `tearDown()` methods
   - Call `super.setUp()` at the beginning of `setUp()`
   - Call `super.tearDown()` at the end of `tearDown()`
   - Replace manual mocking with Koin injection using `by inject()`
   - Configure mock behavior in `setUp()`

5. **Verify**:
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

Ensure you're using the ViewModel constructor with the mocked dependencies:

```kotlin
// Incorrect
viewModel = ExampleViewModel() // Uses Koin-injected dependencies

// Correct
viewModel = ExampleViewModel(mockExampleUseCase) // Uses provided mock
```

### Issue: Coroutine Testing

For testing coroutines, use the `StandardTestDispatcher` and `runTest`:

```kotlin
@Test
fun `test loading data`() = runTest {
    // Arrange
    whenever(mockExampleUseCase.getData()).thenReturn(flow {
        emit(Result.Success(testData))
    })
    
    // Act
    viewModel.loadData()
    testScheduler.advanceUntilIdle() // Advance the test dispatcher
    
    // Assert
    assertEquals(testData, viewModel.data.value)
}
```

### Issue: Flow Testing

For testing Flows, collect the Flow in a test coroutine:

```kotlin
@Test
fun `test data flow`() = runTest {
    // Arrange
    val collectedValues = mutableListOf<List<ExampleData>>()
    val job = launch {
        viewModel.data.collect { collectedValues.add(it) }
    }
    
    // Act
    viewModel.loadData()
    testScheduler.advanceUntilIdle()
    
    // Assert
    assertEquals(2, collectedValues.size) // Initial empty list + loaded data
    assertEquals(testData, collectedValues.last())
    
    job.cancel() // Clean up
}
```
