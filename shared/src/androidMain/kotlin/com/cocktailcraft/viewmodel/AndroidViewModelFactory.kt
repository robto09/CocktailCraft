package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.usecase.*
import com.cocktailcraft.viewmodel.android.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android implementation of the ViewModelFactory.
 * This factory creates Android-specific ViewModel implementations.
 */
class AndroidViewModelFactory : ViewModelFactory, KoinComponent {
    // Use cases
    private val getCocktailsUseCase: GetCocktailsUseCase by inject()
    private val searchCocktailsUseCase: SearchCocktailsUseCase by inject()
    private val networkStatusUseCase: NetworkStatusUseCase by inject()
    private val getCocktailDetailsUseCase: GetCocktailDetailsUseCase by inject()
    private val getRecommendationsUseCase: GetRecommendationsUseCase by inject()
    private val manageFavoritesUseCase: ManageFavoritesUseCase by inject()
    private val manageCartUseCase: ManageCartUseCase by inject()
    private val themeUseCase: ThemeUseCase by inject()
    private val offlineModeUseCase: OfflineModeUseCase by inject()
    private val manageOrdersUseCase: ManageOrdersUseCase by inject()
    private val placeOrderUseCase: PlaceOrderUseCase by inject()
    private val authUseCase: AuthUseCase by inject()
    private val manageReviewsUseCase: ManageReviewsUseCase by inject()

    override fun createHomeViewModel(): IHomeViewModel {
        return HomeViewModel(
            getCocktailsUseCase = getCocktailsUseCase,
            searchCocktailsUseCase = searchCocktailsUseCase,
            networkStatusUseCase = networkStatusUseCase
        )
    }

    override fun createCocktailDetailViewModel(): ICocktailDetailViewModel {
        return CocktailDetailViewModel(
            getCocktailDetailsUseCase = getCocktailDetailsUseCase,
            getRecommendationsUseCase = getRecommendationsUseCase,
            manageFavoritesUseCase = manageFavoritesUseCase
        )
    }

    override fun createCartViewModel(): ICartViewModel {
        return CartViewModel(
            manageCartUseCase = manageCartUseCase
        )
    }

    override fun createFavoritesViewModel(): IFavoritesViewModel {
        return FavoritesViewModel(
            manageFavoritesUseCase = manageFavoritesUseCase
        )
    }

    override fun createThemeViewModel(): IThemeViewModel {
        return ThemeViewModel(
            themeUseCase = themeUseCase
        )
    }

    override fun createOfflineModeViewModel(): IOfflineModeViewModel {
        return OfflineModeViewModel(
            offlineModeUseCase = offlineModeUseCase,
            networkStatusUseCase = networkStatusUseCase
        )
    }

    override fun createOrderViewModel(): IOrderViewModel {
        return OrderViewModel(
            manageOrdersUseCase = manageOrdersUseCase,
            placeOrderUseCase = placeOrderUseCase
        )
    }

    override fun createProfileViewModel(): IProfileViewModel {
        return ProfileViewModel(
            authUseCase = authUseCase
        )
    }

    override fun createReviewViewModel(): IReviewViewModel {
        return ReviewViewModel(
            manageReviewsUseCase = manageReviewsUseCase
        )
    }
}

/**
 * Android implementation of the createPlatformViewModelFactory function.
 */
actual fun createPlatformViewModelFactory(): ViewModelFactory {
    return AndroidViewModelFactory()
}
