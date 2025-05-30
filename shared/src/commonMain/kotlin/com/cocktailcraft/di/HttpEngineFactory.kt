package com.cocktailcraft.di

import io.ktor.client.engine.*

/**
 * Factory for creating platform-specific HTTP client engines.
 */
expect fun httpClientEngine(): HttpClientEngine