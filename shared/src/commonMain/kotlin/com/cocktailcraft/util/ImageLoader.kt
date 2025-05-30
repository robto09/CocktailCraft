package com.cocktailcraft.util

/**
 * Abstract interface for image loading operations.
 * Platform-specific implementations will provide actual loading logic.
 */
expect class ImageLoader {
    /**
     * Preload a list of images into the cache.
     * 
     * @param imageUrls List of image URLs to preload
     * @param onProgress Optional callback for preloading progress (0.0-1.0)
     */
    suspend fun preloadImages(
        imageUrls: List<String>,
        onProgress: ((Float) -> Unit)? = null
    )
    
    /**
     * Clear all image caches (memory and disk).
     */
    fun clearCache()
}

/**
 * Factory to create platform-specific ImageLoader instances.
 */
expect class ImageLoaderFactory {
    fun createImageLoader(): ImageLoader
}