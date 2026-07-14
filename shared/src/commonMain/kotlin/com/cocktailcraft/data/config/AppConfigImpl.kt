package com.cocktailcraft.data.config

import com.cocktailcraft.domain.config.AppConfig

/**
 * Implementation of AppConfig that provides configuration values for the application.
 * This is an adapter in the hexagonal architecture that implements the port defined in the domain layer.
 */
internal class AppConfigImpl(
    // Injected per-platform from the DI modules (AR-4): debug builds get
    // verbose wire logging automatically; release builds stay quiet. The
    // default keeps test fixtures terse.
    override val verboseNetworkLogging: Boolean = false
) : AppConfig {
    override val apiBaseUrl: String = "https://www.thecocktaildb.com/api/json/v1/1"

    override val imageBaseUrl: String = "https://www.thecocktaildb.com/images"

    override val ingredientsImagePath: String = "ingredients"

    override val cocktailsImagePath: String = "media/k/drinks"

    // Timeouts for network requests - generous defaults
    override val networkTimeoutMs: Long = 30000 // 30 seconds

    // Number of retries for network operations (consumed by NetworkModule's
    // HttpRequestRetry — the single retry layer, SH-7/AR-4)
    override val maxRetries: Int = 3

    // Storage keys
    override val favoritesStorageKey: String = "favorite_cocktails"
    override val ordersStorageKey: String = "orders_data"
    override val reviewsStorageKey: String = "user_reviews"
    override val recentlyViewedStorageKey: String = "recently_viewed_cocktails"
    override val offlineModeEnabledKey: String = "offline_mode_enabled"

    // Cache configuration (enforced by CocktailCache)
    override val cacheExpirationMs: Long = 86400000 // 24 hours cache expiration
    override val maxOfflineCocktails: Int = 100 // Maximum number of cocktails to cache
}