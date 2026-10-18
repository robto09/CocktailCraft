package com.cocktailcraft.android.accessibility

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.cocktailcraft.android.screens.HomeContentSection
import com.cocktailcraft.android.screens.OrderDetailContent
import com.cocktailcraft.android.screens.detail.DetailChipsSection
import com.cocktailcraft.android.screenshot.ScreenshotFixtures
import com.cocktailcraft.android.ui.components.ActiveFilterChip
import com.cocktailcraft.android.ui.components.AnimatedThemeToggleRow
import com.cocktailcraft.android.ui.components.AppTopBar
import com.cocktailcraft.android.ui.components.CartItemCard
import com.cocktailcraft.android.ui.components.CocktailItem
import com.cocktailcraft.android.ui.components.FilterSection
import com.cocktailcraft.android.ui.components.HomeSortButton
import com.cocktailcraft.android.ui.components.OfflineModeIndicator
import com.cocktailcraft.android.ui.components.RatingDisplay
import com.cocktailcraft.android.ui.components.RecentlyViewedRow
import com.cocktailcraft.android.ui.components.SectionHeader
import com.cocktailcraft.android.ui.preview.PreviewData
import com.cocktailcraft.android.ui.theme.AnimatedCocktailBarTheme
import com.cocktailcraft.viewmodel.state.HomeSortOption
import com.cocktailcraft.viewmodel.state.OrderDetailUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Pins the screen-reader semantics added in the accessibility pass: labels,
 * toggle/selection state, headings, live regions and custom actions. These
 * are invisible in screenshots, so they need their own assertions.
 */
@RunWith(RobolectricTestRunner::class)
// Plain Application: the real one starts Koin, network monitoring and WorkManager
@Config(application = Application::class)
class AccessibilitySemanticsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(content: @Composable () -> Unit) {
        composeRule.setContent {
            AnimatedCocktailBarTheme(darkTheme = false) { content() }
        }
    }

    private fun hasRole(role: Role) = SemanticsMatcher.expectValue(SemanticsProperties.Role, role)

    private fun hasStateDescription(state: String) =
        SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, state)

    private val isPoliteLiveRegion =
        SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite)

    private fun hasClickLabel(label: String) = SemanticsMatcher("onClick label = $label") {
        it.config.getOrNull(SemanticsActions.OnClick)?.label == label
    }

    @Test
    fun cocktailCard_favoriteIsAToggleWithAStableLabel() {
        setContent {
            CocktailItem(cocktail = PreviewData.cocktail, onClick = {}, onAddToCart = {}, isFavorite = true)
        }

        composeRule.onNodeWithContentDescription("Favorite")
            .assert(isToggleable())
            .assertIsOn()
        composeRule.onNodeWithContentDescription("Add Margarita to cart").assert(hasClickAction())
        composeRule.onNode(hasClickLabel("View Margarita details")).assert(hasClickAction())
    }

    @Test
    fun cartItem_stepperIsLabeledPerItemAndQuantityIsAnnounced() {
        var favorite = false
        setContent {
            CartItemCard(
                item = PreviewData.cartItem,
                onIncreaseQuantity = {},
                onDecreaseQuantity = {},
                onRemove = {},
                isFavorite = favorite,
                onToggleFavorite = { favorite = !favorite }
            )
        }

        composeRule.onNodeWithContentDescription("Increase quantity of Margarita").assert(hasClickAction())
        composeRule.onNodeWithContentDescription("Decrease quantity of Margarita").assert(hasClickAction())
        composeRule.onNodeWithContentDescription("Remove Margarita from cart").assert(hasClickAction())
        composeRule.onNodeWithContentDescription("Quantity: 2").assert(isPoliteLiveRegion)
        composeRule.onNodeWithContentDescription("Favorite").assertIsOff()
    }

    @Test
    fun sortButton_isLabeledWithItsCurrentValueAndOffersRadioOptions() {
        var picked: HomeSortOption? = null
        setContent { HomeSortButton(selected = HomeSortOption.PRICE_ASC, onSelect = { picked = it }) }

        composeRule.onNodeWithContentDescription("Sort cocktails")
            .assert(hasClickAction())
            .assert(hasStateDescription("Sorted by Price: low to high"))
            .performClick()

        composeRule.onNodeWithText("Sort by").assert(isHeading())
        composeRule.onNode(hasText("Price: low to high") and hasRole(Role.RadioButton), useUnmergedTree = false)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, true))
        composeRule.onNode(hasText("Rating") and hasRole(Role.RadioButton))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, false))
            .performClick()
        assertEquals(HomeSortOption.RATING, picked)
    }

    @Test
    fun recentlyViewedRow_hasAHeadingAndOneLabeledStopPerCocktail() {
        var opened: String? = null
        setContent { RecentlyViewedRow(cocktails = PreviewData.cocktails, onCocktailClick = { opened = it.id }) }

        composeRule.onNodeWithText("Recently viewed").assert(isHeading())
        composeRule.onNode(hasClickLabel("View ${PreviewData.cocktail.name} details"))
            .assert(hasText(PreviewData.cocktail.name))
            .performClick()
        assertEquals(PreviewData.cocktail.id, opened)
    }

    @Test
    fun ratingDisplay_readsAsOneElement() {
        setContent { RatingDisplay(rating = 4.5f, reviewCount = 12) }

        // The stars carry the spoken rating; the duplicate "4.5" is hidden
        composeRule.onNodeWithContentDescription("4.5 out of 5 stars").assertExists()
        composeRule.onNodeWithText("(12 reviews)").assertExists()
        composeRule.onAllNodesWithContentDescription("4.5", substring = false).assertCountEquals(0)
    }

    @Test
    fun titles_areHeadings() {
        setContent {
            Column {
                AppTopBar(title = "Cart")
                SectionHeader(title = "Your Cart")
            }
        }

        composeRule.onNodeWithText("Cart").assert(isHeading())
        composeRule.onNodeWithText("Your Cart").assert(isHeading())
    }

    @Test
    fun toggleRow_isASingleSwitch() {
        var checked = false
        setContent {
            AnimatedThemeToggleRow(
                title = "High Contrast",
                subtitle = "Off",
                icon = Icons.Default.Visibility,
                isChecked = checked,
                onToggle = { checked = !checked }
            )
        }

        composeRule.onNode(hasText("High Contrast"))
            .assert(isToggleable())
            .assert(hasRole(Role.Switch))
            .assertIsOff()
            .performClick()
        assertTrue(checked)
        // The visual switch inside the row is not a second focus stop
        composeRule.onAllNodes(isToggleable()).assertCountEquals(1)
    }

    @Test
    fun detailChips_areNotButtons() {
        setContent {
            DetailChipsSection(category = "Cocktail", glass = "Highball glass", alcoholic = "Alcoholic")
        }

        composeRule.onNode(hasContentDescription("Category: Cocktail"))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Role))
    }

    @Test
    fun activeFilterChip_announcesItsRemoveAction() {
        var cleared = false
        setContent { ActiveFilterChip(label = "Ingredient: Tequila", onClear = { cleared = true }) }

        composeRule.onNode(hasClickLabel("Remove filter"))
            .assert(hasText("Ingredient: Tequila"))
            .performClick()
        assertTrue(cleared)
    }

    @Test
    fun filterSection_headerIsAHeadingWithExpandedState() {
        setContent { FilterSection(title = "Category") { Text("Options") } }

        composeRule.onNode(hasText("Category"))
            .assert(isHeading())
            .assert(hasStateDescription("Expanded"))
            .assert(hasClickLabel("Collapse"))
            .performClick()
        composeRule.onNode(hasText("Category")).assert(hasStateDescription("Collapsed"))
    }

    @Test
    fun homeList_exposesPullToRefreshAsACustomAction() {
        var refreshed = 0
        setContent {
            HomeContentSection(
                isLoading = false,
                errorMessage = "",
                cocktails = PreviewData.cocktails,
                searchQuery = "",
                selectedCategory = null,
                hasActiveFilters = false,
                isSearchActive = false,
                hasMoreData = false,
                favorites = emptyList(),
                isNetworkAvailable = true,
                onRetry = { refreshed++ },
                onClearSearch = {},
                onClearCategory = {},
                onCocktailClick = {},
                onAddToCart = {},
                onToggleFavorite = {},
                onLoadMore = {}
            )
        }

        val hasRefreshAction = SemanticsMatcher("has Refresh custom action") { node ->
            node.config.getOrNull(SemanticsActions.CustomActions).orEmpty().any { it.label == "Refresh" }
        }
        val node = composeRule.onNode(hasRefreshAction).fetchSemanticsNode()
        node.config[SemanticsActions.CustomActions].first { it.label == "Refresh" }.action()
        assertEquals(1, refreshed)
    }

    @Test
    fun offlineBanner_isALiveRegion() {
        setContent { OfflineModeIndicator(isOffline = true) }

        composeRule.onNode(hasText("You're offline — showing saved cocktails.", substring = true))
            .assert(isPoliteLiveRegion)
    }

    @Test
    fun orderDetail_reorderPutsTheItemsBackAndIsOfferedForTodaysOrders() {
        var reorders = 0
        setContent {
            OrderDetailContent(
                state = OrderDetailUiState(order = ScreenshotFixtures.completedOrder),
                onBackClick = {},
                onConfirmCancel = {},
                onReorder = { reorders++ }
            )
        }

        composeRule.onNodeWithTag("order_detail_reorder")
            .performScrollTo()
            .assertIsEnabled()
            .assert(hasClickAction())
            .performClick()
        assertEquals(1, reorders)
    }

    @Test
    fun orderDetail_reorderOfAMigratedOrderIsDisabledWithItsReasonAsState() {
        // Items from before order items recorded their cocktail have no id
        setContent {
            OrderDetailContent(
                state = OrderDetailUiState(order = ScreenshotFixtures.inProgressOrder),
                onBackClick = {},
                onConfirmCancel = {}
            )
        }

        composeRule.onNodeWithTag("order_detail_reorder")
            .assertIsNotEnabled()
            .assert(hasStateDescription("Reorder isn't available for orders placed before the latest app update."))
        // The visible reason is not a second TalkBack stop
        composeRule.onAllNodes(hasText("Reorder isn't available", substring = true)).assertCountEquals(0)
    }
}
