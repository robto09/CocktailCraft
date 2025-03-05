package com.coffee.store.di

import com.coffee.store.data.config.AppConfigImpl
import com.coffee.store.data.remote.CocktailApi
import com.coffee.store.data.remote.CocktailApiImpl
import com.coffee.store.data.repository.AuthRepositoryImpl
import com.coffee.store.data.repository.CartRepositoryImpl
import com.coffee.store.data.repository.CocktailRepositoryImpl
import com.coffee.store.data.repository.OrderRepositoryImpl
import com.coffee.store.domain.config.AppConfig
import com.coffee.store.domain.repository.AuthRepository
import com.coffee.store.domain.repository.CartRepository
import com.coffee.store.domain.repository.CocktailRepository
import com.coffee.store.domain.repository.OrderRepository
import com.coffee.store.domain.usecase.PlaceOrderUseCase
import com.coffee.store.domain.usecase.ToggleFavoriteUseCase
import com.russhwolf.settings.Settings
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val appModule = module {
    // Config
    single<AppConfig> { AppConfigImpl() }
    
    // HTTP Client
    single { 
        HttpClient {
            install(ContentNegotiation) {
                json(get<Json>())
            }
            install(Logging) {
                level = LogLevel.INFO
            }
        }
    }
    
    // API
    single<CocktailApi> { CocktailApiImpl(get()) }
    
    // JSON
    single { 
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }
    
    // Repositories
    single<CocktailRepository> { 
        CocktailRepositoryImpl(
            api = get(),
            settings = get(),
            appConfig = get()
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
    
    // Use Cases
    factory { PlaceOrderUseCase(orderRepository = get()) }
    factory { ToggleFavoriteUseCase(cocktailRepository = get()) }
}

expect fun platformModule(): org.koin.core.module.Module 