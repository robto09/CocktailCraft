package com.cocktailcraft.di

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Locks in the single retry policy (SH-7): transient server errors and
 * TheCocktailDB's 429 rate limit retry; ordinary client errors don't.
 */
class NetworkRetryPolicyTest {

    @Test
    fun serverErrorsAndRateLimitAreRetryable() {
        assertTrue(shouldRetryResponse(500))
        assertTrue(shouldRetryResponse(503))
        assertTrue(shouldRetryResponse(599))
        assertTrue(shouldRetryResponse(429))
    }

    @Test
    fun ordinaryStatusesAreNotRetryable() {
        assertFalse(shouldRetryResponse(200))
        assertFalse(shouldRetryResponse(304))
        assertFalse(shouldRetryResponse(400))
        assertFalse(shouldRetryResponse(404))
        assertFalse(shouldRetryResponse(418))
    }
}
