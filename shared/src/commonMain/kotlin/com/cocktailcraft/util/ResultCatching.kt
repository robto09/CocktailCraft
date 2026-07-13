package com.cocktailcraft.util

import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.CancellationException

/**
 * Repository-boundary helper for the typed error pipeline (AR-2).
 *
 * Runs [block] — which builds its own Result.Success/Result.Error for
 * legitimate business-rule early returns (e.g. auth lockout) — and, if it
 * throws, converts the exception into a [Result.Error] carrying the
 * [com.cocktailcraft.domain.util.ErrorCode] assigned by the shared type-based
 * classifier ([ErrorHandler.getErrorFromException]) instead of defaulting to
 * ErrorCode.UNKNOWN. A thrown DomainException (from a nested getOrThrow())
 * keeps its original code. Coroutine cancellation is rethrown, never turned
 * into a user-facing error.
 */
internal inline fun <T> runCatchingResult(defaultMessage: String, block: () -> Result<T>): Result<T> {
    return try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.Error(
            message = e.message ?: defaultMessage,
            code = ErrorHandler.getErrorFromException(e, defaultMessage).errorCode
        )
    }
}
