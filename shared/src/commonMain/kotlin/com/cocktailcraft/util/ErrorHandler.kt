package com.cocktailcraft.util

import com.cocktailcraft.domain.util.DomainException
import com.cocktailcraft.domain.util.ErrorCode
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.serialization.SerializationException

/**
 * Platform-agnostic error handling utilities for the CocktailCraft app.
 * This class provides common error handling logic that can be used across all platforms.
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
        val title: String,
        val message: String,
        val category: ErrorCategory,
        val recoveryAction: RecoveryAction? = null,
        val originalException: Throwable? = null,
        val errorCode: ErrorCode = ErrorCode.UNKNOWN
    )

    /**
     * Recovery action that can be taken to resolve an error
     */
    data class RecoveryAction(
        val actionLabel: String,
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
     * Build a user-friendly error from a typed domain error code.
     */
    fun errorFromCode(
        code: ErrorCode,
        message: String,
        recoveryAction: RecoveryAction? = null,
        originalException: Throwable? = null
    ): UserFriendlyError {
        val category = categoryFor(code)
        return UserFriendlyError(
            title = getCategoryTitle(category),
            message = message.ifBlank { getRecoverySuggestion(category) },
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
        defaultMessage: String = "Something went wrong. Please try again.",
        recoveryAction: RecoveryAction? = null
    ): UserFriendlyError {
        return when {
            // Errors that already carry a typed domain code
            exception is DomainException -> errorFromCode(
                code = exception.code,
                message = exception.message ?: defaultMessage,
                recoveryAction = recoveryAction,
                originalException = exception
            )

            // Timeout errors
            exception is TimeoutCancellationException ||
            exception is HttpRequestTimeoutException ||
            exception is ConnectTimeoutException ||
            exception is SocketTimeoutException -> UserFriendlyError(
                title = "Connection Timeout",
                message = "The connection timed out. This might be due to slow internet or server issues. Please try again.",
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
                    401, 403 -> "You don't have permission to access this resource."
                    404 -> "The requested resource was not found."
                    429 -> "Too many requests. Please wait a moment and try again."
                    else -> defaultMessage
                }
                errorFromCode(code, message, recoveryAction, exception)
            }

            // HTTP 5xx
            exception is ServerResponseException -> UserFriendlyError(
                title = "Server Error",
                message = "The server encountered an error. Please try again later.",
                category = ErrorCategory.SERVER,
                recoveryAction = recoveryAction,
                originalException = exception,
                errorCode = ErrorCode.SERVER_ERROR
            )

            // Connectivity failures below the HTTP layer
            exception is IOException -> UserFriendlyError(
                title = "Network Error",
                message = "Unable to connect to the server. Please check your internet connection.",
                category = ErrorCategory.NETWORK,
                recoveryAction = recoveryAction,
                originalException = exception,
                errorCode = ErrorCode.NETWORK
            )

            // Malformed payloads
            exception is SerializationException -> UserFriendlyError(
                title = "Data Error",
                message = "Received unexpected data from the server. Please try again.",
                category = ErrorCategory.DATA,
                recoveryAction = recoveryAction,
                originalException = exception,
                errorCode = ErrorCode.INVALID_DATA
            )

            // Authentication errors
            // Note: SecurityException is JVM-specific, so we check by exception message instead
            (exception.message?.contains("security", ignoreCase = true) == true ||
            exception.message?.contains("permission", ignoreCase = true) == true) -> UserFriendlyError(
                title = "Authentication Error",
                message = "You don't have permission to access this resource. Please log in again.",
                category = ErrorCategory.AUTHENTICATION,
                recoveryAction = recoveryAction,
                originalException = exception,
                errorCode = ErrorCode.UNAUTHORIZED
            )

            // Data errors
            exception is IllegalArgumentException ||
            exception is IllegalStateException -> UserFriendlyError(
                title = "Data Error",
                message = exception.message ?: "Invalid data received. Please try again.",
                category = ErrorCategory.DATA,
                recoveryAction = recoveryAction,
                originalException = exception,
                errorCode = ErrorCode.INVALID_DATA
            )

            // Default for unknown errors
            else -> {
                val message = when {
                    exception.message?.contains("timeout", ignoreCase = true) == true ->
                        "The request timed out. Please try again."
                    exception.message?.contains("connect", ignoreCase = true) == true ->
                        "Unable to connect to the server. Please check your internet connection."
                    exception.message?.contains("server", ignoreCase = true) == true ->
                        "The server encountered an error. Please try again later."
                    exception.message?.contains("404", ignoreCase = true) == true ->
                        "The requested resource was not found."
                    exception.message?.contains("401", ignoreCase = true) == true ||
                    exception.message?.contains("403", ignoreCase = true) == true ->
                        "You don't have permission to access this resource."
                    exception.message?.contains("500", ignoreCase = true) == true ->
                        "The server encountered an internal error. Please try again later."
                    else -> defaultMessage
                }

                val category = when {
                    message.contains("internet", ignoreCase = true) ||
                    message.contains("connect", ignoreCase = true) -> ErrorCategory.NETWORK
                    message.contains("server", ignoreCase = true) -> ErrorCategory.SERVER
                    message.contains("permission", ignoreCase = true) -> ErrorCategory.AUTHENTICATION
                    else -> ErrorCategory.UNKNOWN
                }

                UserFriendlyError(
                    title = getCategoryTitle(category),
                    message = message,
                    category = category,
                    recoveryAction = recoveryAction,
                    originalException = exception
                )
            }
        }
    }

    /**
     * Get a user-friendly title for an error category
     */
    private fun getCategoryTitle(category: ErrorCategory): String {
        return when (category) {
            ErrorCategory.NETWORK -> "Network Error"
            ErrorCategory.SERVER -> "Server Error"
            ErrorCategory.CLIENT -> "Application Error"
            ErrorCategory.AUTHENTICATION -> "Authentication Error"
            ErrorCategory.DATA -> "Data Error"
            ErrorCategory.UNKNOWN -> "Error"
        }
    }

    /**
     * Get a recovery suggestion based on error category
     */
    fun getRecoverySuggestion(category: ErrorCategory): String {
        return when (category) {
            ErrorCategory.NETWORK -> "Check your internet connection and try again."
            ErrorCategory.SERVER -> "Our servers are experiencing issues. Please try again later."
            ErrorCategory.CLIENT -> "Try restarting the app."
            ErrorCategory.AUTHENTICATION -> "Please log in again."
            ErrorCategory.DATA -> "Try refreshing the data."
            ErrorCategory.UNKNOWN -> "Please try again or contact support if the issue persists."
        }
    }

    /**
     * Get a default recovery action based on error category
     */
    fun getDefaultRecoveryAction(category: ErrorCategory, action: () -> Unit): RecoveryAction {
        val label = when (category) {
            ErrorCategory.NETWORK -> "Retry"
            ErrorCategory.SERVER -> "Try Again"
            ErrorCategory.CLIENT -> "Refresh"
            ErrorCategory.AUTHENTICATION -> "Log In"
            ErrorCategory.DATA -> "Refresh"
            ErrorCategory.UNKNOWN -> "Try Again"
        }

        return RecoveryAction(label, action)
    }

    /**
     * Create a user-friendly error with the given parameters
     */
    fun createUserFriendlyError(
        title: String,
        message: String,
        category: ErrorCategory = ErrorCategory.UNKNOWN,
        recoveryAction: RecoveryAction? = null,
        originalException: Throwable? = null,
        errorCode: ErrorCode = ErrorCode.UNKNOWN
    ): UserFriendlyError {
        return UserFriendlyError(
            title = title,
            message = message,
            category = category,
            recoveryAction = recoveryAction,
            originalException = originalException,
            errorCode = errorCode
        )
    }

    /**
     * Create a standard network error
     */
    fun createNetworkError(retryAction: (() -> Unit)? = null): UserFriendlyError {
        return UserFriendlyError(
            title = "Network Error",
            message = "Unable to connect to the server. Please check your internet connection and try again.",
            category = ErrorCategory.NETWORK,
            recoveryAction = retryAction?.let { RecoveryAction("Retry", it) },
            errorCode = ErrorCode.NETWORK
        )
    }

    /**
     * Create a standard server error
     */
    fun createServerError(retryAction: (() -> Unit)? = null): UserFriendlyError {
        return UserFriendlyError(
            title = "Server Error",
            message = "The server encountered an error. Please try again later.",
            category = ErrorCategory.SERVER,
            recoveryAction = retryAction?.let { RecoveryAction("Retry", it) },
            errorCode = ErrorCode.SERVER_ERROR
        )
    }

    /**
     * Create an error from an exception
     */
    fun createErrorFromException(
        exception: Throwable,
        defaultTitle: String = "Error",
        defaultMessage: String = "An unexpected error occurred.",
        retryAction: (() -> Unit)? = null
    ): UserFriendlyError {
        // Platform-specific network exception handling will be done in actual implementations
        
        // Check for timeout exceptions
        if (exception is TimeoutCancellationException ||
            exception.message?.contains("timeout", ignoreCase = true) == true) {
            return UserFriendlyError(
                title = "Network Error",
                message = "The request timed out. Please try again.",
                category = ErrorCategory.NETWORK,
                recoveryAction = retryAction?.let { RecoveryAction("Retry", it) },
                originalException = exception,
                errorCode = ErrorCode.TIMEOUT
            )
        }

        // Default error with the exception message
        return UserFriendlyError(
            title = defaultTitle,
            message = "$defaultMessage: ${exception.message}",
            category = ErrorCategory.UNKNOWN,
            recoveryAction = retryAction?.let { RecoveryAction("Retry", it) },
            originalException = exception,
            errorCode = ErrorCode.UNKNOWN
        )
    }

    /**
     * Create an error from an error code
     */
    fun createErrorFromErrorCode(
        errorCode: ErrorCode,
        defaultTitle: String = "Error",
        defaultMessage: String = "An error occurred",
        retryAction: (() -> Unit)? = null
    ): UserFriendlyError {
        return when (errorCode) {
            ErrorCode.NETWORK -> createNetworkError(retryAction)
            ErrorCode.TIMEOUT -> UserFriendlyError(
                title = "Network Error",
                message = "The request timed out. Please try again.",
                category = ErrorCategory.NETWORK,
                recoveryAction = retryAction?.let { RecoveryAction("Retry", it) },
                errorCode = errorCode
            )
            ErrorCode.UNAUTHORIZED -> UserFriendlyError(
                title = "Authentication Error",
                message = "You are not authorized to perform this action. Please sign in and try again.",
                category = ErrorCategory.AUTHENTICATION,
                recoveryAction = retryAction?.let { RecoveryAction("Sign In", it) },
                errorCode = errorCode
            )
            ErrorCode.INVALID_DATA -> UserFriendlyError(
                title = "Data Error",
                message = "The data provided is invalid. Please check your input and try again.",
                category = ErrorCategory.DATA,
                recoveryAction = retryAction?.let { RecoveryAction("Try Again", it) },
                errorCode = errorCode
            )
            else -> UserFriendlyError(
                title = defaultTitle,
                message = defaultMessage,
                category = ErrorCategory.UNKNOWN,
                recoveryAction = retryAction?.let { RecoveryAction("Try Again", it) },
                errorCode = errorCode
            )
        }
    }
}