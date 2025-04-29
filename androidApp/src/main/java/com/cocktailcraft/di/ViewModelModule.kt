package com.cocktailcraft.di

import com.cocktailcraft.viewmodel.*
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for ViewModels.
 * This module provides all ViewModels used in the application.
 *
 * The ViewModels are organized into two categories:
 * 1. Legacy ViewModels: Original implementation with direct repository access
 * 2. Enhanced ViewModels: Improved implementation using use cases and clean architecture
 */
val viewModelModule = module {
    // Legacy ViewModels - Original implementation with direct repository access
    viewModel { HomeViewModel() }
    viewModel { CocktailDetailViewModel(get(), get()) }
    viewModel { CartViewModel() }
    viewModel { FavoritesViewModel() }
    viewModel { OrderViewModel() }
    viewModel { ProfileViewModel() }
    viewModel { ReviewViewModel() }
    viewModel { ThemeViewModel() }
    viewModel { OfflineModeViewModel() }

    // Enhanced ViewModels - Improved implementation using use cases and clean architecture
    viewModel { EnhancedHomeViewModel() }
    viewModel { EnhancedCocktailDetailViewModel() }
    viewModel { EnhancedFavoritesViewModel() }
}
