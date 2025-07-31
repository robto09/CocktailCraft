package com.cocktailcraft.util

import platform.Foundation.NSLog

/**
 * iOS implementation of debug logging
 */
actual fun logInternal(message: String) {
    NSLog("${CocktailDebugLogger.TAG}: $message")
}