package com.cocktailcraft.di

import com.cocktailcraft.viewmodel.*
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for ViewModels.
 * This module provides all ViewModels used in the application.
 */
val viewModelModule = module {
    // Legacy ViewModels
    viewModel { HomeViewModel() }
    viewModel { CocktailDetailViewModel(get(), get()) }
    viewModel { CartViewModel() }
    viewModel { FavoritesViewModel() }
    viewModel { OrderViewModel() }
    viewModel { ProfileViewModel() }
    viewModel { ReviewViewModel() }
    viewModel { ThemeViewModel() }
    viewModel { OfflineModeViewModel() }
    
    // Enhanced ViewModels with use case architecture
    viewModel { EnhancedHomeViewModel() }
    viewModel { EnhancedCocktailDetailViewModel() }
    viewModel { EnhancedFavoritesViewModel() }
}
