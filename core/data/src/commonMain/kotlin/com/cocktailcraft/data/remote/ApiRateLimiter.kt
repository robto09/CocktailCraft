package com.cocktailcraft.data.remote

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The proactive min-interval throttle for TheCocktailDB's free API: callers
 * reserve a call slot at least [MIN_API_CALL_INTERVAL_MS] after the previous
 * one. Retry and 429 backoff are not here; Ktor's HttpRequestRetry owns
 * them.
 *
 * A Koin `single`, so every [CocktailRemoteDataSource] shares one budget.
 */
internal class ApiRateLimiter {

    private val mutex = Mutex()

    // The most recently reserved call slot (epoch millis; may be in the future).
    private var lastReservedSlot: Long = Long.MIN_VALUE / 2

    companion object {
        const val MIN_API_CALL_INTERVAL_MS = 1000L // 1 second between calls
    }

    /**
     * Atomically reserves the next free API call slot at or after [nowMs]
     * (at least [MIN_API_CALL_INTERVAL_MS] after the previously reserved one)
     * and returns how long the caller must wait before making its call.
     *
     * Check and record happen under one lock, so concurrent callers each get
     * their own slot instead of all reading the same "last call" time and
     * firing together. The lock is not held while the caller waits.
     */
    suspend fun reserveSlot(nowMs: Long): Long = mutex.withLock {
        val slot = maxOf(nowMs, lastReservedSlot + MIN_API_CALL_INTERVAL_MS)
        lastReservedSlot = slot
        slot - nowMs
    }
}
