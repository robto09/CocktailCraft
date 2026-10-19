package com.cocktailcraft.di

import io.ktor.client.engine.darwin.DarwinHttpRequestException
import platform.Foundation.NSURLErrorCannotConnectToHost
import platform.Foundation.NSURLErrorCannotFindHost
import platform.Foundation.NSURLErrorDNSLookupFailed
import platform.Foundation.NSURLErrorDomain
import platform.Foundation.NSURLErrorNotConnectedToInternet

private val unreachableHostCodes = setOf(
    NSURLErrorCannotFindHost,
    NSURLErrorCannotConnectToHost,
    NSURLErrorDNSLookupFailed,
    NSURLErrorNotConnectedToInternet
)

// The Darwin engine wraps URLSession's NSError
internal actual fun isUnreachableHostError(cause: Throwable): Boolean {
    val error = (cause as? DarwinHttpRequestException)?.origin ?: return false
    return error.domain == NSURLErrorDomain && error.code in unreachableHostCodes
}
