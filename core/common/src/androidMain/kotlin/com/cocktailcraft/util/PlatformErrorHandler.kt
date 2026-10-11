package com.cocktailcraft.util

import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.error_generic
import com.cocktailcraft.resources.error_message_network_retry
import com.cocktailcraft.resources.error_message_timeout
import com.cocktailcraft.resources.error_title_network
import com.cocktailcraft.resources.error_title_timeout
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
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
        defaultMessage: StringDesc = MR.strings.error_generic.desc(),
        recoveryAction: ErrorHandler.RecoveryAction? = null
    ): ErrorHandler.UserFriendlyError {
        return when (exception) {
            // Network errors
            is UnknownHostException,
            is ConnectException -> ErrorHandler.UserFriendlyError(
                title = MR.strings.error_title_network.desc(),
                message = MR.strings.error_message_network_retry.desc(),
                category = ErrorHandler.ErrorCategory.NETWORK,
                recoveryAction = recoveryAction,
                originalException = exception,
                errorCode = com.cocktailcraft.domain.util.ErrorCode.NETWORK
            )

            // Timeout errors
            is SocketTimeoutException,
            is TimeoutException -> ErrorHandler.UserFriendlyError(
                title = MR.strings.error_title_timeout.desc(),
                message = MR.strings.error_message_timeout.desc(),
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
