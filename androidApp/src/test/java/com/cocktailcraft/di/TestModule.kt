package com.cocktailcraft.di

import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.domain.repository.AuthRepository
import com.cocktailcraft.domain.repository.CartRepository
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.usecase.PlaceOrderUseCase
import com.cocktailcraft.domain.usecase.ToggleFavoriteUseCase
import com.cocktailcraft.util.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.mockito.kotlin.mock

/**
 * Koin module for testing that provides mock implementations.
 * This module can be used in unit tests to provide mock dependencies.
 */
val testModule = module {
    // Mock repositories
    single<CocktailRepository> { mock() }
    single<CartRepository> { mock() }
    single<AuthRepository> { mock() }
    single<OrderRepository> { mock() }
    
    // Mock use cases
    factory { PlaceOrderUseCase(orderRepository = get()) }
    factory { ToggleFavoriteUseCase(cocktailRepository = get()) }
    
    // Mock config
    single<AppConfig> { AppConfigImpl() }
    
    // Mock JSON
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }
    
    // Mock NetworkMonitor
    single { 
        mock<NetworkMonitor>().apply {
            val isOnlineFlow = MutableStateFlow(true)
            org.mockito.kotlin.whenever(this.isOnline).thenReturn(isOnlineFlow)
        }
    }
}
