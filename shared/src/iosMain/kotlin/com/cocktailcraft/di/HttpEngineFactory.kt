package com.cocktailcraft.di

import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*

/**
 * iOS implementation using Darwin engine.
 */
actual fun httpClientEngine(): HttpClientEngine = Darwin.create {
    configureRequest {
        setAllowsCellularAccess(true)
    }
}