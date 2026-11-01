package com.cocktailcraft.domain.util

import kotlin.native.HiddenFromObjC

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()

    /**
     * [message] is raw (untranslated) data-layer text, only ever logged.
     * [retryAfterMs], when set, is how long until the operation may be tried
     * again (e.g. a sign-in lockout); the presentation layer words it. No
     * user-facing copy is built below :presentation.
     */
    data class Error(val message: String, val code: ErrorCode = ErrorCode.UNKNOWN, val retryAfterMs: Long? = null) :
        Result<Nothing>()

    // Helper functions to safely handle results
    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error

    // Safely get data or null
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    // Convert result to another type
    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }
}

// Extension: chain a Result-returning transform on success
@HiddenFromObjC
fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
    is Result.Success -> transform(data)
    is Result.Error -> this
}

// Extension: perform a side-effect on success
@HiddenFromObjC
fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

// Extension: perform a side-effect on error
@HiddenFromObjC
fun <T> Result<T>.onError(action: (String, ErrorCode) -> Unit): Result<T> {
    if (this is Result.Error) action(message, code)
    return this
}

// Extension: get data or a default value
@HiddenFromObjC
fun <T> Result<T>.getOrDefault(defaultValue: @UnsafeVariance T): T = when (this) {
    is Result.Success -> data
    else -> defaultValue
}

/**
 * Exception carrying a domain [ErrorCode] (and the error's [retryAfterMs],
 * if any), thrown when a [Result.Error] is unwrapped via [getOrThrow]. Lets
 * try/catch error paths keep the typed details.
 */
class DomainException(val code: ErrorCode, message: String, val retryAfterMs: Long? = null) : Exception(message)

// Extension: get data or throw an exception (useful for bridging to try/catch callers)
@HiddenFromObjC
fun <T> Result<T>.getOrThrow(): T = when (this) {
    is Result.Success -> data
    is Result.Error -> throw DomainException(code, message, retryAfterMs)
}