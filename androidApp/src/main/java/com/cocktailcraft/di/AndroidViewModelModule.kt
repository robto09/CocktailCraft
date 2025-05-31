package com.cocktailcraft.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.koin.core.qualifier.named

/**
 * Android-specific Koin module for ViewModels.
 * This overrides the shared module's ViewModels with proper Android ViewModels.
 */
val androidViewModelModule = module {
    // Theme ViewModel - explicitly use Android's version
    viewModel { com.cocktailcraft.viewmodel.ThemeViewModel() }
    
    // Cart ViewModel
    viewModel { com.cocktailcraft.viewmodel.CartViewModel() }
    
    // Cocktail Detail ViewModel - already defined in recommendationModule
    // viewModel { CocktailDetailViewModel(get(), get()) }
    
    // Favorites ViewModel
    viewModel { com.cocktailcraft.viewmodel.FavoritesViewModel() }
    
    // Home ViewModel
    viewModel { com.cocktailcraft.viewmodel.HomeViewModel() }
    
    // Offline Mode ViewModel
    viewModel { com.cocktailcraft.viewmodel.OfflineModeViewModel() }
    
    // Order ViewModel
    viewModel { com.cocktailcraft.viewmodel.OrderViewModel() }
    
    // Profile ViewModel
    viewModel { com.cocktailcraft.viewmodel.ProfileViewModel() }
    
    // Review ViewModel
    viewModel { com.cocktailcraft.viewmodel.ReviewViewModel() }
}