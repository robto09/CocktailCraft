package com.cocktailcraft.di

import com.cocktailcraft.viewmodel.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Helper class to provide easy access to ViewModels from iOS/Swift code.
 * This acts as a bridge between Koin DI and Swift/SwiftUI.
 */
class KoinHelper : KoinComponent {
    // ViewModels
    fun getHomeViewModel(): HomeViewModel = getKoin().get()
    fun getCocktailDetailViewModel(): CocktailDetailViewModel = getKoin().get()
    fun getCartViewModel(): CartViewModel = getKoin().get()
    fun getFavoritesViewModel(): FavoritesViewModel = getKoin().get()
    fun getProfileViewModel(): ProfileViewModel = getKoin().get()
    fun getOrderViewModel(): OrderViewModel = getKoin().get()
    fun getOfflineModeViewModel(): OfflineModeViewModel = getKoin().get()
    fun getThemeViewModel(): ThemeViewModel = getKoin().get()
    fun getReviewViewModel(): ReviewViewModel = getKoin().get()
}

/**
 * Singleton instance for iOS to access Koin components
 */
object KoinIOS {
    val helper = KoinHelper()
}