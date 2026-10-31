package com.cocktailcraft.android.accessibility

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.cocktailcraft.android.screens.AppSettingsCard
import com.cocktailcraft.android.screens.CartContent
import com.cocktailcraft.android.screens.HomeContentSection
import com.cocktailcraft.android.screens.HomeSearchBarSection
import com.cocktailcraft.android.screens.OrderDetailContent
import com.cocktailcraft.android.screens.ProfileHeaderCard
import com.cocktailcraft.android.screens.detail.DetailChipsSection
import com.cocktailcraft.android.screens.detail.DetailMainInfoCard
import com.cocktailcraft.android.screens.detail.DetailReviewsSection
import com.cocktailcraft.android.ui.components.SearchFilterChips
import com.cocktailcraft.android.ui.components.SuccessToast
import com.cocktailcraft.android.ui.preview.PreviewData
import com.cocktailcraft.android.ui.theme.AnimatedCocktailBarTheme
import com.cocktailcraft.domain.model.CocktailRatingSummary
import com.cocktailcraft.domain.model.Money
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderItem
import com.cocktailcraft.domain.model.OrderStatus
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.viewmodel.state.CartUiState
import com.cocktailcraft.viewmodel.state.HomeSortOption
import com.cocktailcraft.viewmodel.state.OrderDetailUiState
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziATFAccessibilityCheckOptions
import com.github.takahirom.roborazzi.RoborazziATFAccessibilityChecker
import com.github.takahirom.roborazzi.checkRoboAccessibility
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils.matchesCheck
import com.google.android.apps.common.testing.accessibility.framework.checks.ImageContrastCheck
import com.google.android.apps.common.testing.accessibility.framework.checks.TextContrastCheck
import org.hamcrest.CoreMatchers.anyOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Runs Google's Accessibility Test Framework over the main screens' content
 * on Robolectric, through Roborazzi (Compose's own enableAccessibilityChecks
 * is a no-op on Robolectric; it runs in androidTest instead): touch-target
 * size, missing labels on clickable nodes, duplicate labels, redundant
 * descriptions, traversal order and the rest of ATF's latest preset. Any ATF
 * warning or error fails the test. Runs at 1x and Android's 2x maximum font
 * scale.
 *
 * Contrast checks are suppressed: the brand coral (#EB6A43, 3.2:1 on white),
 * the default secondary gray (#8E8E93, 3.3:1) and the success green fall
 * short of 4.5:1 for small text by design. The in-app High Contrast setting
 * strengthens secondary text; changing the brand palette is a design
 * decision outside these checks (see README → Accessibility).
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
// Plain Application: the real one starts Koin, network monitoring and WorkManager
@Config(application = Application::class)
class AccessibilityChecksTest(private val fontScale: Float) {

    @get:Rule
    val composeRule = createComposeRule()

    @OptIn(ExperimentalRoborazziApi::class)
    private fun check(content: @Composable () -> Unit) {
        composeRule.setContent {
            AnimatedCocktailBarTheme(darkTheme = false, fontScale = fontScale) {
                content()
            }
        }
        composeRule.onRoot().checkRoboAccessibility(
            RoborazziATFAccessibilityCheckOptions(
                RoborazziATFAccessibilityChecker(
                    AccessibilityCheckPreset.LATEST,
                    anyOf(
                        matchesCheck(TextContrastCheck::class.java),
                        matchesCheck(ImageContrastCheck::class.java)
                    )
                ),
                RoborazziATFAccessibilityChecker.CheckLevel.Warning
            )
        )
    }

    @Test
    fun home() = check {
        HomeContentSection(
            isLoading = false,
            errorMessage = "",
            cocktails = PreviewData.cocktails,
            searchQuery = "",
            selectedCategory = null,
            hasActiveFilters = false,
            isSearchActive = false,
            hasMoreData = false,
            favorites = listOf(PreviewData.cocktail),
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
    fun homeSortAndRecentlyViewed() = check {
        Column {
            HomeSearchBarSection(
                searchQuery = "",
                isAdvancedSearchActive = false,
                hasActiveFilters = false,
                sortOption = HomeSortOption.RATING,
                onSearchQueryChange = {},
                onClearSearch = {},
                onToggleAdvancedSearch = {},
                onSortChange = {}
            )
            HomeContentSection(
                isLoading = false,
                errorMessage = "",
                cocktails = PreviewData.cocktails,
                searchQuery = "",
                selectedCategory = null,
                hasActiveFilters = false,
                isSearchActive = false,
                hasMoreData = false,
                favorites = listOf(PreviewData.cocktail),
                isNetworkAvailable = true,
                recentlyViewed = PreviewData.cocktails,
                ratingSummaries = mapOf(PreviewData.cocktail.id to CocktailRatingSummary(4f, 3)),
                onRetry = {},
                onClearSearch = {},
                onClearCategory = {},
                onCocktailClick = {},
                onAddToCart = {},
                onToggleFavorite = {},
                onLoadMore = {}
            )
        }
    }

    @Test
    fun activeFilters() = check {
        SearchFilterChips(
            filters = SearchFilters(category = "Cocktail", ingredient = "Tequila", alcoholic = true),
            onClearFilter = {},
            onClearAllFilters = {}
        )
    }

    @Test
    fun detail() = check {
        Column(modifier = Modifier.padding(top = 20.dp)) {
            DetailMainInfoCard(
                cocktail = PreviewData.cocktail,
                onRefreshDetails = {},
                onAddToCart = {}
            )
            DetailChipsSection(
                category = PreviewData.cocktail.category,
                glass = PreviewData.cocktail.glass,
                alcoholic = PreviewData.cocktail.alcoholic
            )
            DetailReviewsSection(reviews = PreviewData.reviews, onWriteReviewClick = {})
        }
    }

    @Test
    fun orderDetail() = check {
        OrderDetailContent(
            state = OrderDetailUiState(
                order = Order(
                    id = "ORD-42052",
                    date = "2026-07-02",
                    items = listOf(OrderItem(name = "Margarita", quantity = 2, price = Money(1250))),
                    total = Money(3099),
                    status = OrderStatus.PENDING
                )
            ),
            onBackClick = {},
            onConfirmCancel = {}
        )
    }

    @Test
    fun undoToast() = check {
        SuccessToast(message = "Removed Margarita from cart", actionLabel = "Undo", onAction = {})
    }

    @Test
    fun cart() = check {
        val item = PreviewData.cartItem
        CartContent(
            state = CartUiState(
                cartItems = listOf(item),
                totalPrice = item.lineTotal,
                itemCount = item.quantity
            ),
            error = null,
            favoriteIds = setOf(item.cocktail.id),
            onStartShopping = {},
            onIncreaseQuantity = {},
            onDecreaseQuantity = {},
            onRemove = {},
            onToggleFavorite = {},
            onConfirmOrder = {}
        )
    }

    // One card per test: stacked, they overflow the window at 2x and ATF
    // would flag the clipped bottom rows
    @Test
    fun profileHeader() = check {
        ProfileHeaderCard(
            userName = "Guest User",
            userEmail = "guest@example.com",
            isLoggedIn = false,
            onSignInClick = {},
            onSignUpClick = {}
        )
    }

    @Test
    fun appSettings() = check {
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
        @ParameterizedRobolectricTestRunner.Parameters(name = "fontScale={0}")
        fun fontScales() = listOf(1f, 2f).map { arrayOf<Any>(it) }
    }
}
