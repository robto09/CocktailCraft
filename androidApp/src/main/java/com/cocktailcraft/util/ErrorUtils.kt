package com.cocktailcraft.util

import com.cocktailcraft.domain.util.ErrorCode
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

/**
 * Utility class for handling errors consistently throughout the app.
 */
object ErrorUtils {

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
     * Convert an exception to a user-friendly error message
     */
    fun getErrorFromException(
        exception: Throwable,
        defaultMessage: String = "Something went wrong. Please try again.",
        recoveryAction: RecoveryAction? = null
    ): UserFriendlyError {
        return when (exception) {
            // Network errors
            is UnknownHostException,
            is ConnectException -> UserFriendlyError(
                title = "Network Error",
                message = "Unable to connect to the server. Please check your internet connection and try again.",
                category = ErrorCategory.NETWORK,
                recoveryAction = recoveryAction,
                originalException = exception,
                errorCode = ErrorCode.NETWORK
            )

            // Timeout errors
            is SocketTimeoutException,
            is TimeoutException -> UserFriendlyError(
                title = "Connection Timeout",
                message = "The connection timed out. This might be due to slow internet or server issues. Please try again.",
                category = ErrorCategory.NETWORK,
                recoveryAction = recoveryAction,
                originalException = exception,
                errorCode = ErrorCode.TIMEOUT
            )

            // Authentication errors
            is SecurityException -> UserFriendlyError(
                title = "Authentication Error",
                message = "You don't have permission to access this resource. Please log in again.",
                category = ErrorCategory.AUTHENTICATION,
                recoveryAction = recoveryAction,
                originalException = exception,
                errorCode = ErrorCode.UNAUTHORIZED
            )

            // Data errors
            is IllegalArgumentException,
            is IllegalStateException -> UserFriendlyError(
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
        // Check for network-related exceptions
        if (exception is java.net.UnknownHostException ||
            exception is java.net.ConnectException ||
            exception.message?.contains("connect", ignoreCase = true) == true ||
            exception.message?.contains("network", ignoreCase = true) == true) {
            return createNetworkError(retryAction)
        }

        // Check for timeout exceptions
        if (exception is java.net.SocketTimeoutException ||
            exception is java.util.concurrent.TimeoutException ||
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
