package com.cocktailcraft.di

import kotlinx.serialization.json.Json
import org.koin.dsl.module

/**
 * Koin module for common dependencies shared across all modules.
 * This module provides basic utilities and configurations that are used by multiple modules.
 */
val commonModule = module {
    // JSON serialization
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = false
            encodeDefaults = true
        }
    }
}
