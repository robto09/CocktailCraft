package com.cocktailcraft.di

import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.domain.usecase.PlaceOrderUseCase
import com.cocktailcraft.domain.usecase.ToggleFavoriteUseCase
import org.koin.dsl.module

/**
 * Koin module for domain-related dependencies including use cases and domain configurations.
 */
val domainModule = module {
    // Config
    single<AppConfig> { AppConfigImpl() }

    // Use Cases
    factory { PlaceOrderUseCase(orderRepository = get()) }
    factory { ToggleFavoriteUseCase(cocktailRepository = get()) }
}
