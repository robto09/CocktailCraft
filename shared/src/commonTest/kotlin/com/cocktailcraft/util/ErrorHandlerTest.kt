package com.cocktailcraft.util

import com.cocktailcraft.domain.util.DomainException
import com.cocktailcraft.domain.util.ErrorCode
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.utils.io.errors.IOException
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/**
 * Locks in the behavior of the single error-construction path
 * ([ErrorHandler.getErrorFromException] + [ErrorHandler.errorFromCode]).
 */
class ErrorHandlerTest {

    // --- Exception classification (getErrorFromException) ---

    @Test
    fun ioExceptionIsClassifiedAsNetworkError() {
        val exception = IOException("socket closed")
        val error = ErrorHandler.getErrorFromException(exception)

        assertEquals(ErrorHandler.ErrorCategory.NETWORK, error.category)
        assertEquals(ErrorCode.NETWORK, error.errorCode)
        assertEquals("Network Error", error.title)
        assertEquals(
            "Unable to connect to the server. Please check your internet connection.",
            error.message
        )
        assertSame(exception, error.originalException)
    }

    @Test
    fun timeoutIsClassifiedAsNetworkTimeout() {
        val error = ErrorHandler.getErrorFromException(
            HttpRequestTimeoutException("https://example.com/api", 5_000L)
        )

        assertEquals(ErrorHandler.ErrorCategory.NETWORK, error.category)
        assertEquals(ErrorCode.TIMEOUT, error.errorCode)
        assertEquals("Connection Timeout", error.title)
        assertEquals(
            "The connection timed out. This might be due to slow internet or server issues. Please try again.",
            error.message
        )
    }

    @Test
    fun serializationExceptionIsClassifiedAsDataError() {
        val error = ErrorHandler.getErrorFromException(SerializationException("unexpected JSON"))

        assertEquals(ErrorHandler.ErrorCategory.DATA, error.category)
        assertEquals(ErrorCode.INVALID_DATA, error.errorCode)
        assertEquals("Data Error", error.title)
        assertEquals("Received unexpected data from the server. Please try again.", error.message)
    }

    @Test
    fun domainExceptionUsesItsTypedCodeAndMessage() {
        val error = ErrorHandler.getErrorFromException(
            DomainException(ErrorCode.NOT_FOUND, "Cocktail not found")
        )

        assertEquals(ErrorCode.NOT_FOUND, error.errorCode)
        assertEquals(ErrorHandler.ErrorCategory.DATA, error.category)
        assertEquals("Data Error", error.title)
        assertEquals("Cocktail not found", error.message)
    }

    @Test
    fun illegalArgumentExceptionKeepsItsMessageAsDataError() {
        val error = ErrorHandler.getErrorFromException(IllegalArgumentException("bad id"))

        assertEquals(ErrorHandler.ErrorCategory.DATA, error.category)
        assertEquals(ErrorCode.INVALID_DATA, error.errorCode)
        assertEquals("bad id", error.message)
    }

    @Test
    fun unknownExceptionFallsBackToDefaultMessage() {
        val error = ErrorHandler.getErrorFromException(RuntimeException("boom"))

        assertEquals(ErrorHandler.ErrorCategory.UNKNOWN, error.category)
        assertEquals(ErrorCode.UNKNOWN, error.errorCode)
        assertEquals("Error", error.title)
        assertEquals("Something went wrong. Please try again.", error.message)
    }

    @Test
    fun unknownExceptionHonoursCallerSuppliedDefaultMessage() {
        val error = ErrorHandler.getErrorFromException(
            RuntimeException("boom"),
            defaultMessage = "Failed to load cocktails."
        )

        assertEquals("Failed to load cocktails.", error.message)
    }

    @Test
    fun recoveryActionIsCarriedThrough() {
        val action = ErrorHandler.RecoveryAction("Retry") {}
        val error = ErrorHandler.getErrorFromException(
            IOException("offline"),
            recoveryAction = action
        )

        assertSame(action, error.recoveryAction)
    }

    // --- Error-code mapping (errorFromCode / categoryFor) ---

    @Test
    fun categoryForMapsEveryCode() {
        assertEquals(ErrorHandler.ErrorCategory.NETWORK, ErrorHandler.categoryFor(ErrorCode.NETWORK))
        assertEquals(ErrorHandler.ErrorCategory.NETWORK, ErrorHandler.categoryFor(ErrorCode.TIMEOUT))
        assertEquals(ErrorHandler.ErrorCategory.AUTHENTICATION, ErrorHandler.categoryFor(ErrorCode.UNAUTHORIZED))
        assertEquals(ErrorHandler.ErrorCategory.AUTHENTICATION, ErrorHandler.categoryFor(ErrorCode.FORBIDDEN))
        assertEquals(ErrorHandler.ErrorCategory.DATA, ErrorHandler.categoryFor(ErrorCode.INVALID_DATA))
        assertEquals(ErrorHandler.ErrorCategory.DATA, ErrorHandler.categoryFor(ErrorCode.NOT_FOUND))
        assertEquals(ErrorHandler.ErrorCategory.SERVER, ErrorHandler.categoryFor(ErrorCode.SERVER_ERROR))
        assertEquals(ErrorHandler.ErrorCategory.CLIENT, ErrorHandler.categoryFor(ErrorCode.CLIENT_ERROR))
        assertEquals(ErrorHandler.ErrorCategory.UNKNOWN, ErrorHandler.categoryFor(ErrorCode.UNKNOWN))
    }

    @Test
    fun errorFromCodeBuildsTitleFromCategoryAndKeepsMessage() {
        val error = ErrorHandler.errorFromCode(ErrorCode.SERVER_ERROR, "The server is on fire.")

        assertEquals("Server Error", error.title)
        assertEquals("The server is on fire.", error.message)
        assertEquals(ErrorHandler.ErrorCategory.SERVER, error.category)
        assertEquals(ErrorCode.SERVER_ERROR, error.errorCode)
    }

    @Test
    fun errorFromCodeFallsBackToRecoverySuggestionForBlankMessage() {
        val error = ErrorHandler.errorFromCode(ErrorCode.NETWORK, "  ")

        assertEquals("Check your internet connection and try again.", error.message)
    }
}
