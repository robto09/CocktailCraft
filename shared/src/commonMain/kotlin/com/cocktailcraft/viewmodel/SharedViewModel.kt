package com.cocktailcraft.viewmodel

import co.touchlab.kermit.Logger
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.ErrorHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Base ViewModel class for multiplatform ViewModels.
 * Provides common functionality including error handling and loading state management.
 * Dependencies are constructor-injected so subclasses can be built without a Koin container.
 */
abstract class SharedViewModel {

    /**
     * ViewModel scope for launching coroutines.
     * Main-dispatched so state updates originate on the UI thread on both
     * platforms, matching AndroidX viewModelScope semantics. (On iOS,
     * Main.immediate currently behaves like Main - it always dispatches.)
     */
    protected val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    // The single error channel, observed by both platforms for error UI.
    // Loading state lives in each screen's UiState — there is deliberately
    // no parallel isLoading flow here.
    private val _error = MutableStateFlow<ErrorHandler.UserFriendlyError?>(null)
    val error: StateFlow<ErrorHandler.UserFriendlyError?> = _error.asStateFlow()

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
     * Unwrap a [Result], reporting a [Result.Error] to the error channel
     * (typed via its ErrorCode) instead of silently discarding it.
     */
    protected fun <T> Result<T>.getOrReport(
        default: T,
        fallbackMessage: String = "Something went wrong. Please try again.",
        recoveryAction: ErrorHandler.RecoveryAction? = null
    ): T = when (this) {
        is Result.Success -> data
        is Result.Error -> {
            _error.value = ErrorHandler.errorFromCode(
                code = code,
                message = message.ifBlank { fallbackMessage },
                recoveryAction = recoveryAction
            )
            Logger.e { "Error in ${this@SharedViewModel::class.simpleName}: $message ($code)" }
            default
        }
        is Result.Loading -> default
    }

    /**
     * Clean up resources when ViewModel is no longer needed
     */
    open fun onCleared() {
        viewModelScope.cancel()
    }
}
