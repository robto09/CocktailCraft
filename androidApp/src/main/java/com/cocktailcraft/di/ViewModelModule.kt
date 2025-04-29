package com.cocktailcraft.di

import com.cocktailcraft.viewmodel.*
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for ViewModels.
 * This module provides all ViewModels used in the application.
 *
 * All ViewModels now use the clean architecture approach with use cases
 * instead of directly accessing repositories.
 */
val viewModelModule = module {
    // ViewModels
    viewModel { HomeViewModel() }
    viewModel { CocktailDetailViewModel() }
    viewModel { CartViewModel() }
    viewModel { FavoritesViewModel() }
    viewModel { OrderViewModel() }
    viewModel { ProfileViewModel() }
    viewModel { ReviewViewModel() }
    viewModel { ThemeViewModel() }
    viewModel { OfflineModeViewModel() }
}
