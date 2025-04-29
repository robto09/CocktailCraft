package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.util.ErrorHandler
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.ErrorUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * Base ViewModel class that provides common functionality for all ViewModels,
 * including standardized error handling, loading state management, and Koin integration.
 *
 * This class follows MVVM + Clean Architecture principles:
 * - Provides common state management for loading and errors
 * - Offers standardized error handling methods
 * - Includes utilities for executing operations with automatic error handling
 * - Supports both Android and iOS through platform-specific implementations
 */
abstract class BaseViewModel : KoinViewModel() {

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state - can be observed for displaying error UI
    private val _error = MutableStateFlow<ErrorUtils.UserFriendlyError?>(null)
    val error: StateFlow<ErrorUtils.UserFriendlyError?> = _error.asStateFlow()

    // Error events - for one-time error handling (like showing a dialog)
    private val _errorEvent = MutableSharedFlow<ErrorUtils.UserFriendlyError>()
    val errorEvent: SharedFlow<ErrorUtils.UserFriendlyError> = _errorEvent.asSharedFlow()

    /**
     * Set loading state
     */
    protected open fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    /**
     * Handle an exception and convert it to a user-friendly error
     */
    protected open fun handleException(
        exception: Throwable,
        defaultMessage: String = "Something went wrong. Please try again.",
        showAsEvent: Boolean = false,
        recoveryAction: ErrorUtils.RecoveryAction? = null
    ) {
        val userFriendlyError = ErrorUtils.getErrorFromException(
            exception = exception,
            defaultMessage = defaultMessage,
            recoveryAction = recoveryAction
        )

        if (showAsEvent) {
            // Emit as a one-time event
            viewModelScope.launch {
                _errorEvent.emit(userFriendlyError)
            }
        } else {
            // Set as a persistent state
            _error.value = userFriendlyError
        }

        // Always log the error
        println("Error in ${this::class.simpleName}: ${exception.message}")
        exception.printStackTrace()
    }

    /**
     * Set a user-friendly error directly
     */
    protected open fun setError(
        title: String,
        message: String,
        category: ErrorUtils.ErrorCategory = ErrorUtils.ErrorCategory.UNKNOWN,
        showAsEvent: Boolean = false,
        recoveryAction: ErrorUtils.RecoveryAction? = null
    ) {
        val userFriendlyError = ErrorUtils.UserFriendlyError(
            title = title,
            message = message,
            category = category,
            recoveryAction = recoveryAction
        )

        if (showAsEvent) {
            viewModelScope.launch {
                _errorEvent.emit(userFriendlyError)
            }
        } else {
            _error.value = userFriendlyError
        }
    }

    /**
     * Clear the current error
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Execute a suspending operation with automatic error handling
     */
    protected open fun <T> executeWithErrorHandling(
        operation: suspend () -> T,
        onSuccess: (T) -> Unit,
        onError: ((ErrorUtils.UserFriendlyError) -> Unit)? = null,
        defaultErrorMessage: String = "Something went wrong. Please try again.",
        showAsEvent: Boolean = false,
        showLoading: Boolean = true,
        recoveryAction: ErrorUtils.RecoveryAction? = null
    ) {
        viewModelScope.launch {
            try {
                if (showLoading) {
                    setLoading(true)
                }

                val result = operation()
                onSuccess(result)

            } catch (e: Exception) {
                val error = ErrorUtils.getErrorFromException(
                    exception = e,
                    defaultMessage = defaultErrorMessage,
                    recoveryAction = recoveryAction
                )

                if (showAsEvent) {
                    _errorEvent.emit(error)
                } else {
                    _error.value = error
                }

                onError?.invoke(error)

            } finally {
                if (showLoading) {
                    setLoading(false)
                }
            }
        }
    }

    /**
     * Handle a Flow of Result<T> with automatic error handling
     */
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
        viewModelScope.launch {
            flow
                .catch { e ->
                    // Handle exceptions from the flow
                    val error = ErrorUtils.getErrorFromException(
                        exception = e,
                        defaultMessage = defaultErrorMessage,
                        recoveryAction = recoveryAction
                    )

                    if (showAsEvent) {
                        _errorEvent.emit(error)
                    } else {
                        _error.value = error
                    }

                    onError?.invoke(error)

                    if (showLoading) {
                        setLoading(false)
                    }
                }
                .collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            if (showLoading) {
                                setLoading(false)
                            }
                            onSuccess(result.data)
                        }
                        is Result.Error -> {
                            if (showLoading) {
                                setLoading(false)
                            }

                            val error = ErrorUtils.createErrorFromErrorCode(
                                errorCode = result.code,
                                defaultMessage = result.message,
                                retryAction = recoveryAction?.action
                            )

                            if (showAsEvent) {
                                _errorEvent.emit(error)
                            } else {
                                _error.value = error
                            }

                            onError?.invoke(error)
                        }
                        is Result.Loading -> {
                            if (showLoading) {
                                setLoading(true)
                            }
                            onLoading?.invoke()
                        }
                    }
                }
        }
    }

    /**
     * Execute an operation with error handling and return a Flow.
     * This is useful for operations that need to return a Flow directly.
     *
     * @param operation The operation to execute
     * @param defaultErrorMessage The default error message to show if the operation fails
     * @param showLoading Whether to show loading state
     * @return Flow of the operation result
     */
    protected fun <T> executeWithErrorHandlingFlow(
        operation: suspend () -> Flow<T>,
        defaultErrorMessage: String = "An error occurred",
        showLoading: Boolean = true
    ): Flow<T> = flow {
        if (showLoading) {
            setLoading(true)
        }

        try {
            val resultFlow = operation()
            resultFlow.collect { value ->
                emit(value)
            }
        } catch (e: Exception) {
            // Set error state
            val errorMessage = e.message ?: defaultErrorMessage
            setError(
                title = "Error",
                message = errorMessage,
                category = ErrorUtils.ErrorCategory.DATA
            )

            // Re-throw the exception to propagate it to the caller
            throw e
        } finally {
            if (showLoading) {
                setLoading(false)
            }
        }
    }
}
