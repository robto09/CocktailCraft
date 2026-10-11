package com.cocktailcraft.domain.util

/**
 * Standardized error codes for the application.
 * These codes can be used to categorize errors and provide consistent error handling.
 */
enum class ErrorCode {
    // Network related errors
    NETWORK,
    TIMEOUT,

    // Authentication errors
    UNAUTHORIZED,
    FORBIDDEN,

    // Data errors
    INVALID_DATA,
    NOT_FOUND,

    // Server errors
    SERVER_ERROR,

    // Client errors
    CLIENT_ERROR,

    // Unknown errors
    UNKNOWN
}
