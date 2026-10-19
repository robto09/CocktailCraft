package com.cocktailcraft.di

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Locks in the single retry policy (SH-7): transient server errors and
 * TheCocktailDB's 429 rate limit retry; ordinary client errors don't.
 * Transport failures retry only when a second attempt can help.
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

    @Test
    fun aDroppedConnectionIsRetryable() {
        assertTrue(shouldRetryException(IOException("Connection reset")))
    }

    @Test
    fun timeoutsUnreachableHostsAndCancellationAreNotRetried() {
        assertFalse(shouldRetryException(HttpRequestTimeoutException("https://example.com", 10_000L)))
        assertFalse(shouldRetryException(ConnectTimeoutException("connect timeout")))
        assertFalse(shouldRetryException(UnresolvedAddressException()))
        assertFalse(shouldRetryException(CancellationException("stopped")))
    }

    @Test
    fun theOnlyRetryWaitsWellUnderASecond() {
        // AppConfigImpl.maxRetries is 1, so the backoff is the base delay
        // plus jitter: far inside the 10 second request budget
        assertTrue(RetryBackoff.BASE_DELAY_MS + RetryBackoff.JITTER_MS <= 500L)
    }
}
