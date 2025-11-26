package com.cocktailcraft.di

import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.domain.usecase.PlaceOrderUseCase
import com.cocktailcraft.domain.usecase.ToggleFavoriteUseCase
import org.koin.dsl.module

/**
 * Koin module for domain-related dependencies including use cases, domain configurations, and shared ViewModels.
 */
val domainModule = module {
    // Config
    single<AppConfig> { AppConfigImpl() }

    // Use Cases
    factory { PlaceOrderUseCase(orderRepository = get()) }
    factory { ToggleFavoriteUseCase(cocktailRepository = get()) }

    // Shared ViewModels (Proof of Concept)
    factory { com.cocktailcraft.viewmodel.SharedCocktailListViewModel() }
    
    // Shared ViewModels with full SKIE integration (working ones)
    factory { com.cocktailcraft.viewmodel.SharedHomeViewModel() }
    factory { com.cocktailcraft.viewmodel.SharedCartViewModel() }
    factory { com.cocktailcraft.viewmodel.SharedCocktailDetailViewModel() }
    factory { com.cocktailcraft.viewmodel.SharedFavoritesViewModel() }
    
    // Phase 1 ViewModels - Core Dependencies (Completed)
    factory { com.cocktailcraft.viewmodel.SharedOfflineModeViewModel() }
    factory { com.cocktailcraft.viewmodel.SharedOrderViewModel() }
    
    // Phase 2 ViewModels - User Experience Features (Completed)
    factory { com.cocktailcraft.viewmodel.SharedProfileViewModel() }
    // SharedThemeViewModel MUST be a singleton so all screens share the same theme state
    single { com.cocktailcraft.viewmodel.SharedThemeViewModel() }
    factory { com.cocktailcraft.viewmodel.SharedReviewViewModel() }
}
