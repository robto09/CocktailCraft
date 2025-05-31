package com.cocktailcraft.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * iOS implementation of ImageLoader using native URLSession with caching.
 */
actual class ImageLoader {
    
    private val urlSession: NSURLSession by lazy {
        val config = NSURLSessionConfiguration.defaultSessionConfiguration
        // Configure cache: 100MB disk cache, 10MB memory cache
        config.URLCache = NSURLCache(
            memoryCapacity = (10 * 1024 * 1024).toULong(), // 10 MB
            diskCapacity = (100 * 1024 * 1024).toULong(),   // 100 MB
            diskPath = "cocktailcraft_image_cache"
        )
        config.requestCachePolicy = NSURLRequestReturnCacheDataElseLoad
        NSURLSession.sessionWithConfiguration(config)
    }
    
    actual suspend fun preloadImages(
        imageUrls: List<String>,
        onProgress: ((Float) -> Unit)?
    ) {
        if (imageUrls.isEmpty()) return
        
        withContext(Dispatchers.IO) {
            var loadedCount = 0
            
            imageUrls.forEach { urlString ->
                if (urlString.isNotBlank()) {
                    try {
                        // Create URL and download image data
                        NSURL.URLWithString(urlString)?.let { url ->
                            downloadImageData(url)
                        }
                        
                        // Update progress
                        loadedCount++
                        val progress = loadedCount.toFloat() / imageUrls.size
                        withContext(Dispatchers.Main) {
                            onProgress?.invoke(progress)
                        }
                    } catch (e: Exception) {
                        // Log error but continue with other images
                        Logger.e("ImageLoader", "Failed to preload image: $urlString", e)
                    }
                }
            }
        }
    }
    
    private suspend fun downloadImageData(url: NSURL): NSData = suspendCancellableCoroutine { continuation ->
        val task = urlSession.dataTaskWithURL(url) { data, response, error ->
            when {
                error != null -> {
                    continuation.resumeWithException(Exception(error.localizedDescription))
                }
                data != null -> {
                    continuation.resume(data)
                }
                else -> {
                    continuation.resumeWithException(Exception("No data received"))
                }
            }
        }
        
        continuation.invokeOnCancellation {
            task.cancel()
        }
        
        task.resume()
    }
    
    actual fun clearCache() {
        // Clear URLCache
        urlSession.configuration.URLCache?.removeAllCachedResponses()
        
        // Also clear the shared URL cache
        NSURLCache.sharedURLCache.removeAllCachedResponses()
    }
}

actual class ImageLoaderFactory {
    actual fun createImageLoader(): ImageLoader = ImageLoader()
}