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
import com.cocktailcraft.data.repository.CocktailRepositoryImpl
import com.cocktailcraft.data.repository.CocktailSearchRepositoryImpl
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
            cacheManager = get(),
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

    // Composite binding survives only for the iOS KoinHelper's exported
    // Objective-C surface; it delegates every call to the focused impls above
    single<CocktailRepository> {
        CocktailRepositoryImpl(
            searchRepository = get(),
            detailRepository = get(),
            catalogRepository = get(),
            favoritesRepository = get(),
            offlineRepository = get()
        )
    }

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
