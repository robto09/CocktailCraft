package com.cocktailcraft.di

import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.cache.CocktailCacheManager
import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.data.repository.AuthRepositoryImpl
import com.cocktailcraft.data.repository.CartRepositoryImpl
import com.cocktailcraft.data.repository.CocktailFavoritesRepositoryImpl
import com.cocktailcraft.data.repository.CocktailOfflineRepositoryImpl
import com.cocktailcraft.data.repository.CocktailRepositoryImpl
import com.cocktailcraft.data.repository.FavoritesRepositoryImpl
import com.cocktailcraft.data.repository.OrderRepositoryImpl
import com.cocktailcraft.data.repository.ReviewRepositoryImpl
import com.cocktailcraft.domain.repository.AuthRepository
import com.cocktailcraft.domain.repository.CartRepository
import com.cocktailcraft.domain.repository.CocktailCatalogRepository
import com.cocktailcraft.domain.repository.CocktailDetailRepository
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.domain.repository.CocktailSearchRepository
import com.cocktailcraft.domain.repository.FavoritesRepository
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.repository.ReviewRepository
import kotlinx.serialization.json.Json
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
        }
    }

    // Cache
    single { CocktailCache(
        settings = get(),
        json = get(),
        appConfig = get()
    ) }

    // In-memory cache manager (thread-safe singleton)
    single { CocktailCacheManager() }

    // Remote data source: API access, DTO mapping, rate-limit bookkeeping
    single { CocktailRemoteDataSource(api = get(), cacheManager = get()) }

    // Focused repositories, each owning one concern
    single {
        CocktailOfflineRepositoryImpl(
            settings = get(),
            appConfig = get(),
            networkMonitor = get(),
            cocktailCache = get(),
            remote = get()
        )
    }
    single<CocktailOfflineRepository> { get<CocktailOfflineRepositoryImpl>() }

    single<CocktailFavoritesRepository> {
        CocktailFavoritesRepositoryImpl(
            settings = get(),
            appConfig = get(),
            cocktailCache = get(),
            remote = get(),
            offlineRepository = get()
        )
    }

    // Search/detail/catalog repository; composite binding delegates the
    // favorites and offline portions to the focused impls above
    single<CocktailRepository> {
        CocktailRepositoryImpl(
            remote = get(),
            cocktailCache = get(),
            cacheManager = get(),
            appConfig = get(),
            offlineRepository = get(),
            favoritesRepository = get()
        )
    }
    single<CocktailSearchRepository> { get<CocktailRepository>() }
    single<CocktailDetailRepository> { get<CocktailRepository>() }
    single<CocktailCatalogRepository> { get<CocktailRepository>() }

    single<CartRepository> {
        CartRepositoryImpl(get(), get())
    }

    single<AuthRepository> {
        AuthRepositoryImpl(get(), get())
    }

    single<OrderRepository> {
        OrderRepositoryImpl(
            settings = get(),
            json = get(),
            appConfig = get()
        )
    }

    single<ReviewRepository> {
        ReviewRepositoryImpl(
            settings = get(),
            json = get(),
            appConfig = get()
        )
    }

    single<FavoritesRepository> {
        FavoritesRepositoryImpl(
            cocktailRepository = get<CocktailFavoritesRepository>(),
            settings = get(),
            appConfig = get()
        )
    }
}
