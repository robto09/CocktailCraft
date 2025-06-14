package com.cocktailcraft.util

/**
 * Centralized debug logger for cocktail loading issues
 */
object CocktailDebugLogger {
    const val TAG = "CocktailDebug"
    
    // Use expect/actual for platform-specific logging
    fun log(message: String) {
        logInternal(message)
    }
}

// Platform-specific implementation
expect fun logInternal(message: String)