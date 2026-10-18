package com.cocktailcraft.util

import com.cocktailcraft.domain.util.DomainException
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.CancellationException
import kotlin.native.HiddenFromObjC

/**
 * Repository/use-case boundary helper for the typed error pipeline (AR-2).
 *
 * Runs [block] — which builds its own Result.Success/Result.Error for
 * legitimate business-rule early returns (e.g. auth lockout) — and, if it
 * throws, converts the exception into a [Result.Error] carrying the
 * [com.cocktailcraft.domain.util.ErrorCode] assigned by the shared type-based
 * classifier ([ErrorHandler.getErrorFromException]) instead of defaulting to
 * ErrorCode.UNKNOWN. A thrown DomainException (from a nested getOrThrow())
 * keeps its original code. Coroutine cancellation is rethrown, never turned
 * into a user-facing error.
 *
 * Lives in :core:common (was :core:data-internal) so the domain use cases
 * share it instead of hand-rolling catch blocks that swallow cancellation.
 */
@HiddenFromObjC
inline fun <T> runCatchingResult(defaultMessage: String, block: () -> Result<T>): Result<T> {
    return try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: DomainException) {
        Result.Error(message = e.message ?: defaultMessage, code = e.code, displayMessage = e.displayMessage)
    } catch (e: Exception) {
        Result.Error(
            message = e.message ?: defaultMessage,
            code = ErrorHandler.getErrorFromException(e).errorCode
        )
    }
}

/**
 * Runs [block], answering [onFailure] for any exception EXCEPT coroutine
 * cancellation, which is rethrown. For best-effort steps that degrade to a
 * fallback value (a cached copy, an empty list) instead of a Result.Error: a
 * bare `catch (e: Exception)` there would also catch the CancellationException
 * of a cancelled caller and keep running as if nothing happened.
 */
@HiddenFromObjC
inline fun <T> runCatchingNonCancellation(onFailure: (Exception) -> T, block: () -> T): T {
    return try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        onFailure(e)
    }
}
