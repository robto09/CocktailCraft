package com.cocktailcraft.util

import com.cocktailcraft.domain.util.DomainException
import com.cocktailcraft.domain.util.ErrorCode
import com.cocktailcraft.domain.util.Result
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
        assertEquals("bad input", error.message)
        assertEquals(ErrorCode.INVALID_DATA, error.code, "AR-2: the type classifier must assign the code")
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
    fun cancellationIsRethrownNotWrapped() {
        assertFailsWith<CancellationException> {
            runCatchingResult<Int>("fallback") { throw CancellationException("cancelled") }
        }
    }

    @Test
    fun messagelessExceptionFallsBackToDefaultMessage() {
        val result = runCatchingResult<Int>("Failed to place order") { throw RuntimeException() }

        val error = result as Result.Error
        assertEquals("Failed to place order", error.message)
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
}
