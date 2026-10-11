package com.cocktailcraft.android.screenshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cocktailcraft.android.screens.AppSettingsCard
import com.cocktailcraft.android.ui.components.CartItemCard
import com.cocktailcraft.android.ui.components.CategoryFilterRow
import com.cocktailcraft.android.ui.components.CocktailItem
import com.cocktailcraft.android.ui.components.PrimaryButton
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

/**
 * Key components under the in-app accessibility/appearance settings: high
 * contrast (dark), 1.5x font scale, and a non-default accent color.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
class AccessibilityScreenshotTest(variant: ThemeVariant) : ScreenshotTest(variant) {

    @Test
    fun cocktailItem() = snapshot("CocktailItem") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CocktailItem(
                cocktail = ScreenshotFixtures.cocktail,
                onClick = {},
                onAddToCart = {},
                isFavorite = true
            )
            CocktailItem(
                cocktail = ScreenshotFixtures.outOfStockCocktail,
                onClick = {},
                onAddToCart = {}
            )
        }
    }

    @Test
    fun cartItemAndCheckout() = snapshot("CartItemAndCheckout") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CartItemCard(
                item = ScreenshotFixtures.cartItem,
                onIncreaseQuantity = {},
                onDecreaseQuantity = {},
                onRemove = {},
                isFavorite = false,
                onToggleFavorite = {}
            )
            PrimaryButton(text = "Place Order", onClick = {}, modifier = Modifier.fillMaxWidth())
        }
    }

    @Test
    fun categoryFilterRow() = snapshot("CategoryFilterRow") {
        CategoryFilterRow(
            categories = ScreenshotFixtures.categories,
            selectedCategory = "Cocktail",
            onCategorySelected = {}
        )
    }

    @Test
    fun appSettingsCard() = snapshot("AppSettingsCard") {
        AppSettingsCard(
            isLoggedIn = true,
            isDarkMode = false,
            isSystemTheme = true,
            onOrderHistoryClick = {},
            onToggleFollowSystemTheme = {},
            onToggleDarkMode = {},
            onLogoutClick = {}
        )
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun variants() = listOf(
            ThemeVariant.HighContrastDark,
            ThemeVariant.LargeFont,
            ThemeVariant.AccentBlue
        ).map { arrayOf<Any>(it) }
    }
}
