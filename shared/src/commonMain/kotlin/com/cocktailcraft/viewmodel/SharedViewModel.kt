package com.cocktailcraft.viewmodel

import co.touchlab.kermit.Logger
import com.cocktailcraft.util.ErrorHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    // The single error channel, observed by both platforms for error UI
    private val _error = MutableStateFlow<ErrorHandler.UserFriendlyError?>(null)
    val error: StateFlow<ErrorHandler.UserFriendlyError?> = _error.asStateFlow()

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
        recoveryAction: ErrorHandler.RecoveryAction? = null
    ) {
        _error.value = ErrorHandler.getErrorFromException(
            exception = exception,
            defaultMessage = defaultMessage,
            recoveryAction = recoveryAction
        )

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
        recoveryAction: ErrorHandler.RecoveryAction? = null
    ) {
        _error.value = ErrorHandler.UserFriendlyError(
            title = title,
            message = message,
            category = category,
            recoveryAction = recoveryAction
        )
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
        onError: ((ErrorHandler.UserFriendlyError) -> Unit)? = null,
        defaultErrorMessage: String = "Something went wrong. Please try again.",
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

                _error.value = error

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
