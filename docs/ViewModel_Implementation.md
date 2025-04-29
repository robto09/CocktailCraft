# ViewModel Implementation Guide

This document explains the implementation pattern for ViewModels in the CocktailCraft app, focusing on how to handle asynchronous operations using coroutines and Flow.

## Table of Contents

1. [Overview](#overview)
2. [ViewModel Structure](#viewmodel-structure)
3. [Coroutine Handling](#coroutine-handling)
4. [State Management](#state-management)
5. [Error Handling](#error-handling)
6. [Resource Cleanup](#resource-cleanup)
7. [Cross-Platform Considerations](#cross-platform-considerations)
8. [Best Practices](#best-practices)

## Overview

The CocktailCraft app follows the MVVM (Model-View-ViewModel) architecture pattern with Clean Architecture principles. ViewModels are responsible for:

- Managing UI-related data in a lifecycle-conscious way
- Handling user interactions
- Processing data from the domain layer
- Exposing state to the UI layer

## ViewModel Structure

Each ViewModel in the app follows this general structure:

```kotlin
class SomeViewModel(
    // Dependencies injected via constructor
    private val someUseCase: SomeUseCase,
    private val anotherUseCase: AnotherUseCase
) : ISomeViewModel {
    // Coroutine scope for this ViewModel
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // State exposed to the UI
    override val someState = MutableStateFlow<SomeType>(initialValue)
    override val anotherState = MutableStateFlow<AnotherType>(initialValue)
    
    // Initialization
    init {
        // Initialize state, start monitoring, etc.
        loadInitialData()
    }
    
    // Actions that can be triggered by the UI
    override fun someAction(param: SomeType) {
        viewModelScope.launch {
            // Update state, call use cases, etc.
        }
    }
    
    // Clean up resources
    fun onCleared() {
        viewModelScope.cancel()
    }
}
```

## Coroutine Handling

ViewModels use coroutines to handle asynchronous operations. Here's how:

1. **CoroutineScope**: Each ViewModel creates its own `CoroutineScope` with a `SupervisorJob` to ensure that failures in one coroutine don't affect others.

2. **Launching Coroutines**: Use `viewModelScope.launch` to start a new coroutine for asynchronous operations.

3. **Collecting Flows**: Use `collect` or `collectLatest` to collect values from a Flow.

Example:

```kotlin
fun loadData() {
    viewModelScope.launch {
        isLoading.value = true
        
        try {
            someUseCase.getData()
                .catch { error ->
                    // Handle error
                    isLoading.value = false
                }
                .collect { result ->
                    isLoading.value = false
                    if (result.isSuccess) {
                        data.value = result.getOrNull() ?: emptyList()
                    }
                }
        } catch (e: Exception) {
            // Handle unexpected errors
            isLoading.value = false
        }
    }
}
```

## State Management

ViewModels use `StateFlow` to expose state to the UI. Here's how:

1. **Defining State**: Use `MutableStateFlow` for state that can be updated by the ViewModel.

2. **Updating State**: Update state by setting the `value` property of the `MutableStateFlow`.

3. **Exposing State**: Expose state as `StateFlow` to the UI.

Example:

```kotlin
// Define state
private val _data = MutableStateFlow<List<Item>>(emptyList())
val data: StateFlow<List<Item>> = _data

// Update state
_data.value = newData
```

## Error Handling

ViewModels handle errors in a user-friendly way. Here's how:

1. **Catching Exceptions**: Use `try-catch` blocks to catch exceptions.

2. **Handling Flow Errors**: Use `catch` operator to handle errors in Flows.

3. **Updating State**: Update state to reflect errors.

Example:

```kotlin
viewModelScope.launch {
    try {
        someUseCase.getData()
            .catch { error ->
                // Handle specific Flow errors
                errorState.value = "An error occurred: ${error.message}"
            }
            .collect { result ->
                if (result.isSuccess) {
                    data.value = result.getOrNull() ?: emptyList()
                } else {
                    // Handle Result.failure
                    errorState.value = "Failed to load data"
                }
            }
    } catch (e: Exception) {
        // Handle unexpected errors
        errorState.value = "An unexpected error occurred"
    }
}
```

## Resource Cleanup

ViewModels clean up resources when they're no longer needed. Here's how:

1. **Cancelling Coroutines**: Call `viewModelScope.cancel()` in the `onCleared()` method.

2. **Closing Resources**: Close any resources that need to be closed.

Example:

```kotlin
fun onCleared() {
    viewModelScope.cancel()
    // Close other resources if needed
}
```

## Cross-Platform Considerations

The CocktailCraft app is designed to be cross-platform. Here's how ViewModels handle this:

1. **Shared Interfaces**: ViewModels implement shared interfaces defined in the common module.

2. **Platform-Specific Implementations**: Each platform provides its own implementation of the shared interfaces.

3. **Common State**: State is defined using Kotlin Multiplatform's `StateFlow`.

Example:

```kotlin
// Shared interface in commonMain
interface ISomeViewModel {
    val someState: StateFlow<SomeType>
    fun someAction(param: SomeType)
}

// Android implementation in androidMain
class SomeViewModel(
    private val someUseCase: SomeUseCase
) : ISomeViewModel {
    override val someState = MutableStateFlow<SomeType>(initialValue)
    
    override fun someAction(param: SomeType) {
        // Android-specific implementation
    }
}

// iOS implementation in iosMain
class SomeViewModel(
    private val someUseCase: SomeUseCase
) : ISomeViewModel {
    override val someState = MutableStateFlow<SomeType>(initialValue)
    
    override fun someAction(param: SomeType) {
        // iOS-specific implementation
    }
}
```

## Best Practices

1. **Single Responsibility**: Each ViewModel should have a single responsibility.

2. **Dependency Injection**: Use constructor injection to provide dependencies to ViewModels.

3. **State Immutability**: Treat state as immutable. Create new state objects instead of modifying existing ones.

4. **Error Handling**: Handle errors gracefully and provide user-friendly error messages.

5. **Resource Cleanup**: Clean up resources when they're no longer needed.

6. **Testing**: Write unit tests for ViewModels to ensure they work correctly.

7. **Documentation**: Document the purpose and behavior of each ViewModel.

8. **Consistent Naming**: Use consistent naming conventions for ViewModels and their methods.

9. **Avoid Android Dependencies**: Keep ViewModels free of Android-specific dependencies to make them more testable and portable.

10. **Use Use Cases**: Delegate business logic to use cases instead of implementing it directly in ViewModels.
