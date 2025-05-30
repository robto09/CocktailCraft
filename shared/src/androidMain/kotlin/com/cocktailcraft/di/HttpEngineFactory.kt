package com.cocktailcraft.di

import io.ktor.client.engine.*
import io.ktor.client.engine.android.*

/**
 * Android implementation using Android engine.
 */
actual fun httpClientEngine(): HttpClientEngine = Android.create {
    // Android engine configuration
    connectTimeout = 30_000
    socketTimeout = 30_000
}