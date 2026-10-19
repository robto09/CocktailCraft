package com.cocktailcraft.viewmodel

import dev.icerock.moko.resources.desc.StringDesc

/**
 * A one-off outcome a screen announces with a toast/snackbar and then
 * forgets — "Margarita added to cart", "Password changed". Delivered through
 * [SharedViewModel.events], never through uiState.
 *
 * Failures that a screen has to render (an error state, an alert) stay on
 * [SharedViewModel.error]; [Failure] is for an action whose outcome belongs
 * to whatever screen triggered it rather than to the ViewModel's own screen
 * (add-to-cart runs from Home, Favorites and Detail — parking that failure on
 * the cart's error channel would pop it up later on the Cart tab).
 * [message] is a shared string, resolved for the user's locale by the UI.
 *
 * [action], when set, is a button the toast offers next to the message —
 * Undo after a removal. A toast with an action stays up long enough to be
 * reached (with TalkBack/VoiceOver too); one without is the plain toast.
 */
sealed class UiEvent {
    abstract val message: StringDesc
    abstract val action: UiEventAction?

    data class Success(override val message: StringDesc, override val action: UiEventAction? = null) : UiEvent()

    data class Failure(override val message: StringDesc, override val action: UiEventAction? = null) : UiEvent()
}

/**
 * A button offered on a [UiEvent]'s toast, e.g. Undo. [label] is a shared
 * string. [perform] runs the ViewModel's handler at most once — a second tap,
 * or a tap plus an accessibility action, does nothing — and returns at once:
 * the work runs on the ViewModel's scope and reports back like any other
 * action (state, error, events). Call it on the main thread.
 *
 * Deliberately not a data class: two Undo actions for the same item are
 * still different actions, so every event carrying one is distinct (SwiftUI's
 * onChange only fires for a value that differs from the last one).
 */
class UiEventAction internal constructor(val label: StringDesc, private val onPerform: () -> Unit) {
    private var performed = false

    /** Whether [perform] has already run. */
    val isPerformed: Boolean get() = performed

    fun perform() {
        if (performed) return
        performed = true
        onPerform()
    }
}
