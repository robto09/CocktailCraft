package com.cocktailcraft.util

import com.cocktailcraft.domain.util.DomainException
import com.cocktailcraft.domain.util.ErrorCode
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlin.native.HiddenFromObjC

/**
 * Classifies a failure by exception type: Ktor's network/HTTP exceptions,
 * serialization failures, require()/check() violations. Shared by the
 * repository boundary ([runCatchingResult], which only needs the
 * [ErrorCode]) and the presentation layer's ErrorHandler, which picks the
 * user-facing copy per [Kind].
 *
 * Deliberately free of display copy: this module sits below the domain,
 * and only :presentation decides what the user reads. Classification is by
 * type only — no message-substring guessing beyond the security check.
 */
@HiddenFromObjC
object ErrorClassifier {

    /** What went wrong, finer than [ErrorCode] where the copy differs. */
    enum class Kind(val code: ErrorCode) {
        TIMEOUT(ErrorCode.TIMEOUT),

        /** HTTP 401 */
        UNAUTHORIZED(ErrorCode.UNAUTHORIZED),

        /** HTTP 403 */
        FORBIDDEN(ErrorCode.FORBIDDEN),

        /** HTTP 404 */
        NOT_FOUND(ErrorCode.NOT_FOUND),

        /** HTTP 429 */
        TOO_MANY_REQUESTS(ErrorCode.CLIENT_ERROR),

        /** Any other HTTP 4xx */
        CLIENT_ERROR(ErrorCode.CLIENT_ERROR),

        /** HTTP 5xx */
        SERVER_ERROR(ErrorCode.SERVER_ERROR),

        /** Connectivity failure below the HTTP layer */
        NETWORK(ErrorCode.NETWORK),

        /** Malformed payload */
        INVALID_PAYLOAD(ErrorCode.INVALID_DATA),

        /** A security/permission failure (matched by message: no common type) */
        SECURITY(ErrorCode.UNAUTHORIZED),

        /** A require()/check() violation */
        INVALID_DATA(ErrorCode.INVALID_DATA),

        UNKNOWN(ErrorCode.UNKNOWN)
    }

    fun classify(exception: Throwable): Kind = when {
        isTimeout(exception) -> Kind.TIMEOUT
        exception is ClientRequestException -> httpClientKind(exception.response.status.value)
        exception is ServerResponseException -> Kind.SERVER_ERROR
        exception is IOException -> Kind.NETWORK
        exception is SerializationException -> Kind.INVALID_PAYLOAD
        isSecurityFailure(exception) -> Kind.SECURITY
        exception is IllegalArgumentException || exception is IllegalStateException -> Kind.INVALID_DATA
        else -> Kind.UNKNOWN
    }

    private fun isTimeout(exception: Throwable): Boolean =
        exception is TimeoutCancellationException ||
            exception is HttpRequestTimeoutException ||
            exception is ConnectTimeoutException ||
            exception is SocketTimeoutException

    private fun httpClientKind(status: Int): Kind = when (status) {
        HTTP_UNAUTHORIZED -> Kind.UNAUTHORIZED
        HTTP_FORBIDDEN -> Kind.FORBIDDEN
        HTTP_NOT_FOUND -> Kind.NOT_FOUND
        HTTP_TOO_MANY_REQUESTS -> Kind.TOO_MANY_REQUESTS
        else -> Kind.CLIENT_ERROR
    }

    // SecurityException is JVM-specific, so it is matched by message
    private fun isSecurityFailure(exception: Throwable): Boolean =
        exception.message?.contains("security", ignoreCase = true) == true ||
            exception.message?.contains("permission", ignoreCase = true) == true

    /** The typed code for [exception]: a [DomainException] keeps its own. */
    fun codeFor(exception: Throwable): ErrorCode =
        if (exception is DomainException) exception.code else classify(exception).code

    private const val HTTP_UNAUTHORIZED = 401
    private const val HTTP_FORBIDDEN = 403
    private const val HTTP_NOT_FOUND = 404
    private const val HTTP_TOO_MANY_REQUESTS = 429
}
