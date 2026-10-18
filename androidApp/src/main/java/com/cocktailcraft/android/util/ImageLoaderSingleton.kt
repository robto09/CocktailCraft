package com.cocktailcraft.android.util

import android.content.Context
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.disk.DiskCache
import coil.imageLoader
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import com.cocktailcraft.android.BuildConfig
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * The app's single Coil [ImageLoader] with tuned caching.
 *
 * CocktailCraftApplication is Coil's ImageLoaderFactory and builds the loader
 * with [create], so Coil itself owns the instance: it is created once,
 * lazily and thread-safely, on first use. [getImageLoader] returns it.
 */
object ImageLoaderSingleton {

    // Constants for cache sizes
    private const val MEMORY_CACHE_PERCENT = 0.25 // Use 25% of available memory for the image cache
    private const val DISK_CACHE_SIZE = 100 * 1024 * 1024L // 100MB disk cache
    private const val TIMEOUT_SECONDS = 15L

    /**
     * Get the shared ImageLoader instance (created on first use).
     */
    fun getImageLoader(context: Context): ImageLoader = context.imageLoader

    /**
     * Build the ImageLoader. [okHttpClient] is the app-wide client the Ktor
     * API engine also derives from (resolved from Koin by the Application).
     */
    fun create(context: Context, okHttpClient: OkHttpClient): ImageLoader {
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
            // Network configuration: newBuilder() shares the API client's
            // connection pool and TLS sessions (same host). Its own
            // dispatcher keeps a screenful of image loads from queueing API
            // calls behind them (OkHttp runs at most 5 calls per host per
            // dispatcher).
            .okHttpClient {
                okHttpClient.newBuilder()
                    .dispatcher(Dispatcher())
                    .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .build()
            }
            // Cache policies
            .respectCacheHeaders(false) // Override server cache policies
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            // We'll skip custom decoders for now as they're causing issues
            // Coil has default decoders that will work fine
            // Verbose request logging is debug-only noise
            .logger(if (BuildConfig.DEBUG) DebugLogger() else null)
            .crossfade(true)
            .build()
    }

    /**
     * Clear all caches (memory and disk).
     */
    @OptIn(ExperimentalCoilApi::class)
    fun clearCache(context: Context) {
        val loader = getImageLoader(context)
        loader.memoryCache?.clear()
        loader.diskCache?.clear()
    }
}
