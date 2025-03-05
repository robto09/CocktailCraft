package com.coffee.store.domain.util

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

enum class ErrorCode {
    NETWORK_ERROR,
    SERVER_ERROR,
    NOT_FOUND,
    VALIDATION_ERROR,
    UNKNOWN
} 