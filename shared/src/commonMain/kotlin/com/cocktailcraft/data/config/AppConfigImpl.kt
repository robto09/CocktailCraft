package com.cocktailcraft.data.config

import com.cocktailcraft.domain.config.AppConfig

/**
 * Implementation of AppConfig that provides configuration values for the application.
 * This is an adapter in the hexagonal architecture that implements the port defined in the domain layer.
 */
class AppConfigImpl : AppConfig {
    override val apiBaseUrl: String = "https://www.thecocktaildb.com/api/json/v1/1"

    override val imageBaseUrl: String = "https://www.thecocktaildb.com/images"

    override val ingredientsImagePath: String = "ingredients"

    override val cocktailsImagePath: String = "media/k/drinks"

    // Timeouts for network requests - generous defaults
    override val networkTimeoutMs: Long = 30000 // 30 seconds

    // Progressive timeouts for retries
    override val initialNetworkTimeoutMs: Long = 10000 // 10 seconds for first attempt
    override val maxNetworkTimeoutMs: Long = 60000 // 60 seconds max timeout

    // Number of retries for network operations
    override val maxRetries: Int = 3

    // Storage keys
    override val favoritesStorageKey: String = "favorite_cocktails"
    override val ordersStorageKey: String = "orders_data"
    override val recentlyViewedStorageKey: String = "recently_viewed_cocktails"
    override val offlineModeEnabledKey: String = "offline_mode_enabled"

    // Cache configuration
    override val cacheExpirationMs: Long = 86400000 // 24 hours cache expiration
    override val maxOfflineCocktails: Int = 50 // Maximum number of cocktails to cache
}