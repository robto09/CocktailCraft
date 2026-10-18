package com.cocktailcraft.data.repository

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class InFlightRequestsTest {

    @Test
    fun callersArrivingMidFlightShareTheResult() = runTest {
        val requests = InFlightRequests<String, String>()
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
        val requests = InFlightRequests<String, Int>()
        var runs = 0

        requests.run("k") { ++runs }
        val second = requests.run("k") { ++runs }

        assertEquals(2, second)
    }

    @Test
    fun differentKeysRunIndependently() = runTest {
        val requests = InFlightRequests<String, String>()

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
    fun aWaiterRunsItsOwnRequestWhenTheLeaderIsCancelled() = runTest {
        val requests = InFlightRequests<String, String>()
        val leaderStarted = CompletableDeferred<Unit>()

        val leader = launch {
            requests.run("k") {
                leaderStarted.complete(Unit)
                delay(1_000)
                "leader"
            }
        }
        leaderStarted.await()
        val follower = async { requests.run("k") { "follower" } }
        leader.cancel()

        assertEquals("follower", follower.await())
    }
}
