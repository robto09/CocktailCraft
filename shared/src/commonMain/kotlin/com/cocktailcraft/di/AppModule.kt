package com.cocktailcraft.di

import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.data.remote.CocktailApi
import com.cocktailcraft.data.remote.CocktailApiImpl
import com.cocktailcraft.data.repository.AuthRepositoryImpl
import com.cocktailcraft.data.repository.CartRepositoryImpl
import com.cocktailcraft.data.repository.CocktailRepositoryImpl
import com.cocktailcraft.data.repository.OrderRepositoryImpl
import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.domain.repository.AuthRepository
import com.cocktailcraft.domain.repository.CartRepository
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.usecase.PlaceOrderUseCase
import com.cocktailcraft.domain.usecase.ToggleFavoriteUseCase
import com.russhwolf.settings.Settings
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
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
// The platformModule function is declared in PlatformModule.kt 