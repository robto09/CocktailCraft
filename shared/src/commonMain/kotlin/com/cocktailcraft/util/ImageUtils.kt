package com.cocktailcraft.util

import com.cocktailcraft.domain.model.Cocktail

/**
 * Utility class for image operations like preloading.
 * Uses the platform-specific ImageLoader implementation.
 */
object ImageUtils {
    
    /**
     * Preload cocktail images for a list of cocktails.
     */
    suspend fun preloadCocktailImages(
        imageLoader: ImageLoader,
        cocktails: List<Cocktail>,
        onProgress: ((Float) -> Unit)? = null
    ) {
        val imageUrls = cocktails.mapNotNull { it.imageUrl }.filter { it.isNotBlank() }
        imageLoader.preloadImages(imageUrls, onProgress)
    }
}