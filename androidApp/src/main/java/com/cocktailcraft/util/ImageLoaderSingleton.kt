package com.cocktailcraft.util

import android.content.Context
import android.os.Build
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Singleton for managing a shared ImageLoader instance with optimized caching.
 */
object ImageLoaderSingleton {
    private var imageLoader: ImageLoader? = null

    // Constants for cache sizes
    private const val MEMORY_CACHE_PERCENT = 0.25 // Use 25% of available memory for the image cache
    private const val DISK_CACHE_SIZE = 100 * 1024 * 1024L // 100MB disk cache

    /**
     * Get the shared ImageLoader instance, creating it if necessary.
     */
    fun getImageLoader(context: Context): ImageLoader {
        if (imageLoader == null) {
            synchronized(this) {
                if (imageLoader == null) {
                    imageLoader = createImageLoader(context)
                }
            }
        }
        return imageLoader!!
    }

    /**
     * Create a new ImageLoader with optimized settings.
     */
    private fun createImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            // Memory cache configuration
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(MEMORY_CACHE_PERCENT)
                    .build()
            }
            // Disk cache configuration
            .diskCache {
                DiskCache.Builder()
                    .directory(File(context.cacheDir, "image_cache"))
                    .maxSizeBytes(DISK_CACHE_SIZE)
                    .build()
            }
            // Network configuration
            .okHttpClient {
                OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build()
            }
            // Cache policies
            .respectCacheHeaders(false) // Override server cache policies
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            // We'll skip custom decoders for now as they're causing issues
            // Coil has default decoders that will work fine
            // Enable logging in debug mode
            .logger(DebugLogger())
            .crossfade(true)
            .build()
    }

    /**
     * Clear all caches (memory and disk).
     */
    @OptIn(ExperimentalCoilApi::class)
    fun clearCache() {
        imageLoader?.let { loader ->
            loader.memoryCache?.clear()
            loader.diskCache?.clear()
        }
    }
}
