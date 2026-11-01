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
    // Injected per-platform from the DI modules: debug builds get
    // verbose wire logging automatically; release builds stay quiet. The
    // default keeps test fixtures terse.
    override val verboseNetworkLogging: Boolean = false,
    // TheCocktailDB key for this build: the cocktailDbApiKey build input
    // (see core/data/build.gradle.kts), "1" — the public test key — unless
    // one is configured. A parameter so tests can pin it.
    apiKey: String = BuildApiConfig.COCKTAILDB_API_KEY
) : AppConfig {
    init {
        require(API_KEY_PATTERN.matches(apiKey)) { "TheCocktailDB API key must be letters and digits only" }
    }

    override val apiBaseUrl: String = "$API_ROOT/$apiKey"

    // Budget for a whole request, retries included, and for each socket
    // read. NetworkModule caps the connect phase lower (CONNECT_TIMEOUT_MS,
    // 5 seconds), so an unreachable server surfaces its error in about 10
    // seconds instead of 20.
    override val networkTimeoutMs: Long = 10_000 // 10 seconds

    // Number of retries for network operations (consumed by NetworkModule's
    // HttpRequestRetry — the single retry layer). One quick
    // retry: each further one only delays the error or the cached fallback.
    override val maxRetries: Int = 1

    // Cache configuration (enforced by CocktailCache)
    override val cacheExpirationMs: Long = 86400000 // 24 hours cache expiration

    // Maximum number of cocktails to cache. A single category can return ~100
    // drinks, so this leaves room for several browsed categories offline.
    override val maxOfflineCocktails: Int = 500

    private companion object {
        const val API_ROOT = "https://www.thecocktaildb.com/api/json/v1"

        // The key is a URL path segment
        val API_KEY_PATTERN = Regex("[A-Za-z0-9]+")
    }
}
