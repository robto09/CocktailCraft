package com.cocktailcraft.di

import com.cocktailcraft.domain.usecase.*
import org.koin.dsl.module

/**
 * Koin module for use cases.
 * This module provides all use cases used in the application, organized by feature.
 */
val useCaseModule = module {
    // Cocktail use cases
    factory { GetCocktailsUseCase(cocktailRepository = get()) }
    factory { GetCocktailDetailsUseCase(cocktailRepository = get()) }
    factory { SearchCocktailsUseCase(cocktailRepository = get(), advancedFilterUseCase = get()) }
    factory { GetRecommendationsUseCase(cocktailRepository = get()) }
    factory { GetFilterOptionsUseCase(cocktailRepository = get()) }
    factory { AdvancedFilterUseCase() }
    
    // Favorites use cases
    factory { ManageFavoritesUseCase(cocktailRepository = get()) }
    factory { ToggleFavoriteUseCase(cocktailRepository = get()) }
    factory { FavoritesUseCase(cocktailRepository = get(), offlineModeUseCase = get()) }
    
    // Order use cases
    factory { PlaceOrderUseCase(orderRepository = get()) }
    
    // Network and offline mode use cases
    factory { NetworkStatusUseCase(cocktailRepository = get(), networkMonitor = get()) }
    factory { OfflineModeUseCase(cocktailRepository = get(), networkMonitor = get()) }
}
