package com.cocktailcraft.domain.util

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val code: ErrorCode = ErrorCode.UNKNOWN) : Result<Nothing>()
    object Loading : Result<Nothing>()

    // Helper functions to safely handle results
    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
    fun isLoading(): Boolean = this is Loading

    // Safely get data or null
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    // Convert result to another type
    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> this
    }
}

// Extension: chain a Result-returning transform on success
fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
    is Result.Success -> transform(data)
    is Result.Error -> Result.Error(message, code)
    is Result.Loading -> Result.Loading
}

// Extension: perform a side-effect on success
fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

// Extension: perform a side-effect on error
fun <T> Result<T>.onError(action: (String, ErrorCode) -> Unit): Result<T> {
    if (this is Result.Error) action(message, code)
    return this
}

// Extension: get data or a default value
fun <T> Result<T>.getOrDefault(defaultValue: @UnsafeVariance T): T = when (this) {
    is Result.Success -> data
    else -> defaultValue
}

/**
 * Exception carrying a domain [ErrorCode], thrown when a [Result.Error] is
 * unwrapped via [getOrThrow]. Lets try/catch error paths keep the typed code.
 */
class DomainException(val code: ErrorCode, message: String) : Exception(message)

// Extension: get data or throw an exception (useful for bridging to try/catch callers)
fun <T> Result<T>.getOrThrow(): T = when (this) {
    is Result.Success -> data
    is Result.Error -> throw DomainException(code, message)
    is Result.Loading -> throw IllegalStateException("Result is still loading")
}