package com.cocktailcraft.di

import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.cache.CocktailCacheManager
import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.data.repository.AuthRepositoryImpl
import com.cocktailcraft.data.repository.CartRepositoryImpl
import com.cocktailcraft.data.repository.CocktailCatalogRepositoryImpl
import com.cocktailcraft.data.repository.CocktailCategoryFetcher
import com.cocktailcraft.data.repository.CocktailDetailRepositoryImpl
import com.cocktailcraft.data.repository.CocktailFavoritesRepositoryImpl
import com.cocktailcraft.data.repository.CocktailOfflineRepositoryImpl
import com.cocktailcraft.data.repository.CocktailSearchRepositoryImpl
import com.cocktailcraft.data.repository.OrderRepositoryImpl
import com.cocktailcraft.data.repository.ReviewRepositoryImpl
import com.cocktailcraft.domain.repository.AuthRepository
import com.cocktailcraft.domain.repository.CartRepository
import com.cocktailcraft.domain.repository.CocktailCatalogRepository
import com.cocktailcraft.domain.repository.CocktailDetailRepository
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import com.cocktailcraft.domain.repository.CocktailSearchRepository
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.repository.ReviewRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin module for data-related dependencies including repositories and caching.
 */
val dataModule = module {
    // JSON
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            // A JSON null for a non-nullable field with a default coerces to
            // the default instead of failing the whole decode (SH-6).
            coerceInputValues = true
        }
    }

    // Background dispatcher for Settings I/O and JSON (de)serialization
    single<CoroutineDispatcher>(named("ioDispatcher")) { Dispatchers.IO }

    // Offline decision — single source of truth for "is the app offline" (AR-7)
    single {
        com.cocktailcraft.data.cache.OfflineModePolicy(
            settings = get(),
            appConfig = get(),
            networkMonitor = get(),
            ioDispatcher = get(named("ioDispatcher"))
        )
    }

    // Cache
    single { CocktailCache(
        settings = get(),
        json = get(),
        appConfig = get(),
        ioDispatcher = get(named("ioDispatcher")),
        offlineModePolicy = get()
    ) }

    // In-memory cache manager (thread-safe singleton)
    single { CocktailCacheManager() }

    // Remote data source: API access, DTO mapping, rate-limit bookkeeping
    single { CocktailRemoteDataSource(api = get(), cacheManager = get()) }

    // Focused repositories, each owning one concern
    single<CocktailOfflineRepository> {
        CocktailOfflineRepositoryImpl(
            offlineModePolicy = get(),
            cocktailCache = get(),
            cacheManager = get(),
            remote = get()
        )
    }

    single<CocktailFavoritesRepository> {
        CocktailFavoritesRepositoryImpl(
            settings = get(),
            appConfig = get(),
            cocktailCache = get(),
            remote = get(),
            offlineRepository = get(),
            ioDispatcher = get(named("ioDispatcher"))
        )
    }

    // Category fetch-and-cache path shared by the search and catalog repositories
    single {
        CocktailCategoryFetcher(
            remote = get(),
            cocktailCache = get(),
            cacheManager = get(),
            offlineRepository = get()
        )
    }

    single<CocktailSearchRepository> {
        CocktailSearchRepositoryImpl(
            remote = get(),
            cocktailCache = get(),
            offlineRepository = get(),
            categoryFetcher = get()
        )
    }

    single<CocktailDetailRepository> {
        CocktailDetailRepositoryImpl(
            remote = get(),
            cocktailCache = get(),
            appConfig = get(),
            offlineRepository = get()
        )
    }

    single<CocktailCatalogRepository> {
        CocktailCatalogRepositoryImpl(
            remote = get(),
            categoryFetcher = get()
        )
    }

    single<CartRepository> {
        CartRepositoryImpl(get(), get(), get(named("ioDispatcher")))
    }

    single<AuthRepository> {
        AuthRepositoryImpl(get(secureSettingsQualifier), get(), get(named("ioDispatcher")))
    }

    single<OrderRepository> {
        OrderRepositoryImpl(
            settings = get(),
            json = get(),
            appConfig = get(),
            ioDispatcher = get(named("ioDispatcher"))
        )
    }

    single<ReviewRepository> {
        ReviewRepositoryImpl(
            settings = get(),
            json = get(),
            appConfig = get(),
            ioDispatcher = get(named("ioDispatcher"))
        )
    }
}
