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
) : BaseViewModel(), ISomeViewModel {
    // Using viewModelScope from AndroidX ViewModel
    // This is automatically provided by the BaseViewModel
    
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

1. **viewModelScope**: Each ViewModel uses the `viewModelScope` provided by AndroidX ViewModel, which is automatically canceled when the ViewModel is cleared.

2. **Launching Coroutines**: Use `viewModelScope.launch` to start a new coroutine for asynchronous operations.

3. **Handling Result Flows**: Use the `handleResultFlow` helper method from BaseViewModel to handle Result-wrapped Flows consistently.

Example:

```kotlin
fun loadData() {
    viewModelScope.launch {
        handleResultFlow(
            flow = someUseCase.getData(),
            onSuccess = { data ->
                // Handle success
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

ViewModels handle errors in a user-friendly way using the BaseViewModel's error handling utilities. Here's how:

1. **Standardized Error Handling**: Use the `handleResultFlow` method to handle errors consistently across all ViewModels.

2. **User-Friendly Errors**: Errors are converted to user-friendly messages using the ErrorUtils class.

3. **Recovery Actions**: Provide recovery actions that users can take to resolve errors.

Example:

```kotlin
viewModelScope.launch {
    handleResultFlow(
        flow = someUseCase.getData(),
        onSuccess = { data ->
            _data.value = data
        },
        onError = { error ->
            // Custom error handling if needed
            // The base error handling is done by handleResultFlow
        },
        defaultErrorMessage = "Failed to load data. Please try again.",
        recoveryAction = ErrorUtils.RecoveryAction("Retry") { loadData() },
        showAsEvent = true // Show as a one-time event instead of persistent state
    )
}
```

## Resource Cleanup

ViewModels clean up resources when they're no longer needed. Here's how:

1. **Automatic Coroutine Cancellation**: The `viewModelScope` is automatically canceled when the ViewModel is cleared.

2. **Closing Resources**: Override the `onCleared()` method to close any additional resources.

Example:

```kotlin
override fun onCleared() {
    super.onCleared() // This handles coroutine cancellation
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

4. **Consistent Error Handling**: Use the BaseViewModel's error handling utilities consistently.

5. **Coroutine Best Practices**:
   - Always use `viewModelScope.launch` for coroutines
   - Use `handleResultFlow` for handling Result-wrapped Flows
   - Rename unused parameters to underscore (_) to follow Kotlin conventions

6. **Resource Cleanup**: Clean up resources when they're no longer needed.

7. **Testing**: Write unit tests for ViewModels to ensure they work correctly.

8. **Documentation**: Document the purpose and behavior of each ViewModel.

9. **Avoid Android Dependencies**: Keep ViewModels free of Android-specific dependencies to make them more testable and portable.

10. **Use Use Cases**: Delegate business logic to use cases instead of implementing it directly in ViewModels.
