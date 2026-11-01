package com.cocktailcraft.data.repository

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InFlightRequestsTest {

    /** Requests running in the test's background scope, standing in for the app scope. */
    private fun <T> TestScope.requests() = InFlightRequests<String, T>(backgroundScope)

    @Test
    fun callersArrivingMidFlightShareTheResult() = runTest {
        val requests = requests<String>()
        var runs = 0

        val results = List(3) {
            async {
                requests.run("k") {
                    runs++
                    delay(100)
                    "result $runs"
                }
            }
        }.awaitAll()

        assertEquals(1, runs)
        assertEquals(List(3) { "result 1" }, results)
    }

    @Test
    fun aCallAfterTheRequestFinishedStartsANewOne() = runTest {
        val requests = requests<Int>()
        var runs = 0

        requests.run("k") { ++runs }
        val second = requests.run("k") { ++runs }

        assertEquals(2, second)
    }

    @Test
    fun differentKeysRunIndependently() = runTest {
        val requests = requests<String>()

        val results = listOf("a", "b").map { key ->
            async {
                requests.run(key) {
                    delay(100)
                    key
                }
            }
        }.awaitAll()

        assertEquals(listOf("a", "b"), results)
    }

    @Test
    fun aCancelledCallerDoesNotCancelTheRequestItStarted() = runTest {
        // A screen that closes mid-refresh must not lose the fetch: it still
        // completes (and writes to the database) for everyone else
        val requests = requests<String>()
        val leaderStarted = CompletableDeferred<Unit>()
        var completed = false

        val leader = launch {
            requests.run("k") {
                leaderStarted.complete(Unit)
                delay(1_000)
                completed = true
                "leader"
            }
        }
        leaderStarted.await()
        val follower = async { requests.run("k") { "follower" } }
        leader.cancel()
        advanceUntilIdle()

        assertEquals("leader", follower.await())
        assertTrue(completed)
    }
}
