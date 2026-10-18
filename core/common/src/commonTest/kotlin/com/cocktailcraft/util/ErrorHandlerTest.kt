package com.cocktailcraft.util

import com.cocktailcraft.domain.util.DomainException
import com.cocktailcraft.domain.util.ErrorCode
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.common_retry
import com.cocktailcraft.resources.error
import com.cocktailcraft.resources.error_generic
import com.cocktailcraft.resources.error_load_cocktails_failed
import com.cocktailcraft.resources.error_message_invalid_payload
import com.cocktailcraft.resources.error_message_network
import com.cocktailcraft.resources.error_message_timeout
import com.cocktailcraft.resources.error_suggestion_network
import com.cocktailcraft.resources.error_title_data
import com.cocktailcraft.resources.error_title_network
import com.cocktailcraft.resources.error_title_server
import com.cocktailcraft.resources.error_title_timeout
import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.utils.io.errors.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlinx.serialization.SerializationException

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
        assertEquals(MR.strings.error_title_network.desc(), error.title)
        assertEquals(
            MR.strings.error_message_network.desc(),
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
        assertEquals(MR.strings.error_title_timeout.desc(), error.title)
        assertEquals(
            MR.strings.error_message_timeout.desc(),
            error.message
        )
    }

    @Test
    fun serializationExceptionIsClassifiedAsDataError() {
        val error = ErrorHandler.getErrorFromException(SerializationException("unexpected JSON"))

        assertEquals(ErrorHandler.ErrorCategory.DATA, error.category)
        assertEquals(ErrorCode.INVALID_DATA, error.errorCode)
        assertEquals(MR.strings.error_title_data.desc(), error.title)
        assertEquals(MR.strings.error_message_invalid_payload.desc(), error.message)
    }

    @Test
    fun domainExceptionUsesItsTypedCodeAndMessage() {
        val error = ErrorHandler.getErrorFromException(
            DomainException(ErrorCode.NOT_FOUND, "Cocktail not found")
        )

        assertEquals(ErrorCode.NOT_FOUND, error.errorCode)
        assertEquals(ErrorHandler.ErrorCategory.DATA, error.category)
        assertEquals(MR.strings.error_title_data.desc(), error.title)
        assertEquals(StringDesc.Raw("Cocktail not found"), error.message)
    }

    @Test
    fun domainExceptionDisplayMessageWinsOverItsRawMessage() {
        val localized = MR.strings.error_load_cocktails_failed.desc()

        val error = ErrorHandler.getErrorFromException(
            DomainException(ErrorCode.FORBIDDEN, "raw log text", displayMessage = localized)
        )

        assertEquals(ErrorCode.FORBIDDEN, error.errorCode)
        assertEquals(localized, error.message)
    }

    @Test
    fun illegalArgumentExceptionKeepsItsMessageAsDataError() {
        val error = ErrorHandler.getErrorFromException(IllegalArgumentException("bad id"))

        assertEquals(ErrorHandler.ErrorCategory.DATA, error.category)
        assertEquals(ErrorCode.INVALID_DATA, error.errorCode)
        assertEquals(StringDesc.Raw("bad id"), error.message)
    }

    @Test
    fun unknownExceptionFallsBackToDefaultMessage() {
        val error = ErrorHandler.getErrorFromException(RuntimeException("boom"))

        assertEquals(ErrorHandler.ErrorCategory.UNKNOWN, error.category)
        assertEquals(ErrorCode.UNKNOWN, error.errorCode)
        assertEquals(MR.strings.error.desc(), error.title)
        assertEquals(MR.strings.error_generic.desc(), error.message)
    }

    @Test
    fun unrecognizedExceptionNoLongerPatternMatchesOnMessageSubstrings() {
        // SH-12: the old fallback would have guessed NETWORK/"The request
        // timed out..." from these substrings; classification is by exception
        // type (or a DomainException's typed code) only.
        val error = ErrorHandler.getErrorFromException(
            RuntimeException("Connection timeout while talking to server 500")
        )

        assertEquals(ErrorHandler.ErrorCategory.UNKNOWN, error.category)
        assertEquals(ErrorCode.UNKNOWN, error.errorCode)
        assertEquals(MR.strings.error_generic.desc(), error.message)
    }

    @Test
    fun unknownExceptionHonoursCallerSuppliedDefaultMessage() {
        val error = ErrorHandler.getErrorFromException(
            RuntimeException("boom"),
            defaultMessage = MR.strings.error_load_cocktails_failed.desc()
        )

        assertEquals(MR.strings.error_load_cocktails_failed.desc(), error.message)
    }

    @Test
    fun recoveryActionIsCarriedThrough() {
        val action = ErrorHandler.RecoveryAction(MR.strings.common_retry.desc()) {}
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
        val error = ErrorHandler.errorFromCode(ErrorCode.SERVER_ERROR, StringDesc.Raw("The server is on fire."))

        assertEquals(MR.strings.error_title_server.desc(), error.title)
        assertEquals(StringDesc.Raw("The server is on fire."), error.message)
        assertEquals(ErrorHandler.ErrorCategory.SERVER, error.category)
        assertEquals(ErrorCode.SERVER_ERROR, error.errorCode)
    }

    @Test
    fun errorFromCodeFallsBackToRecoverySuggestionForBlankMessage() {
        val error = ErrorHandler.errorFromCode(ErrorCode.NETWORK, ErrorHandler.rawMessage("  "))

        assertEquals(MR.strings.error_suggestion_network.desc(), error.message)
    }
}
