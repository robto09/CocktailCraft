package com.cocktailcraft.android.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.cocktailcraft.domain.util.ErrorCode
import com.cocktailcraft.android.ui.theme.AppColors
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.util.PlatformErrorHandler

// Re-export shared error types for backward compatibility
typealias ErrorCategory = ErrorHandler.ErrorCategory
typealias UserFriendlyError = ErrorHandler.UserFriendlyError
typealias RecoveryAction = ErrorHandler.RecoveryAction

/**
 * Utility class for handling errors consistently throughout the app.
 */
object ErrorUtils {

    /**
     * Convert an exception to a user-friendly error message
     * Delegates to PlatformErrorHandler for Android-specific exceptions
     */
    fun getErrorFromException(
        exception: Throwable,
        defaultMessage: String = "Something went wrong. Please try again.",
        recoveryAction: RecoveryAction? = null
    ): UserFriendlyError {
        return PlatformErrorHandler.getErrorFromPlatformException(
            exception = exception,
            defaultMessage = defaultMessage,
            recoveryAction = recoveryAction
        )
    }


    // Delegate to shared ErrorHandler for all other methods
    fun getRecoverySuggestion(category: ErrorCategory) = ErrorHandler.getRecoverySuggestion(category)
    
    fun getDefaultRecoveryAction(category: ErrorCategory, action: () -> Unit) = 
        ErrorHandler.getDefaultRecoveryAction(category, action)
    
    fun createUserFriendlyError(
        title: String,
        message: String,
        category: ErrorCategory = ErrorCategory.UNKNOWN,
        recoveryAction: RecoveryAction? = null,
        originalException: Throwable? = null,
        errorCode: ErrorCode = ErrorCode.UNKNOWN
    ) = ErrorHandler.createUserFriendlyError(
        title, message, category, recoveryAction, originalException, errorCode
    )
    
    fun createNetworkError(retryAction: (() -> Unit)? = null) = 
        ErrorHandler.createNetworkError(retryAction)
    
    fun createServerError(retryAction: (() -> Unit)? = null) = 
        ErrorHandler.createServerError(retryAction)
    
    fun createErrorFromException(
        exception: Throwable,
        defaultTitle: String = "Error",
        defaultMessage: String = "An unexpected error occurred.",
        retryAction: (() -> Unit)? = null
    ) = PlatformErrorHandler.getErrorFromPlatformException(
        exception, "$defaultTitle. $defaultMessage", retryAction?.let { RecoveryAction("Retry", it) }
    )
    
    fun createErrorFromErrorCode(
        errorCode: ErrorCode,
        defaultTitle: String = "Error",
        defaultMessage: String = "An error occurred",
        retryAction: (() -> Unit)? = null
    ) = ErrorHandler.createErrorFromErrorCode(
        errorCode, defaultTitle, defaultMessage, retryAction
    )

    /**
     * Get an appropriate icon for the error category
     */
    @Composable
    fun getErrorIcon(category: ErrorCategory): ImageVector {
        return when (category) {
            ErrorCategory.NETWORK -> Icons.Default.WifiOff
            ErrorCategory.SERVER,
            ErrorCategory.CLIENT -> Icons.Default.Warning
            else -> Icons.Default.Error
        }
    }

    /**
     * Get an appropriate color for the error category
     */
    @Composable
    fun getErrorColor(category: ErrorCategory): Color {
        return when (category) {
            ErrorCategory.NETWORK -> AppColors.Primary
            ErrorCategory.SERVER -> Color(0xFFF57C00) // Orange
            ErrorCategory.AUTHENTICATION -> Color(0xFFD32F2F) // Red
            else -> AppColors.Error
        }
    }
}
