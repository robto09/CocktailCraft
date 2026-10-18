package com.cocktailcraft.data.config

import com.cocktailcraft.domain.config.AppConfig
import kotlin.native.HiddenFromObjC

/**
 * Implementation of AppConfig that provides configuration values for the application.
 * This is an adapter in the hexagonal architecture that implements the port defined in the domain layer.
 *
 * Public (was internal) so the :shared DI graph tests can stand in for the
 * platform module; :core:data is neither exported to Swift nor visible to
 * androidApp, so this widens nothing beyond the umbrella.
 */
@HiddenFromObjC // AppConfig is; K/N requires its subtypes to match
class AppConfigImpl(
    // Injected per-platform from the DI modules (AR-4): debug builds get
    // verbose wire logging automatically; release builds stay quiet. The
    // default keeps test fixtures terse.
    override val verboseNetworkLogging: Boolean = false
) : AppConfig {
    override val apiBaseUrl: String = "https://www.thecocktaildb.com/api/json/v1/1"

    // Budget for a whole request and for each socket read. NetworkModule
    // caps the connect phase lower (CONNECT_TIMEOUT_MS, 10 seconds).
    override val networkTimeoutMs: Long = 20_000 // 20 seconds

    // Number of retries for network operations (consumed by NetworkModule's
    // HttpRequestRetry — the single retry layer, SH-7/AR-4)
    override val maxRetries: Int = 3

    // Cache configuration (enforced by CocktailCache)
    override val cacheExpirationMs: Long = 86400000 // 24 hours cache expiration
    // Maximum number of cocktails to cache. A single category can return ~100
    // drinks, so this leaves room for several browsed categories offline.
    override val maxOfflineCocktails: Int = 500
}