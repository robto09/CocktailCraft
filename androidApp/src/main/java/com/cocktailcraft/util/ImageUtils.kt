package com.cocktailcraft.util

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.cocktailcraft.domain.model.Cocktail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Utility class for image operations like preloading and advanced image handling.
 */
object ImageUtils {
    
    /**
     * Preload a list of images into the cache.
     * 
     * @param context The application context
     * @param imageUrls List of image URLs to preload
     * @param onProgress Optional callback for preloading progress (0.0-1.0)
     */
    fun preloadImages(
        context: Context,
        imageUrls: List<String>,
        onProgress: ((Float) -> Unit)? = null
    ) {
        if (imageUrls.isEmpty()) return
        
        val imageLoader = ImageLoaderSingleton.getImageLoader(context)
        val scope = CoroutineScope(Dispatchers.IO)
        
        scope.launch {
            var loadedCount = 0
            
            imageUrls.forEach { url ->
                if (url.isNotBlank()) {
                    val request = ImageRequest.Builder(context)
                        .data(url)
                        .memoryCacheKey(url)
                        .diskCacheKey(url)
                        .build()
                    
                    try {
                        // Execute the request to preload the image
                        val result = imageLoader.execute(request)
                        
                        // Update progress
                        loadedCount++
                        val progress = loadedCount.toFloat() / imageUrls.size
                        withContext(Dispatchers.Main) {
                            onProgress?.invoke(progress)
                        }
                        
                        // Log success or failure
                        when (result) {
                            is SuccessResult -> {
                                // Image loaded successfully
                            }
                            is ErrorResult -> {
                                // Image failed to load
                            }
                        }
                    } catch (e: Exception) {
                        // Handle exceptions
                    }
                }
            }
        }
    }
    
    /**
     * Preload cocktail images for a list of cocktails.
     */
    fun preloadCocktailImages(
        context: Context,
        cocktails: List<Cocktail>,
        onProgress: ((Float) -> Unit)? = null
    ) {
        val imageUrls = cocktails.mapNotNull { it.imageUrl }.filter { it.isNotBlank() }
        preloadImages(context, imageUrls, onProgress)
    }
    
    /**
     * Build an optimized image request with proper sizing and caching.
     * 
     * @param url The image URL
     * @param size Target size for the image (for resizing)
     * @param placeholder Placeholder drawable
     * @param error Error drawable
     * @param allowHardware Whether to allow hardware bitmaps (disable for shared elements)
     */
    @Composable
    fun buildOptimizedImageRequest(
        url: String?,
        size: Int? = null,
        placeholder: Drawable? = null,
        error: Drawable? = null,
        allowHardware: Boolean = true
    ): ImageRequest {
        val context = LocalContext.current
        
        return ImageRequest.Builder(context)
            .data(url)
            .apply {
                // Apply size if provided (for resizing)
                size?.let { 
                    size(it)
                }
                
                // Set placeholders
                placeholder?.let { placeholder(it) }
                error?.let { error(it) }
                
                // Cache configuration
                memoryCacheKey(url)
                diskCacheKey(url)
                
                // Performance settings
                allowHardware(allowHardware)
                crossfade(true)
            }
            .build()
    }
}
