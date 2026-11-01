package com.cocktailcraft.domain.config

import kotlin.native.HiddenFromObjC

/**
 * Application configuration interface that provides access to various configuration values.
 * This follows the hexagonal architecture by defining a port in the domain layer
 * that will be implemented by adapters in the infrastructure layer.
 */
@HiddenFromObjC // Public for :core:data; not Swift API
interface AppConfig {
    /**
     * Base URL for the API
     */
    val apiBaseUrl: String

    /** When true, Ktor logs full request/response bodies — development only. */
    val verboseNetworkLogging: Boolean

    /**
     * Default timeout for network requests in milliseconds
     */
    val networkTimeoutMs: Long

    /**
     * Maximum number of retries for network operations
     */
    val maxRetries: Int

    /**
     * Expiration time for cached data in milliseconds
     */
    val cacheExpirationMs: Long

    /**
     * Maximum number of cocktails to cache for offline mode
     */
    val maxOfflineCocktails: Int
}