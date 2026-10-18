package com.cocktailcraft.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.error_generic
import com.cocktailcraft.resources.undo
import com.cocktailcraft.util.ErrorHandler
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel class for multiplatform ViewModels, on top of the official
 * multiplatform androidx [ViewModel] (Main-dispatched viewModelScope, cleared
 * automatically when owned by a ViewModelStore).
 * Dependencies are constructor-injected so subclasses can be built without a Koin container.
 */
abstract class SharedViewModel : ViewModel() {

    // The single error channel, observed by both platforms for error UI.
    // A consumable StateFlow (survives a moment with no collector) that the
    // UI clears with clearError() once shown. Every load/action also clears
    // it when it starts, so a failure never outlives the next attempt — the
    // Koin-single ViewModels would otherwise carry one failure for the whole
    // session. Everything else a screen renders lives in the subclass's
    // single uiState — there is deliberately no parallel isLoading flow here.
    private val _error = MutableStateFlow<ErrorHandler.UserFriendlyError?>(null)
    val error: StateFlow<ErrorHandler.UserFriendlyError?> = _error.asStateFlow()

    // One-off outcomes for toasts/snackbars (see [UiEvent]). Broadcast with
    // no replay: whoever is collecting when the event fires shows it, and an
    // event nobody is listening for is dropped rather than replayed onto a
    // later screen. Anything that must not be missed belongs in uiState or
    // [error] instead. The small buffer keeps [sendEvent] non-suspending.
    private val _events = MutableSharedFlow<UiEvent>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    /** Announce a one-off outcome on [events]. */
    protected fun sendEvent(event: UiEvent) {
        _events.tryEmit(event)
    }

    /**
     * An Undo button for a toast: [undo] runs once, on viewModelScope, when
     * the user taps it — so a Koin-single ViewModel restores the item even
     * if the screen that showed the toast is gone by then.
     */
    protected fun undoAction(undo: suspend () -> Unit): UiEventAction =
        UiEventAction(MR.strings.undo.desc()) {
            viewModelScope.launch { undo() }
        }

    /**
     * Handle an exception and convert it to a user-friendly error.
     * [defaultMessage] is a shared string, resolved for the user's locale by
     * the platform UI.
     */
    protected open fun handleException(
        exception: Throwable,
        defaultMessage: StringResource = MR.strings.error_generic,
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
            defaultMessage = defaultMessage.desc(),
            recoveryAction = recoveryAction
        )

        // Log the error
        Logger.e("Error in ${this::class.simpleName}", exception)
    }

    /**
     * Set a user-friendly error directly. [message] is usually a shared
     * string (see the StringResource overload) but may be formatted or raw.
     */
    protected open fun setError(
        title: StringResource,
        message: StringDesc,
        category: ErrorHandler.ErrorCategory = ErrorHandler.ErrorCategory.UNKNOWN,
        recoveryAction: ErrorHandler.RecoveryAction? = null
    ) {
        _error.value = ErrorHandler.UserFriendlyError(
            title = title.desc(),
            message = message,
            category = category,
            recoveryAction = recoveryAction
        )
    }

    protected fun setError(
        title: StringResource,
        message: StringResource,
        category: ErrorHandler.ErrorCategory = ErrorHandler.ErrorCategory.UNKNOWN,
        recoveryAction: ErrorHandler.RecoveryAction? = null
    ) = setError(title, message.desc(), category, recoveryAction)

    /**
     * Clear the current error. The UI calls this once the error is shown;
     * subclasses call it at the start of every load/action.
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Unwrap a [Result], reporting a [Result.Error] to the error channel
     * (typed via its ErrorCode) instead of silently discarding it. The
     * error's own (raw) message wins; [fallbackMessage] covers a blank one.
     */
    protected fun <T> Result<T>.getOrReport(
        default: T,
        fallbackMessage: StringResource = MR.strings.error_generic,
        recoveryAction: ErrorHandler.RecoveryAction? = null
    ): T = when (this) {
        is Result.Success -> data
        is Result.Error -> {
            _error.value = ErrorHandler.errorFromCode(
                code = code,
                message = ErrorHandler.rawMessage(message) ?: fallbackMessage.desc(),
                recoveryAction = recoveryAction
            )
            Logger.e { "Error in ${this@SharedViewModel::class.simpleName}: $message ($code)" }
            default
        }
    }

    /**
     * Public so manually-owned instances (iOS factory-scoped wrappers) can
     * clear themselves; store-owned instances are cleared by the platform.
     * Koin `single`-scoped ViewModels are NEITHER — nothing ever calls
     * onCleared() on them, so never put teardown logic here expecting it to
     * run for a singleton (see PresentationModule's ViewModel scoping notes, SH-9).
     */
    public override fun onCleared() {
        super.onCleared()
    }
}
