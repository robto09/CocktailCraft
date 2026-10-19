package com.cocktailcraft.util

import com.cocktailcraft.domain.util.DomainException
import com.cocktailcraft.domain.util.ErrorCode
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrThrow
import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.coroutines.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ResultCatchingTest {

    @Test
    fun successReturnsTheBlockResult() {
        val result = runCatchingResult("fallback") { Result.Success(42) }
        assertEquals(42, (result as Result.Success).data)
    }

    @Test
    fun exceptionIsClassifiedByTypeNotDefaultedToUnknown() {
        val result = runCatchingResult<Int>("fallback") { throw IllegalArgumentException("bad input") }

        val error = result as Result.Error
        assertEquals(ErrorCode.INVALID_DATA, error.code, "AR-2: the type classifier must assign the code")
    }

    @Test
    fun exceptionMessageIsKeptOutOfTheErrorMessage() {
        // Ktor's messages carry the request URL (search terms); the error
        // message is logged, so it holds only the description and the type
        val result = runCatchingResult<Int>("Failed to search") {
            throw IllegalStateException("GET https://example.com/search.php?s=secret failed")
        }

        val error = result as Result.Error
        assertEquals("Failed to search (IllegalStateException)", error.message)
    }

    @Test
    fun domainExceptionKeepsItsOriginalTypedCode() {
        val result = runCatchingResult<Int>("fallback") {
            throw DomainException(ErrorCode.NOT_FOUND, "no such cocktail")
        }

        val error = result as Result.Error
        assertEquals(ErrorCode.NOT_FOUND, error.code, "a nested getOrThrow() must not lose its code")
        assertEquals("no such cocktail", error.message)
    }

    @Test
    fun displayMessageSurvivesGetOrThrowAndRunCatchingResult() {
        val localized = StringDesc.Raw("localized copy")
        val original = Result.Error("raw", ErrorCode.FORBIDDEN, displayMessage = localized)

        val thrown = assertFailsWith<DomainException> { original.getOrThrow() }
        assertEquals(localized, thrown.displayMessage)

        val rewrapped = runCatchingResult<Int>("fallback") { original.getOrThrow() }
        assertEquals(original, rewrapped)
    }

    @Test
    fun cancellationIsRethrownNotWrapped() {
        assertFailsWith<CancellationException> {
            runCatchingResult<Int>("fallback") { throw CancellationException("cancelled") }
        }
    }

    @Test
    fun messagelessExceptionStillCarriesTheDefaultMessage() {
        val result = runCatchingResult<Int>("Failed to place order") { throw RuntimeException() }

        val error = result as Result.Error
        assertEquals("Failed to place order (RuntimeException)", error.message)
    }

    @Test
    fun businessRuleErrorReturnedByBlockPassesThroughUntouched() {
        val result = runCatchingResult<Int>("fallback") {
            Result.Error("account locked", ErrorCode.FORBIDDEN)
        }

        val error = result as Result.Error
        assertEquals(ErrorCode.FORBIDDEN, error.code)
        assertTrue(result.isError())
    }

    @Test
    fun nonCancellationHelperReturnsFallbackForOrdinaryFailures() {
        val value = runCatchingNonCancellation(onFailure = { "fallback: ${it.message}" }) {
            throw IllegalStateException("boom")
        }
        assertEquals("fallback: boom", value)
    }

    @Test
    fun nonCancellationHelperRethrowsCancellation() {
        assertFailsWith<CancellationException> {
            runCatchingNonCancellation(onFailure = { "swallowed" }) {
                throw CancellationException("cancelled")
            }
        }
    }
}
