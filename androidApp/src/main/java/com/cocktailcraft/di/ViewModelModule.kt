package com.cocktailcraft.di

import com.cocktailcraft.domain.usecase.*
import com.cocktailcraft.viewmodel.*
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for ViewModels.
 * This module provides all ViewModels used in the application.
 *
 * All ViewModels follow the MVVM + Clean Architecture approach:
 * - They depend on use cases, not directly on repositories
 * - They handle UI state and user interactions
 * - They transform domain models to UI models when needed
 * - They handle error states and loading states
 *
 * This module explicitly injects use cases into ViewModels for better testability
 * and to make dependencies clear. This approach also prepares for iOS compatibility
 * by making the dependency structure explicit.
 */
val viewModelModule = module {
    // Home screen
    viewModel {
        HomeViewModel(
            getCocktailsUseCase = get(),
            searchCocktailsUseCase = get(),
            networkStatusUseCase = get(),
            getFilterOptionsUseCase = get()
        )
    }

    // Cocktail detail screen
    viewModel {
        CocktailDetailViewModel(
            getCocktailDetailsUseCase = get(),
            getRecommendationsUseCase = get(),
            manageFavoritesUseCase = get()
        )
    }

    // Cart screen
    viewModel {
        CartViewModel(
            manageCartUseCase = get()
        )
    }

    // Favorites screen
    viewModel {
        FavoritesViewModel(
            manageFavoritesUseCase = get()
        )
    }

    // Order screen
    viewModel {
        OrderViewModel(
            manageOrdersUseCase = get(),
            placeOrderUseCase = get()
        )
    }

    // Profile screen
    viewModel {
        ProfileViewModel(
            authUseCase = get()
        )
    }

    // Review screen
    viewModel {
        ReviewViewModel(
            manageReviewsUseCase = get()
        )
    }

    // Theme management
    viewModel {
        ThemeViewModel(
            themeUseCase = get()
        )
    }

    // Offline mode management
    viewModel {
        OfflineModeViewModel(
            offlineModeUseCase = get(),
            networkStatusUseCase = get()
        )
    }
}
