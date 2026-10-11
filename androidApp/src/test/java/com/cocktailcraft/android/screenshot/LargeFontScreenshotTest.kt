package com.cocktailcraft.android.screenshot

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cocktailcraft.android.screens.CartContent
import com.cocktailcraft.android.screens.HomeContentSection
import com.cocktailcraft.android.screens.detail.DetailMainInfoCard
import com.cocktailcraft.viewmodel.state.CartUiState
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

/**
 * Key screens at large font scales (1.5x and Android's 2x maximum), so
 * truncation, overlap and clipped controls show up as screenshot diffs.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
class LargeFontScreenshotTest(variant: ThemeVariant) : ScreenshotTest(variant) {

    @Test
    fun homeLoaded() = snapshot("Home_loaded") {
        HomeContentSection(
            isLoading = false,
            errorMessage = "",
            cocktails = ScreenshotFixtures.cocktails,
            searchQuery = "",
            selectedCategory = null,
            hasActiveFilters = false,
            isSearchActive = false,
            isLoadingMore = false,
            hasMoreData = false,
            favorites = listOf(ScreenshotFixtures.cocktail),
            isNetworkAvailable = true,
            onRetry = {},
            onClearSearch = {},
            onClearCategory = {},
            onCocktailClick = {},
            onAddToCart = {},
            onToggleFavorite = {},
            onLoadMore = {}
        )
    }

    @Test
    fun detailMainInfo() = snapshot("Detail_mainInfo") {
        // The card overlaps the hero image by 20dp; pad so it isn't clipped
        Column(modifier = Modifier.padding(top = 20.dp)) {
            DetailMainInfoCard(
                cocktail = ScreenshotFixtures.cocktail,
                isFavorite = true,
                isInCart = false,
                onToggleFavorite = {},
                onAddToCartClick = {},
                onRefreshDetails = {}
            )
        }
    }

    @Test
    fun cart() = snapshot("Cart") {
        val item = ScreenshotFixtures.cartItem
        CartContent(
            state = CartUiState(
                cartItems = listOf(item),
                totalPrice = item.cocktail.price * item.quantity,
                itemCount = item.quantity
            ),
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

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun variants() = listOf(ThemeVariant.LargeFont, ThemeVariant.LargestFont).map { arrayOf<Any>(it) }
    }
}
