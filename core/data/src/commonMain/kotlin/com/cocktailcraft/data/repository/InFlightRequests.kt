package com.cocktailcraft.data.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Collapses concurrent requests for the same key into one.
 *
 * The first caller for a key runs its block; callers that arrive while it is
 * in flight wait for it and then get its result instead of repeating the
 * request (the home screen and a filter opening the same category together,
 * a double pull-to-refresh). A caller that arrives after a request finished
 * starts a new one. Different keys run in parallel.
 *
 * If the running block is cancelled or throws, the next waiter runs its own
 * block rather than inheriting a result that never came. The map only grows
 * by key (a handful of categories, the cocktails opened this session).
 */
internal class InFlightRequests<K, T : Any> {

    private class Slot<T : Any> {
        val lock = Mutex()

        // Set each time a block completes, so a waiter can tell whether a
        // request finished while it queued.
        var completions = 0L
        var lastResult: T? = null
    }

    private val guard = Mutex()
    private val slots = mutableMapOf<K, Slot<T>>()

    suspend fun run(key: K, block: suspend () -> T): T {
        val (slot, seen) = guard.withLock {
            val slot = slots.getOrPut(key) { Slot() }
            slot to slot.completions
        }
        return slot.lock.withLock {
            val shared = guard.withLock { slot.lastResult.takeIf { slot.completions != seen } }
            shared ?: block().also { result ->
                guard.withLock {
                    slot.lastResult = result
                    slot.completions++
                }
            }
        }
    }
}
