package com.cocktailcraft.util

/**
 * Simple multiplatform logger
 */
object Logger {
    fun d(tag: String, message: String) {
        println("DEBUG [$tag]: $message")
    }
    
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        println("ERROR [$tag]: $message")
        throwable?.printStackTrace()
    }
    
    fun i(tag: String, message: String) {
        println("INFO [$tag]: $message")
    }
    
    fun w(tag: String, message: String) {
        println("WARN [$tag]: $message")
    }
}