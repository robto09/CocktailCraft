package com.cocktailcraft.viewmodel

/**
 * Factory interface for creating ViewModels.
 * This allows platform-specific implementations to create the appropriate ViewModel instances.
 */
interface ViewModelFactory {
    fun createHomeViewModel(): IHomeViewModel
    fun createCocktailDetailViewModel(): ICocktailDetailViewModel
    fun createCartViewModel(): ICartViewModel
    fun createFavoritesViewModel(): IFavoritesViewModel
    fun createThemeViewModel(): IThemeViewModel
    fun createOfflineModeViewModel(): IOfflineModeViewModel
    fun createOrderViewModel(): IOrderViewModel
    fun createProfileViewModel(): IProfileViewModel
    fun createReviewViewModel(): IReviewViewModel
}

/**
 * Expected platform-specific implementation of the ViewModelFactory.
 * Each platform will provide its own implementation.
 */
expect fun createPlatformViewModelFactory(): ViewModelFactory
