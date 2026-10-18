package com.cocktailcraft.android.screenshot

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCart
import com.cocktailcraft.android.screens.CartContent
import com.cocktailcraft.android.screens.FavoritesContent
import com.cocktailcraft.android.screens.OrderListContent
import com.cocktailcraft.android.ui.components.CocktailLoadingShimmer
import com.cocktailcraft.android.ui.components.EmptySearchResultsMessage
import com.cocktailcraft.android.ui.components.EmptyStateComponent
import com.cocktailcraft.android.ui.components.EndOfListMessage
import com.cocktailcraft.android.ui.components.ErrorBanner
import com.cocktailcraft.android.ui.components.NetworkErrorStateDisplay
import com.cocktailcraft.android.ui.components.OfflineModeIndicator
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.viewmodel.state.CartUiState
import com.cocktailcraft.viewmodel.state.FavoritesUiState
import com.cocktailcraft.viewmodel.state.OrderUiState
import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.StringDesc
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

/** Empty, error, offline and loading states (full-screen ones fill the 360x640dp window). */
@RunWith(ParameterizedRobolectricTestRunner::class)
class StateScreenshotTest(variant: ThemeVariant) : ScreenshotTest(variant) {

    @Test
    fun emptyStateFavorites() = snapshot("EmptyState_favorites") {
        EmptyStateComponent(
            title = "No favorites yet",
            message = "Tap the heart on any cocktail to save it here.",
            actionButtonText = "Browse Cocktails",
            icon = Icons.Filled.Favorite
        )
    }

    @Test
    fun emptyStateIcon() = snapshot("EmptyState_icon") {
        EmptyStateComponent(
            title = "Your cart is empty",
            message = "Add some cocktails to get started.",
            actionButtonText = "Start Shopping",
            icon = Icons.Filled.ShoppingCart
        )
    }

    @Test
    fun noResultsForQuery() = snapshot("EmptySearchResults_query") {
        EmptySearchResultsMessage(searchQuery = "Zombie", onClearSearch = {})
    }

    @Test
    fun noResultsForCategory() = snapshot("EmptySearchResults_category") {
        EmptySearchResultsMessage(
            searchQuery = "",
            selectedCategory = "Punch / Party Drink",
            onClearSearch = {},
            onClearCategory = {}
        )
    }

    @Test
    fun noResultsForFilters() = snapshot("EmptySearchResults_filters") {
        EmptySearchResultsMessage(searchQuery = "", hasActiveFilters = true, onClearSearch = {})
    }

    @Test
    fun networkErrorOffline() = snapshot("NetworkError_offline") {
        NetworkErrorStateDisplay(
            errorMessage = "No internet connection",
            isNetworkAvailable = false,
            onRetry = {}
        )
    }

    @Test
    fun networkErrorServer() = snapshot("NetworkError_server") {
        NetworkErrorStateDisplay(
            errorMessage = "The server returned an error. Please try again.",
            isNetworkAvailable = true,
            onRetry = {}
        )
    }

    @Test
    fun banners() = snapshot("Banners") {
        Column {
            OfflineModeIndicator(isOffline = true)
            ErrorBanner(
                error = ErrorHandler.UserFriendlyError(
                    title = StringDesc.Raw("Connection problem"),
                    message = StringDesc.Raw("Showing cached cocktails."),
                    category = ErrorHandler.ErrorCategory.NETWORK
                ),
                onDismiss = {},
                onAction = {}
            )
            ErrorBanner(
                error = ErrorHandler.UserFriendlyError(
                    title = StringDesc.Raw("Something went wrong"),
                    message = StringDesc.Raw("Please try again later."),
                    category = ErrorHandler.ErrorCategory.SERVER
                ),
                onDismiss = {}
            )
            EndOfListMessage(visible = true)
        }
    }

    @Test
    fun loadingShimmer() = snapshot("LoadingShimmer") {
        CocktailLoadingShimmer(itemCount = 3)
    }

    @Test
    fun favoritesLoading() = snapshot("FavoritesLoading") {
        FavoritesContent(
            state = FavoritesUiState(isLoading = true),
            error = null,
            onRefresh = {},
            onBrowseProducts = {},
            onAddToCart = {},
            onToggleFavorite = {}
        )
    }

    @Test
    fun cartLoading() = snapshot("CartLoading") {
        CartContent(
            state = CartUiState(isLoading = true),
            error = null,
            favoriteIds = emptySet(),
            onStartShopping = {},
            onIncreaseQuantity = {},
            onDecreaseQuantity = {},
            onRemove = {},
            onToggleFavorite = {},
            onConfirmOrder = {}
        )
    }

    @Test
    fun ordersLoading() = snapshot("OrdersLoading") {
        OrderListContent(
            state = OrderUiState(isLoading = true),
            error = null,
            onReload = {},
            onBrowseCocktails = {}
        )
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun variants() = ThemeVariant.lightAndDark()
    }
}
