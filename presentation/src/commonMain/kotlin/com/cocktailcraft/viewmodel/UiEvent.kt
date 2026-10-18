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
 */
sealed class UiEvent {
    abstract val message: StringDesc

    data class Success(override val message: StringDesc) : UiEvent()

    data class Failure(override val message: StringDesc) : UiEvent()
}
