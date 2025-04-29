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
// Example of a ViewModel using the new advanced filtering use cases
class HomeViewModel(
    private val advancedFilterUseCase: AdvancedFilterUseCase,
    private val getFilterOptionsUseCase: GetFilterOptionsUseCase
) : BaseViewModel(), IHomeViewModel {

    // State for filter options
    private val _filterOptions = MutableStateFlow<FilterOptions>(FilterOptions())
    override val filterOptions: StateFlow<FilterOptions> = _filterOptions.asStateFlow()

    // State for filtered cocktails
    private val _cocktails = MutableStateFlow<List<Cocktail>>(emptyList())
    override val cocktails: StateFlow<List<Cocktail>> = _cocktails.asStateFlow()
    
    init {
        loadFilterOptions()
    }
    
    private fun loadFilterOptions() {
        viewModelScope.launch {
            handleResultFlow(
                flow = getFilterOptionsUseCase.getOptions(),
                onSuccess = { options ->
                    _filterOptions.value = options
                },
                onError = { _ ->
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to load filter options. Please try again.",
                recoveryAction = ErrorUtils.RecoveryAction("Retry") { loadFilterOptions() }
            )
        }
    }

    override fun applyFilters(filters: SearchFilters) {
        viewModelScope.launch {
            handleResultFlow(
                flow = advancedFilterUseCase.filter(filters),
                onSuccess = { filteredCocktails ->
                    _cocktails.value = filteredCocktails
                },
                onError = { _ ->
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to apply filters. Please try again.",
                recoveryAction = ErrorUtils.RecoveryAction("Retry") { applyFilters(filters) }
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

### 1. Platform-Specific Dependencies

#### Android-Specific Dependencies

```kotlin
// Android ViewModel Factory
class AndroidViewModelFactory(
    private val koin: Koin
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(
                    advancedFilterUseCase = koin.get(),
                    getFilterOptionsUseCase = koin.get(),
                    networkStatusUseCase = koin.get()
                ) as T
            }
            modelClass.isAssignableFrom(OrderViewModel::class.java) -> {
                OrderViewModel(
                    placeOrderUseCase = koin.get(),
                    manageOrdersUseCase = koin.get()
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

// Android Module
val androidModule = module {
    single { AndroidViewModelFactory(get()) }
    single { NetworkMonitor(get()) }
    single { ImageLoader(get()) }
}
```

#### iOS-Specific Dependencies

```kotlin
// iOS ViewModel Factory
class IOSViewModelFactory(
    private val koin: Koin,
    private val coroutineScope: CoroutineScope
) {
    fun createHomeViewModel(): IOSHomeViewModel = IOSHomeViewModel(
        advancedFilterUseCase = koin.get(),
        getFilterOptionsUseCase = koin.get(),
        networkStatusUseCase = koin.get(),
        coroutineScope = coroutineScope
    )

    fun createOrderViewModel(): IOSOrderViewModel = IOSOrderViewModel(
        placeOrderUseCase = koin.get(),
        manageOrdersUseCase = koin.get(),
        coroutineScope = coroutineScope
    )
}

// iOS Module
val iosModule = module {
    single { IOSViewModelFactory(get(), get()) }
    single { IOSNetworkMonitor() }
    single { IOSImageLoader() }
}
```

### 2. Advanced Testing Patterns

#### Testing with States and Events

```kotlin
class HomeViewModelTest : BaseKoinTest() {
    private lateinit var viewModel: HomeViewModel
    private val testScope = TestScope()
    private val testDispatcher = StandardTestDispatcher(testScope.testScheduler)
    
    @Before
    override fun setUp() {
        super.setUp()
        Dispatchers.setMain(testDispatcher)
        
        // Initialize state collectors
        val states = mutableListOf<HomeViewState>()
        val events = mutableListOf<HomeViewEvent>()
        
        testScope.launch {
            viewModel.state.collect { states.add(it) }
        }
        testScope.launch {
            viewModel.events.collect { events.add(it) }
        }
    }
    
    @Test
    fun `test state transitions during cocktail loading`() = testScope.runTest {
        // Given
        val cocktails = listOf(
            Cocktail(id = "1", name = "Margarita"),
            Cocktail(id = "2", name = "Mojito")
        )
        whenever(getCocktailsUseCase()).thenReturn(flow {
            emit(Result.Success(cocktails))
        })
        
        // When
        viewModel.loadCocktails()
        testScheduler.advanceUntilIdle()
        
        // Then
        assertEquals(
            listOf(
                HomeViewState.Initial,
                HomeViewState.Loading,
                HomeViewState.Success(cocktails)
            ),
            states
        )
    }
    
    @Test
    fun `test error handling with recovery`() = testScope.runTest {
        // Given
        val error = NetworkException(404, "Not found")
        whenever(getCocktailsUseCase())
            .thenReturn(flow { emit(Result.Error(error)) })
            .thenReturn(flow { emit(Result.Success(emptyList())) })
        
        // When - First attempt fails
        viewModel.loadCocktails()
        testScheduler.advanceUntilIdle()
        
        // Then - Error state
        assertTrue(states.last() is HomeViewState.Error)
        
        // When - Retry
        (states.last() as HomeViewState.Error).recoveryAction.invoke()
        testScheduler.advanceUntilIdle()
        
        // Then - Success state
        assertTrue(states.last() is HomeViewState.Success)
    }
}
```

#### Testing Platform-Specific Features

```kotlin
// Android-specific test
class AndroidHomeViewModelTest : BaseKoinTest() {
    @Test
    fun `test Android-specific image loading`() = runTest {
        // Given
        val imageLoader: ImageLoader = mock()
        whenever(imageLoader.load(any())).thenReturn(
            Result.Success(AndroidBitmap())
        )
        
        // When
        viewModel.loadCocktailImage("test.jpg")
        
        // Then
        verify(imageLoader).load("test.jpg")
        assertTrue(viewModel.cocktailImage.value is AndroidBitmap)
    }
}

// iOS-specific test
class IOSHomeViewModelTest : BaseKoinTest() {
    @Test
    fun `test iOS-specific image loading`() = runTest {
        // Given
        val imageLoader: IOSImageLoader = mock()
        whenever(imageLoader.load(any())).thenReturn(
            Result.Success(UIImage())
        )
        
        // When
        viewModel.loadCocktailImage("test.jpg")
        
        // Then
        verify(imageLoader).load("test.jpg")
        assertTrue(viewModel.cocktailImage.value is UIImage)
    }
}
```

### 3. Testing Utilities

```kotlin
// Test dispatchers provider
class TestDispatcherProvider : DispatcherProvider {
    private val testDispatcher = StandardTestDispatcher()
    
    override val main: CoroutineDispatcher = testDispatcher
    override val io: CoroutineDispatcher = testDispatcher
    override val default: CoroutineDispatcher = testDispatcher
}

// Flow test utilities
fun <T> Flow<T>.test(
    scope: TestScope,
    timeout: Duration = 1.seconds
): FlowTestCollector<T> = FlowTestCollector(this, scope, timeout)

class FlowTestCollector<T>(
    private val flow: Flow<T>,
    private val scope: TestScope,
    private val timeout: Duration
) {
    private val values = mutableListOf<T>()
    
    suspend fun assertValues(expected: List<T>) {
        scope.launch {
            flow.collect { values.add(it) }
        }
        scope.testScheduler.advanceTimeBy(timeout)
        assertEquals(expected, values)
    }
    
    suspend fun assertValue(expected: T) {
        assertValues(listOf(expected))
    }
}

// Example usage
@Test
fun `test flow values`() = runTest {
    viewModel.cocktails.test(this)
        .assertValues(listOf(
            emptyList(),
            listOf(Cocktail("1", "Margarita"))
        ))
}
```

### 4. Error Handling in Tests

```kotlin
@Test
fun `test network error handling`() = testScope.runTest {
    // Given
    val error = NetworkException(
        code = 404,
        message = "Not found"
    )
    whenever(getCocktailsUseCase()).thenReturn(flow {
        emit(Result.Error(error))
    })
    
    // When
    viewModel.loadCocktails()
    testScheduler.advanceUntilIdle()
    
    // Then
    with(states.last() as HomeViewState.Error) {
        assertEquals(404, (error as ViewError.Network).code)
        assertTrue(isRetryable)
        assertNotNull(recoveryAction)
    }
    
    assertTrue(events.any { it is HomeViewEvent.ShowError })
}

@Test
fun `test validation error handling`() = testScope.runTest {
    // Given
    val error = ValidationException(
        field = "name",
        message = "Name is required"
    )
    
    // When
    viewModel.validateInput("")
    testScheduler.advanceUntilIdle()
    
    // Then
    assertTrue(events.any {
        it is HomeViewEvent.ShowValidationError &&
        it.field == "name"
    })
}
```
