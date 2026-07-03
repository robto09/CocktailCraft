package com.cocktailcraft

import com.cocktailcraft.domain.repository.AuthRepository
import com.cocktailcraft.domain.repository.CartRepository
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.viewmodel.SharedCocktailListViewModel
import com.cocktailcraft.viewmodel.SharedFavoritesViewModel
import com.cocktailcraft.viewmodel.SharedHomeViewModel
import com.cocktailcraft.viewmodel.SharedCartViewModel
import com.cocktailcraft.viewmodel.SharedCocktailDetailViewModel
import com.cocktailcraft.viewmodel.SharedOfflineModeViewModel
import com.cocktailcraft.viewmodel.SharedOrderViewModel
import com.cocktailcraft.viewmodel.SharedProfileViewModel
import com.cocktailcraft.viewmodel.SharedThemeViewModel
import com.cocktailcraft.viewmodel.SharedReviewViewModel
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

fun doInitKoin(): KoinApplication = initKoin {}

// Helper class to provide dependencies to iOS without using reified generics.
// Explicit return types define the exported Objective-C surface.
class KoinHelper : KoinComponent {
    fun getCocktailRepository(): CocktailRepository = get()

    fun getAuthRepository(): AuthRepository = get()

    fun getCartRepository(): CartRepository = get()

    fun getOrderRepository(): OrderRepository = get()

    // Shared ViewModels
    fun getSharedCocktailListViewModel(): SharedCocktailListViewModel = get()

    fun getSharedFavoritesViewModel(): SharedFavoritesViewModel = get()

    fun getSharedHomeViewModel(): SharedHomeViewModel = get()

    fun getSharedCartViewModel(): SharedCartViewModel = get()

    fun getSharedCocktailDetailViewModel(): SharedCocktailDetailViewModel = get()

    fun getSharedOfflineModeViewModel(): SharedOfflineModeViewModel = get()

    fun getSharedOrderViewModel(): SharedOrderViewModel = get()

    fun getSharedProfileViewModel(): SharedProfileViewModel = get()

    fun getSharedThemeViewModel(): SharedThemeViewModel = get()

    fun getSharedReviewViewModel(): SharedReviewViewModel = get()
}
