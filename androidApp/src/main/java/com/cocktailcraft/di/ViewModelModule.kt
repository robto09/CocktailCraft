package com.cocktailcraft.di

import com.cocktailcraft.viewmodel.*
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for ViewModels.
 * This module provides all ViewModels used in the application.
 */
val viewModelModule = module {
    // Original ViewModels
    viewModel { HomeViewModel() }
    viewModel { CocktailDetailViewModel(get(), get()) }
    viewModel { CartViewModel() }
    viewModel { FavoritesViewModel() }
    viewModel { OrderViewModel() }
    viewModel { ProfileViewModel() }
    viewModel { ReviewViewModel() }
    viewModel { ThemeViewModel() }
    viewModel { OfflineModeViewModel() }
    
    // Refactored ViewModels
    viewModel { RefactoredHomeViewModel() }
    viewModel { RefactoredCocktailDetailViewModel() }
    viewModel { RefactoredFavoritesViewModel() }
}
