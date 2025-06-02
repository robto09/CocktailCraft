package com.cocktailcraft.di

import com.cocktailcraft.viewmodel.*
import org.koin.dsl.module

/**
 * Koin module for shared ViewModels.
 * Using factory pattern for cross-platform compatibility.
 * Android will properly scope these as ViewModels when retrieved with koinViewModel()
 */
val viewModelModule = module {
    // Theme ViewModel
    factory { ThemeViewModel() }
    
    // Cart ViewModel
    factory { CartViewModel() }
    
    // Cocktail Detail ViewModel
    factory { CocktailDetailViewModel(get(), get()) }
    
    // Favorites ViewModel
    factory { FavoritesViewModel() }
    
    // Home ViewModel
    factory { HomeViewModel() }
    
    // Offline Mode ViewModel
    factory { OfflineModeViewModel() }
    
    // Order ViewModel
    factory { OrderViewModel() }
    
    // Profile ViewModel
    factory { ProfileViewModel() }
    
    // Review ViewModel
    factory { ReviewViewModel() }
}