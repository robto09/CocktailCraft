package com.cocktailcraft.di

import com.cocktailcraft.viewmodel.*
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Android-specific Koin module that properly scopes shared ViewModels for Android.
 * Creates new instances of ViewModels with proper Android lifecycle management.
 */
val androidViewModelModule = module {
    // Theme ViewModel
    viewModel { ThemeViewModel() }
    
    // Cart ViewModel
    viewModel { CartViewModel() }
    
    // Cocktail Detail ViewModel
    viewModel { CocktailDetailViewModel(get(), get()) }
    
    // Favorites ViewModel
    viewModel { FavoritesViewModel() }
    
    // Home ViewModel
    viewModel { HomeViewModel() }
    
    // Offline Mode ViewModel
    viewModel { OfflineModeViewModel() }
    
    // Order ViewModel
    viewModel { OrderViewModel() }
    
    // Profile ViewModel
    viewModel { ProfileViewModel() }
    
    // Review ViewModel
    viewModel { ReviewViewModel() }
}