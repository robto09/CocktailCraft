package com.cocktailcraft.di

import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.cache.CocktailCacheManager
import com.cocktailcraft.data.repository.AuthRepositoryImpl
import com.cocktailcraft.data.repository.CartRepositoryImpl
import com.cocktailcraft.data.repository.CocktailRepositoryImpl
import com.cocktailcraft.data.repository.FavoritesRepositoryImpl
import com.cocktailcraft.data.repository.OrderRepositoryImpl
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

    // Cocktail Repository — single impl bound to all 5 focused interfaces + composite
    single<CocktailRepository> {
        CocktailRepositoryImpl(
            api = get(),
            settings = get(),
            appConfig = get(),
            networkMonitor = get(),
            cocktailCache = get(),
            cacheManager = get()
        )
    }
    single<CocktailSearchRepository> { get<CocktailRepository>() }
    single<CocktailDetailRepository> { get<CocktailRepository>() }
    single<CocktailCatalogRepository> { get<CocktailRepository>() }
    single<CocktailFavoritesRepository> { get<CocktailRepository>() }
    single<CocktailOfflineRepository> { get<CocktailRepository>() }

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

    single<FavoritesRepository> {
        FavoritesRepositoryImpl(
            cocktailRepository = get<CocktailFavoritesRepository>(),
            settings = get(),
            appConfig = get()
        )
    }
}
