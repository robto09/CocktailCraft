package com.cocktailcraft.domain.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Standardized error handling utility for the shared module.
 * This class provides consistent error handling across platforms.
 */
class ErrorHandler {
    companion object {
        /**
         * Convert an exception to a Result.Error with appropriate error code
         * @param exception The exception to convert
         * @param defaultMessage The default error message to use if the exception doesn't have one
         * @return A Result.Error with the appropriate error message and code
         */
        fun handleException(exception: Throwable, defaultMessage: String = "An unexpected error occurred"): Result.Error {
            val message = exception.message ?: defaultMessage
            val errorCode = getErrorCodeFromException(exception)

            return Result.Error(message, errorCode)
        }

        /**
         * Handle an exception and convert it to a Flow of Result.Error.
         * @param exception The exception to handle
         * @param defaultMessage The default error message to use if the exception doesn't provide one
         * @return A Flow emitting a Result.Error with the appropriate message and error code
         */
        fun <T> handleExceptionAsFlow(exception: Throwable, defaultMessage: String = "An unexpected error occurred"): Flow<Result<T>> = flow {
            emit(handleException(exception, defaultMessage))
        }

        /**
         * Get an appropriate error code for an exception
         * @param exception The exception to get an error code for
         * @return The appropriate error code
         */
        fun getErrorCodeFromException(exception: Throwable): ErrorCode {
            return when {
                // Network errors
                exception.toString().contains("UnknownHostException") ||
                exception.toString().contains("ConnectException") ||
                exception.toString().contains("SocketException") -> ErrorCode.NETWORK

                // Timeout errors
                exception.toString().contains("SocketTimeoutException") ||
                exception.toString().contains("TimeoutException") -> ErrorCode.TIMEOUT

                // Authentication errors
                exception.toString().contains("SecurityException") ||
                exception.toString().contains("401") ||
                exception.toString().contains("Unauthorized") -> ErrorCode.UNAUTHORIZED

                // Forbidden errors
                exception.toString().contains("403") ||
                exception.toString().contains("Forbidden") -> ErrorCode.FORBIDDEN

                // Not found errors
                exception.toString().contains("404") ||
                exception.toString().contains("Not Found") -> ErrorCode.NOT_FOUND

                // Server errors
                exception.toString().contains("500") ||
                exception.toString().contains("502") ||
                exception.toString().contains("503") ||
                exception.toString().contains("504") -> ErrorCode.SERVER_ERROR

                // Data errors
                exception is IllegalArgumentException ||
                exception is IllegalStateException -> ErrorCode.INVALID_DATA

                // Default
                else -> ErrorCode.UNKNOWN
            }
        }

        /**
         * Create a network error
         * @param message The error message
         * @return A Result.Error with a network error code
         */
        fun createNetworkError(message: String = "Network error. Please check your connection."): Result.Error {
            return Result.Error(message, ErrorCode.NETWORK)
        }

        /**
         * Create a server error
         * @param message The error message
         * @return A Result.Error with a server error code
         */
        fun createServerError(message: String = "Server error. Please try again later."): Result.Error {
            return Result.Error(message, ErrorCode.SERVER_ERROR)
        }

        /**
         * Create a data error
         * @param message The error message
         * @return A Result.Error with a data error code
         */
        fun createDataError(message: String = "Invalid data. Please try again."): Result.Error {
            return Result.Error(message, ErrorCode.INVALID_DATA)
        }

        /**
         * Create an authentication error
         * @param message The error message
         * @return A Result.Error with an authentication error code
         */
        fun createAuthError(message: String = "Authentication error. Please log in again."): Result.Error {
            return Result.Error(message, ErrorCode.UNAUTHORIZED)
        }

        /**
         * Create a not found error
         * @param message The error message
         * @return A Result.Error with a not found error code
         */
        fun createNotFoundError(message: String = "Resource not found."): Result.Error {
            return Result.Error(message, ErrorCode.NOT_FOUND)
        }
    }
}