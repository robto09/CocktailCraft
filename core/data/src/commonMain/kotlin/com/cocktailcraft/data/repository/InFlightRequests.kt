package com.cocktailcraft.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Collapses concurrent requests for the same key into one.
 *
 * The first caller for a key starts its block; callers that arrive while it is
 * in flight wait for it and then get its result instead of repeating the
 * request (the home screen and a filter opening the same category together,
 * a double pull-to-refresh). A caller that arrives after a request finished
 * starts a new one. Different keys run in parallel.
 *
 * Blocks run in [scope], the app-wide scope, not in the caller's coroutine:
 * a caller that goes away (its screen closed, its ViewModel cleared) stops
 * waiting, but the request it started still completes and writes its result
 * to the database, and anyone else waiting on it still gets that result. The
 * map only grows by key (a handful of categories, the cocktails opened this
 * session).
 */
internal class InFlightRequests<K, T>(private val scope: CoroutineScope) {

    private val guard = Mutex()
    private val requests = mutableMapOf<K, Deferred<T>>()

    suspend fun run(key: K, block: suspend () -> T): T {
        val request = guard.withLock {
            requests[key]?.takeUnless { it.isCompleted }
                ?: scope.async { block() }.also { requests[key] = it }
        }
        return request.await()
    }
}
