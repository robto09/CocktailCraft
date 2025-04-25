package com.cocktailcraft.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocktailcraft.util.ErrorUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel class that provides common functionality for all ViewModels,
 * including standardized error handling and loading state management.
 */
abstract class BaseViewModel : ViewModel() {

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
}
