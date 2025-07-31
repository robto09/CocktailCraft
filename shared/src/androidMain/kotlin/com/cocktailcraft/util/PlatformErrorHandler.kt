package com.cocktailcraft.util

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

/**
 * Android-specific error handling extensions
 */
object PlatformErrorHandler {
    
    /**
     * Convert Android-specific network exceptions to user-friendly errors
     */
    fun getErrorFromPlatformException(
        exception: Throwable,
        defaultMessage: String = "Something went wrong. Please try again.",
        recoveryAction: ErrorHandler.RecoveryAction? = null
    ): ErrorHandler.UserFriendlyError {
        return when (exception) {
            // Network errors
            is UnknownHostException,
            is ConnectException -> ErrorHandler.UserFriendlyError(
                title = "Network Error",
                message = "Unable to connect to the server. Please check your internet connection and try again.",
                category = ErrorHandler.ErrorCategory.NETWORK,
                recoveryAction = recoveryAction,
                originalException = exception,
                errorCode = com.cocktailcraft.domain.util.ErrorCode.NETWORK
            )

            // Timeout errors
            is SocketTimeoutException,
            is TimeoutException -> ErrorHandler.UserFriendlyError(
                title = "Connection Timeout",
                message = "The connection timed out. This might be due to slow internet or server issues. Please try again.",
                category = ErrorHandler.ErrorCategory.NETWORK,
                recoveryAction = recoveryAction,
                originalException = exception,
                errorCode = com.cocktailcraft.domain.util.ErrorCode.TIMEOUT
            )

            // Fallback to common error handler
            else -> ErrorHandler.getErrorFromException(exception, defaultMessage, recoveryAction)
        }
    }
}