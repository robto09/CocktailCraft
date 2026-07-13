package com.cocktailcraft.viewmodel

import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.ErrorHandler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Base ViewModel class for multiplatform ViewModels, on top of the official
 * multiplatform androidx [ViewModel] (Main-dispatched viewModelScope, cleared
 * automatically when owned by a ViewModelStore).
 * Dependencies are constructor-injected so subclasses can be built without a Koin container.
 */
abstract class SharedViewModel : ViewModel() {

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
        // Cancellation is normal teardown (screen dismissed / scope cleared
        // mid-flight), never a user-facing failure. Without this guard a
        // deep link that dismisses a sheet mid-load puts a
        // "StandaloneCoroutine was cancelled" alert over the new screen.
        if (exception is CancellationException) {
            Logger.d { "Cancelled in ${this::class.simpleName}" }
            return
        }

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
     * Public so manually-owned instances (iOS factory-scoped wrappers) can
     * clear themselves; store-owned instances are cleared by the platform.
     * Koin `single`-scoped ViewModels are NEITHER — nothing ever calls
     * onCleared() on them, so never put teardown logic here expecting it to
     * run for a singleton (see DomainModule's ViewModel scoping notes, SH-9).
     */
    public override fun onCleared() {
        super.onCleared()
    }
}
