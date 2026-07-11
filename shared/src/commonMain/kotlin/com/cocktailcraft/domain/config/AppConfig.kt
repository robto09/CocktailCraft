package com.cocktailcraft.domain.config

/**
 * Application configuration interface that provides access to various configuration values.
 * This follows the hexagonal architecture by defining a port in the domain layer
 * that will be implemented by adapters in the infrastructure layer.
 */
interface AppConfig {
    /**
     * Base URL for the API
     */
    val apiBaseUrl: String

    /**
     * Base URL for images
     */
    val imageBaseUrl: String

    /**
     * Path for ingredient images
     */
    val ingredientsImagePath: String

    /**
     * Path for cocktail images
     */
    val cocktailsImagePath: String

    /**
     * Default timeout for network requests in milliseconds
     */
    val networkTimeoutMs: Long

    /**
     * Initial timeout for first network request attempt
     */
    val initialNetworkTimeoutMs: Long

    /**
     * Maximum timeout for network requests after retries
     */
    val maxNetworkTimeoutMs: Long

    /**
     * Maximum number of retries for network operations
     */
    val maxRetries: Int

    /**
     * Key for storing favorites in local storage
     */
    val favoritesStorageKey: String

    /**
     * Key for storing orders in local storage
     */
    val ordersStorageKey: String

    /**
     * Expiration time for cached data in milliseconds
     */
    val cacheExpirationMs: Long

    /**
     * Key for storing recently viewed cocktails in local storage
     */
    val recentlyViewedStorageKey: String

    /**
     * Maximum number of cocktails to cache for offline mode
     */
    val maxOfflineCocktails: Int

    /**
     * Key for storing offline mode status in local storage
     */
    val offlineModeEnabledKey: String
}