package com.cocktailcraft.viewmodel

import co.touchlab.kermit.Logger
import com.cocktailcraft.util.ErrorHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

/**
 * Base ViewModel class for multiplatform ViewModels.
 * Provides common functionality including error handling, loading state management,
 * and Koin integration.
 */
abstract class SharedViewModel : KoinComponent {
    
    /**
     * ViewModel scope for launching coroutines.
     * Main-dispatched so state updates originate on the UI thread on both
     * platforms, matching AndroidX viewModelScope semantics. (On iOS,
     * Main.immediate currently behaves like Main - it always dispatches.)
     */
    protected val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state - can be observed for displaying error UI
    private val _error = MutableStateFlow<ErrorHandler.UserFriendlyError?>(null)
    val error: StateFlow<ErrorHandler.UserFriendlyError?> = _error.asStateFlow()

    // Error events - for one-time error handling (like showing a dialog)
    private val _errorEvent = MutableSharedFlow<ErrorHandler.UserFriendlyError>()
    val errorEvent: SharedFlow<ErrorHandler.UserFriendlyError> = _errorEvent.asSharedFlow()
    
    // Error string for legacy compatibility
    private val _errorString = MutableStateFlow("")
    val errorString: StateFlow<String> = _errorString.asStateFlow()

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
        recoveryAction: ErrorHandler.RecoveryAction? = null
    ) {
        val userFriendlyError = ErrorHandler.getErrorFromException(
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
        
        // Also set the error string for legacy compatibility
        _errorString.value = userFriendlyError.message

        // Log the error
        Logger.e("Error in ${this::class.simpleName}", exception)
    }

    /**
     * Set a user-friendly error directly
     */
    protected open fun setError(
        title: String,
        message: String,
        category: ErrorHandler.ErrorCategory = ErrorHandler.ErrorCategory.UNKNOWN,
        showAsEvent: Boolean = false,
        recoveryAction: ErrorHandler.RecoveryAction? = null
    ) {
        val userFriendlyError = ErrorHandler.UserFriendlyError(
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
        
        // Also set the error string for legacy compatibility
        _errorString.value = userFriendlyError.message
    }

    /**
     * Clear the current error
     */
    fun clearError() {
        _error.value = null
        _errorString.value = ""
    }

    /**
     * Execute a suspending operation with automatic error handling
     */
    protected open fun <T> executeWithErrorHandling(
        operation: suspend () -> T,
        onSuccess: (T) -> Unit,
        onError: ((ErrorHandler.UserFriendlyError) -> Unit)? = null,
        defaultErrorMessage: String = "Something went wrong. Please try again.",
        showAsEvent: Boolean = false,
        showLoading: Boolean = true,
        recoveryAction: ErrorHandler.RecoveryAction? = null
    ) {
        viewModelScope.launch {
            try {
                if (showLoading) {
                    setLoading(true)
                }

                val result = operation()
                onSuccess(result)

            } catch (e: Exception) {
                val error = ErrorHandler.getErrorFromException(
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
     * Clean up resources when ViewModel is no longer needed
     */
    open fun onCleared() {
        viewModelScope.cancel()
    }
}