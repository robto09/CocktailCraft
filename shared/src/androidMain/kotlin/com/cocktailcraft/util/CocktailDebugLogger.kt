package com.cocktailcraft.util

import android.util.Log

actual fun logInternal(message: String) {
    // Use Log.e for now to ensure it shows up (priority 6)
    Log.e(CocktailDebugLogger.TAG, message)
    // Also use println as fallback
    println("CocktailDebug: $message")
}