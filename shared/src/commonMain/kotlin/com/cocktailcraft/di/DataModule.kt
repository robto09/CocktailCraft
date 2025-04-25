package com.cocktailcraft.di

import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.repository.AuthRepositoryImpl
import com.cocktailcraft.data.repository.CartRepositoryImpl
import com.cocktailcraft.data.repository.CocktailRepositoryImpl
import com.cocktailcraft.data.repository.OrderRepositoryImpl
import com.cocktailcraft.domain.repository.AuthRepository
import com.cocktailcraft.domain.repository.CartRepository
import com.cocktailcraft.domain.repository.CocktailRepository
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
    single { CocktailCache(get(), get(), get()) }

    // Repositories
    single<CocktailRepository> {
        CocktailRepositoryImpl(
            api = get(),
            settings = get(),
            appConfig = get(),
            json = get(),
            networkMonitor = get()
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
}
