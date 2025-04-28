package com.cocktailcraft.di

import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.domain.usecase.*
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

    // New use cases
    factory { GetCocktailsUseCase(cocktailRepository = get()) }
    factory { SearchCocktailsUseCase(cocktailRepository = get()) }
    factory { GetCocktailDetailsUseCase(cocktailRepository = get()) }
    factory { ManageFavoritesUseCase(cocktailRepository = get()) }
    factory { NetworkStatusUseCase(cocktailRepository = get(), networkMonitor = get()) }
    factory { GetFilterOptionsUseCase(cocktailRepository = get()) }
    factory { GetRecommendationsUseCase(cocktailRepository = get()) }
}
