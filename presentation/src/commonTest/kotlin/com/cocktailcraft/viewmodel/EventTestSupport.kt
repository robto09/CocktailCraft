package com.cocktailcraft.viewmodel

import app.cash.turbine.test

/**
 * Runs [block] while collecting [SharedViewModel.events] (which has no
 * replay) and returns the one event it announced; fails if it announced
 * none, or more than one.
 */
internal suspend fun SharedViewModel.eventFrom(block: suspend () -> Unit): UiEvent {
    var event: UiEvent? = null
    events.test {
        block()
        event = awaitItem()
    }
    return checkNotNull(event)
}

/** The Undo [UiEventAction] of the one event [block] announced. */
internal suspend fun SharedViewModel.undoFrom(block: suspend () -> Unit): UiEventAction =
    checkNotNull(eventFrom(block).action) { "the event offers no action" }
