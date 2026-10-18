package com.cocktailcraft.util

import com.cocktailcraft.domain.util.DomainException
import com.cocktailcraft.domain.util.ErrorCode
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.error
import com.cocktailcraft.resources.error_generic
import com.cocktailcraft.resources.error_message_authentication
import com.cocktailcraft.resources.error_message_forbidden
import com.cocktailcraft.resources.error_message_invalid_data
import com.cocktailcraft.resources.error_message_invalid_payload
import com.cocktailcraft.resources.error_message_network
import com.cocktailcraft.resources.error_message_not_found
import com.cocktailcraft.resources.error_message_server
import com.cocktailcraft.resources.error_message_timeout
import com.cocktailcraft.resources.error_message_too_many_requests
import com.cocktailcraft.resources.error_suggestion_authentication
import com.cocktailcraft.resources.error_suggestion_client
import com.cocktailcraft.resources.error_suggestion_data
import com.cocktailcraft.resources.error_suggestion_network
import com.cocktailcraft.resources.error_suggestion_server
import com.cocktailcraft.resources.error_suggestion_unknown
import com.cocktailcraft.resources.error_title_authentication
import com.cocktailcraft.resources.error_title_client
import com.cocktailcraft.resources.error_title_data
import com.cocktailcraft.resources.error_title_network
import com.cocktailcraft.resources.error_title_server
import com.cocktailcraft.resources.error_title_timeout
import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.serialization.SerializationException
import kotlin.native.HiddenFromObjC

/**
 * Platform-agnostic error handling utilities for the CocktailCraft app.
 * This class provides common error handling logic that can be used across all platforms.
 *
 * User-facing copy is a [StringDesc] — a shared string resource (MR.strings)
 * that each platform resolves for the current locale at the UI edge
 * (`toString(context)` on Android, `localized()` on iOS), or raw text from the
 * data layer (exception/repository messages, which are not translated).
 */
object ErrorHandler {

    /**
     * Error categories for grouping similar errors
     */
    enum class ErrorCategory {
        NETWORK,
        SERVER,
        CLIENT,
        AUTHENTICATION,
        DATA,
        UNKNOWN
    }

    /**
     * Data class representing a user-friendly error with recovery options
     */
    data class UserFriendlyError(
        val title: StringDesc,
        val message: StringDesc,
        val category: ErrorCategory,
        val recoveryAction: RecoveryAction? = null,
        val originalException: Throwable? = null,
        val errorCode: ErrorCode = ErrorCode.UNKNOWN
    )

    /**
     * Recovery action that can be taken to resolve an error
     */
    data class RecoveryAction(
        val actionLabel: StringDesc,
        val action: () -> Unit
    )

    /**
     * Map a domain [ErrorCode] to its display category.
     */
    fun categoryFor(code: ErrorCode): ErrorCategory = when (code) {
        ErrorCode.NETWORK, ErrorCode.TIMEOUT -> ErrorCategory.NETWORK
        ErrorCode.UNAUTHORIZED, ErrorCode.FORBIDDEN -> ErrorCategory.AUTHENTICATION
        ErrorCode.INVALID_DATA, ErrorCode.NOT_FOUND -> ErrorCategory.DATA
        ErrorCode.SERVER_ERROR -> ErrorCategory.SERVER
        ErrorCode.CLIENT_ERROR -> ErrorCategory.CLIENT
        ErrorCode.UNKNOWN -> ErrorCategory.UNKNOWN
    }

    /**
     * Build a user-friendly error from a typed domain error code. A null
     * [message] falls back to the category's recovery suggestion.
     */
    fun errorFromCode(
        code: ErrorCode,
        message: StringDesc?,
        recoveryAction: RecoveryAction? = null,
        originalException: Throwable? = null
    ): UserFriendlyError {
        val category = categoryFor(code)
        return UserFriendlyError(
            title = getCategoryTitle(category),
            message = message ?: getRecoverySuggestion(category),
            category = category,
            recoveryAction = recoveryAction,
            originalException = originalException,
            errorCode = code
        )
    }

    /**
     * Convert an exception to a user-friendly error message.
     * Classification is by exception type first (Ktor network/HTTP exceptions,
     * serialization failures, DomainException with its typed code); the legacy
     * message string matching is only a last-resort fallback.
     */
    fun getErrorFromException(
        exception: Throwable,
        defaultMessage: StringDesc = MR.strings.error_generic.desc(),
        recoveryAction: RecoveryAction? = null
    ): UserFriendlyError {
        return when {
            // Errors that already carry a typed domain code. Localized copy
            // from the producer wins; transport failures get the shared copy
            // for their code (their raw text is a Ktor message with the
            // request URL, not something to show); otherwise the message is
            // raw data-layer text: missing -> the caller's default, blank ->
            // the category's recovery suggestion.
            exception is DomainException -> errorFromCode(
                code = exception.code,
                message = exception.displayMessage
                    ?: transportMessage(exception.code)
                    ?: exception.message.let { if (it == null) defaultMessage else rawMessage(it) },
                recoveryAction = recoveryAction,
                originalException = exception
            )

            // Timeout errors
            exception is TimeoutCancellationException ||
            exception is HttpRequestTimeoutException ||
            exception is ConnectTimeoutException ||
            exception is SocketTimeoutException -> UserFriendlyError(
                title = MR.strings.error_title_timeout.desc(),
                message = MR.strings.error_message_timeout.desc(),
                category = ErrorCategory.NETWORK,
                recoveryAction = recoveryAction,
                originalException = exception,
                errorCode = ErrorCode.TIMEOUT
            )

            // HTTP 4xx — authorization, missing resources, other client errors
            exception is ClientRequestException -> {
                val status = exception.response.status.value
                val code = when (status) {
                    401 -> ErrorCode.UNAUTHORIZED
                    403 -> ErrorCode.FORBIDDEN
                    404 -> ErrorCode.NOT_FOUND
                    else -> ErrorCode.CLIENT_ERROR
                }
                val message = when (status) {
                    401, 403 -> MR.strings.error_message_forbidden.desc()
                    404 -> MR.strings.error_message_not_found.desc()
                    429 -> MR.strings.error_message_too_many_requests.desc()
                    else -> defaultMessage
                }
                errorFromCode(code, message, recoveryAction, exception)
            }

            // HTTP 5xx
            exception is ServerResponseException -> UserFriendlyError(
                title = MR.strings.error_title_server.desc(),
                message = MR.strings.error_message_server.desc(),
                category = ErrorCategory.SERVER,
                recoveryAction = recoveryAction,
                originalException = exception,
                errorCode = ErrorCode.SERVER_ERROR
            )

            // Connectivity failures below the HTTP layer
            exception is IOException -> UserFriendlyError(
                title = MR.strings.error_title_network.desc(),
                message = MR.strings.error_message_network.desc(),
                category = ErrorCategory.NETWORK,
                recoveryAction = recoveryAction,
                originalException = exception,
                errorCode = ErrorCode.NETWORK
            )

            // Malformed payloads
            exception is SerializationException -> UserFriendlyError(
                title = MR.strings.error_title_data.desc(),
                message = MR.strings.error_message_invalid_payload.desc(),
                category = ErrorCategory.DATA,
                recoveryAction = recoveryAction,
                originalException = exception,
                errorCode = ErrorCode.INVALID_DATA
            )

            // Authentication errors
            // Note: SecurityException is JVM-specific, so we check by exception message instead
            (exception.message?.contains("security", ignoreCase = true) == true ||
            exception.message?.contains("permission", ignoreCase = true) == true) -> UserFriendlyError(
                title = MR.strings.error_title_authentication.desc(),
                message = MR.strings.error_message_authentication.desc(),
                category = ErrorCategory.AUTHENTICATION,
                recoveryAction = recoveryAction,
                originalException = exception,
                errorCode = ErrorCode.UNAUTHORIZED
            )

            // Data errors
            exception is IllegalArgumentException ||
            exception is IllegalStateException -> UserFriendlyError(
                title = MR.strings.error_title_data.desc(),
                message = exception.message?.let { StringDesc.Raw(it) }
                    ?: MR.strings.error_message_invalid_data.desc(),
                category = ErrorCategory.DATA,
                recoveryAction = recoveryAction,
                originalException = exception,
                errorCode = ErrorCode.INVALID_DATA
            )

            // Default for unknown errors — repositories attach a typed
            // ErrorCode at the boundary (AR-2), so anything reaching here has
            // no better classification available: no message-substring
            // guessing (locale/format-fragile, SH-12), just a generic error.
            else -> UserFriendlyError(
                title = getCategoryTitle(ErrorCategory.UNKNOWN),
                message = defaultMessage,
                category = ErrorCategory.UNKNOWN,
                recoveryAction = recoveryAction,
                originalException = exception,
                errorCode = ErrorCode.UNKNOWN
            )
        }
    }

    /**
     * Shared copy for a connectivity, timeout or server failure, or null for
     * codes whose data-layer message is meant for the user.
     */
    private fun transportMessage(code: ErrorCode): StringDesc? = when (code) {
        ErrorCode.NETWORK -> MR.strings.error_message_network
        ErrorCode.TIMEOUT -> MR.strings.error_message_timeout
        ErrorCode.SERVER_ERROR -> MR.strings.error_message_server
        else -> null
    }?.desc()

    /**
     * Get a user-friendly title for an error category
     */
    private fun getCategoryTitle(category: ErrorCategory): StringDesc {
        return when (category) {
            ErrorCategory.NETWORK -> MR.strings.error_title_network
            ErrorCategory.SERVER -> MR.strings.error_title_server
            ErrorCategory.CLIENT -> MR.strings.error_title_client
            ErrorCategory.AUTHENTICATION -> MR.strings.error_title_authentication
            ErrorCategory.DATA -> MR.strings.error_title_data
            ErrorCategory.UNKNOWN -> MR.strings.error
        }.desc()
    }

    /**
     * Get a recovery suggestion based on error category
     */
    fun getRecoverySuggestion(category: ErrorCategory): StringDesc {
        return when (category) {
            ErrorCategory.NETWORK -> MR.strings.error_suggestion_network
            ErrorCategory.SERVER -> MR.strings.error_suggestion_server
            ErrorCategory.CLIENT -> MR.strings.error_suggestion_client
            ErrorCategory.AUTHENTICATION -> MR.strings.error_suggestion_authentication
            ErrorCategory.DATA -> MR.strings.error_suggestion_data
            ErrorCategory.UNKNOWN -> MR.strings.error_suggestion_unknown
        }.desc()
    }

    /**
     * Raw (untranslated) text from the data layer — a Result.Error or
     * exception message — or null when it is blank, so callers fall back to
     * localized copy.
     *
     * Public (was internal) only because the shared ViewModels now live in
     * :presentation; hidden from ObjC so Swift's surface is unchanged (AR-12).
     */
    @HiddenFromObjC
    fun rawMessage(text: String): StringDesc? =
        text.takeIf { it.isNotBlank() }?.let { StringDesc.Raw(it) }
}
