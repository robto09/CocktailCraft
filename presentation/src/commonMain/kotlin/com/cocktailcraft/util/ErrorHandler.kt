package com.cocktailcraft.util

import com.cocktailcraft.domain.util.DomainException
import com.cocktailcraft.domain.util.ErrorCode
import com.cocktailcraft.domain.util.Result
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
import com.cocktailcraft.resources.error_sign_in_locked_out_minutes
import com.cocktailcraft.resources.error_sign_in_locked_out_seconds
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
import dev.icerock.moko.resources.desc.PluralFormatted
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

/**
 * Turns failures into user-facing errors: the one place display copy for a
 * failure is chosen. Lives in :presentation so the layers below never build
 * copy — they report a typed [ErrorCode] (plus facts such as
 * [Result.Error.retryAfterMs]), and exceptions are classified by
 * [ErrorClassifier] in :core:common.
 *
 * User-facing copy is a [StringDesc] — a shared string resource (MR.strings)
 * that each platform resolves for the current locale at the UI edge
 * (`toString(context)` on Android, `localized()` on iOS). Raw data-layer text
 * (exception and Result.Error messages: untranslated, and for transport
 * failures carrying the request URL) never becomes user-facing copy; it is
 * only ever logged, at Debug.
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
    data class RecoveryAction(val actionLabel: StringDesc, val action: () -> Unit)

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
     * The one mapping from a failed [Result] to user-facing copy, shared by
     * the getOrReport path and (through the [DomainException] a getOrThrow()
     * raises) [getErrorFromException]: a sign-in lockout (FORBIDDEN with
     * [retryAfterMs]) gets its countdown copy; transport failures get the
     * shared copy for their code; anything else gets [defaultMessage], the
     * caller's localized "couldn't do X" copy. The raw [Result.Error.message]
     * is never shown.
     */
    fun errorFromResult(
        code: ErrorCode,
        retryAfterMs: Long?,
        defaultMessage: StringDesc,
        recoveryAction: RecoveryAction? = null,
        originalException: Throwable? = null
    ): UserFriendlyError = errorFromCode(
        code = code,
        message = lockoutMessageFor(code, retryAfterMs) ?: transportMessage(code) ?: defaultMessage,
        recoveryAction = recoveryAction,
        originalException = originalException
    )

    /** [errorFromResult] for a [Result.Error]. */
    fun errorFromResult(
        error: Result.Error,
        defaultMessage: StringDesc,
        recoveryAction: RecoveryAction? = null
    ): UserFriendlyError = errorFromResult(error.code, error.retryAfterMs, defaultMessage, recoveryAction)

    /**
     * Sign-in lockout copy; the remaining wait is rounded up to whole
     * seconds, or whole minutes from one minute on.
     */
    fun lockoutMessage(remainingMs: Long): StringDesc {
        val seconds = ((remainingMs + MS_PER_SECOND - 1) / MS_PER_SECOND).toInt()
        return if (seconds < SECONDS_PER_MINUTE) {
            StringDesc.PluralFormatted(MR.plurals.error_sign_in_locked_out_seconds, seconds, seconds)
        } else {
            val minutes = (seconds + SECONDS_PER_MINUTE - 1) / SECONDS_PER_MINUTE
            StringDesc.PluralFormatted(MR.plurals.error_sign_in_locked_out_minutes, minutes, minutes)
        }
    }

    private fun lockoutMessageFor(code: ErrorCode, retryAfterMs: Long?): StringDesc? =
        if (code == ErrorCode.FORBIDDEN && retryAfterMs != null) lockoutMessage(retryAfterMs) else null

    /**
     * Convert an exception to a user-friendly error message. A
     * DomainException maps by its typed code; anything else by the
     * [ErrorClassifier] kind of its type.
     */
    fun getErrorFromException(
        exception: Throwable,
        defaultMessage: StringDesc = MR.strings.error_generic.desc(),
        recoveryAction: RecoveryAction? = null
    ): UserFriendlyError {
        // Errors that already carry a typed domain code: the same mapping
        // as a reported Result.Error
        if (exception is DomainException) {
            return errorFromResult(
                code = exception.code,
                retryAfterMs = exception.retryAfterMs,
                defaultMessage = defaultMessage,
                recoveryAction = recoveryAction,
                originalException = exception
            )
        }

        val kind = ErrorClassifier.classify(exception)
        val (title, message) = when (kind) {
            ErrorClassifier.Kind.TIMEOUT -> MR.strings.error_title_timeout.desc() to
                MR.strings.error_message_timeout.desc()

            ErrorClassifier.Kind.UNAUTHORIZED, ErrorClassifier.Kind.FORBIDDEN ->
                null to MR.strings.error_message_forbidden.desc()

            ErrorClassifier.Kind.NOT_FOUND -> null to MR.strings.error_message_not_found.desc()

            ErrorClassifier.Kind.TOO_MANY_REQUESTS -> null to MR.strings.error_message_too_many_requests.desc()

            ErrorClassifier.Kind.CLIENT_ERROR -> null to defaultMessage

            ErrorClassifier.Kind.SERVER_ERROR -> MR.strings.error_title_server.desc() to
                MR.strings.error_message_server.desc()

            ErrorClassifier.Kind.NETWORK -> MR.strings.error_title_network.desc() to
                MR.strings.error_message_network.desc()

            ErrorClassifier.Kind.INVALID_PAYLOAD -> MR.strings.error_title_data.desc() to
                MR.strings.error_message_invalid_payload.desc()

            ErrorClassifier.Kind.SECURITY -> MR.strings.error_title_authentication.desc() to
                MR.strings.error_message_authentication.desc()

            // Their message is a developer's require()/check() text, never
            // user-facing copy
            ErrorClassifier.Kind.INVALID_DATA -> MR.strings.error_title_data.desc() to
                MR.strings.error_message_invalid_data.desc()

            // Repositories attach a typed ErrorCode at the boundary, so
            // anything reaching here has no better classification available
            ErrorClassifier.Kind.UNKNOWN -> null to defaultMessage
        }
        val category = categoryFor(kind.code)
        return UserFriendlyError(
            title = title ?: getCategoryTitle(category),
            message = message,
            category = category,
            recoveryAction = recoveryAction,
            originalException = exception,
            errorCode = kind.code
        )
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
    private fun getCategoryTitle(category: ErrorCategory): StringDesc = when (category) {
        ErrorCategory.NETWORK -> MR.strings.error_title_network
        ErrorCategory.SERVER -> MR.strings.error_title_server
        ErrorCategory.CLIENT -> MR.strings.error_title_client
        ErrorCategory.AUTHENTICATION -> MR.strings.error_title_authentication
        ErrorCategory.DATA -> MR.strings.error_title_data
        ErrorCategory.UNKNOWN -> MR.strings.error
    }.desc()

    /**
     * Get a recovery suggestion based on error category
     */
    fun getRecoverySuggestion(category: ErrorCategory): StringDesc = when (category) {
        ErrorCategory.NETWORK -> MR.strings.error_suggestion_network
        ErrorCategory.SERVER -> MR.strings.error_suggestion_server
        ErrorCategory.CLIENT -> MR.strings.error_suggestion_client
        ErrorCategory.AUTHENTICATION -> MR.strings.error_suggestion_authentication
        ErrorCategory.DATA -> MR.strings.error_suggestion_data
        ErrorCategory.UNKNOWN -> MR.strings.error_suggestion_unknown
    }.desc()

    private const val MS_PER_SECOND = 1000L
    private const val SECONDS_PER_MINUTE = 60
}
