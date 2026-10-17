package com.cocktailcraft.android.screenshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.unit.dp
import com.cocktailcraft.android.ui.components.AppTopBar
import com.cocktailcraft.android.ui.components.CocktailSearchBar
import com.cocktailcraft.android.ui.components.RatingBar
import com.cocktailcraft.android.ui.components.RatingDisplay
import com.cocktailcraft.android.ui.components.SectionHeader
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

/** App bar, search bar, headers and ratings — the chrome around the lists. */
@RunWith(ParameterizedRobolectricTestRunner::class)
class ChromeScreenshotTest(variant: ThemeVariant) : ScreenshotTest(variant) {

    @Test
    fun appTopBar() = snapshot("AppTopBar") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Insets zeroed: the status-bar height differs between hosts
            AppTopBar(title = "CocktailCraft", windowInsets = WindowInsets(0))
            AppTopBar(
                title = "Margarita",
                showBackButton = true,
                windowInsets = WindowInsets(0),
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Default.Share, contentDescription = null) }
                }
            )
        }
    }

    @Test
    fun searchBar() = snapshot("CocktailSearchBar") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CocktailSearchBar(
                searchQuery = "",
                isAdvancedSearchActive = false,
                hasActiveFilters = false,
                onSearchQueryChange = {},
                onClearSearch = {},
                onToggleAdvancedSearch = {}
            )
            CocktailSearchBar(
                searchQuery = "Margarita",
                isAdvancedSearchActive = true,
                hasActiveFilters = true,
                onSearchQueryChange = {},
                onClearSearch = {},
                onToggleAdvancedSearch = {}
            )
        }
    }

    @Test
    fun sectionHeader() = snapshot("SectionHeader") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeader(title = "Popular Cocktails")
            SectionHeader(title = "Reviews (2)", actionText = "Write a Review", onActionClick = {})
        }
    }

    @Test
    fun ratings() = snapshot("Ratings") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            RatingBar(rating = 3f)
            RatingBar(rating = 3.5f, useHalfStars = true, starSize = 24.dp)
            RatingDisplay(rating = 4.5f, reviewCount = 12)
            RatingDisplay(rating = 0f, reviewCount = 0)
        }
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun variants() = ThemeVariant.lightAndDark()
    }
}
