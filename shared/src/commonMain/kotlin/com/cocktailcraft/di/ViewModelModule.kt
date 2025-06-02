package com.cocktailcraft.di

import com.cocktailcraft.viewmodel.*
import org.koin.dsl.module

/**
 * Koin module for shared ViewModels.
 * With kmp-viewmodel, ViewModels are now regular factory instances
 * that will be properly scoped when retrieved with koinKmpViewModel()
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