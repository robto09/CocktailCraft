package com.coffee.store.data.config

import com.coffee.store.domain.config.AppConfig

/**
 * Implementation of AppConfig that provides configuration values for the application.
 * This is an adapter in the hexagonal architecture that implements the port defined in the domain layer.
 */
class AppConfigImpl : AppConfig {
    override val apiBaseUrl: String = "https://www.thecocktaildb.com/api/json/v1/1"
    
    override val imageBaseUrl: String = "https://www.thecocktaildb.com/images"
    
    override val ingredientsImagePath: String = "ingredients"
    
    override val cocktailsImagePath: String = "media/k/drinks"
    
    override val networkTimeoutMs: Long = 30000 // 30 seconds
    
    override val favoritesStorageKey: String = "favorite_cocktails"
    
    override val ordersStorageKey: String = "orders_data"
} 