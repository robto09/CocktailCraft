package com.cocktailcraft.android.screens.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocktailcraft.android.ui.components.AnimatedCocktailList
import com.cocktailcraft.android.ui.components.CocktailLoadingShimmer
import com.cocktailcraft.android.ui.components.EmptySearchResultsMessage
import com.cocktailcraft.android.ui.components.NetworkErrorStateDisplay
import com.cocktailcraft.android.ui.components.RecentlyViewedRow
import com.cocktailcraft.android.ui.preview.PreviewData
import com.cocktailcraft.android.ui.theme.AnimatedCocktailBarTheme
import com.cocktailcraft.android.ui.theme.Spacing
import com.cocktailcraft.android.util.refreshAccessibilityAction
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailRatingSummary
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.refresh

// The Home screen's main area: the loading, error and empty states, and
// the cocktail list with the recently viewed row.

// Main content (loading / error / empty / list branches) wrapped in pull-to-refresh
@Composable
internal fun HomeContentSection(
    isLoading: Boolean,
    errorMessage: String,
    cocktails: List<Cocktail>,
    searchQuery: String,
    selectedCategory: String?,
    hasActiveFilters: Boolean,
    isSearchActive: Boolean,
    hasMoreData: Boolean,
    favorites: List<Cocktail>,
    isNetworkAvailable: Boolean,
    // Shown above the list when non-empty; the caller applies the visibility rules
    recentlyViewed: List<Cocktail> = emptyList(),
    ratingSummaries: Map<String, CocktailRatingSummary> = emptyMap(),
    onRetry: () -> Unit,
    onClearSearch: () -> Unit,
    onClearCategory: () -> Unit,
    onClearFilters: () -> Unit = onClearSearch,
    onCocktailClick: (Cocktail) -> Unit,
    onAddToCart: (Cocktail) -> Unit,
    onToggleFavorite: (Cocktail) -> Unit,
    onLoadMore: () -> Unit
) {
    // Material3 pull-to-refresh state
    val pullToRefreshState = rememberPullToRefreshState()
    // The first load shows the skeleton list; the refresh spinner is only
    // for reloading cocktails that are already on screen
    val isRefreshing = isLoading && cocktails.isNotEmpty()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRetry,
        modifier = Modifier
            .fillMaxSize()
            .refreshAccessibilityAction(stringResource(MR.strings.refresh), onRetry),
        state = pullToRefreshState,
        indicator = {
            // Preserve the previous Material2 indicator colors
            // (white container, primary spinner).
            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = MaterialTheme.colorScheme.surface,
                color = MaterialTheme.colorScheme.primary
            )
        }
    ) {
        if (isLoading && cocktails.isEmpty()) {
            // Show shimmer loading effect instead of spinner
            CocktailLoadingShimmer()
        } else if (errorMessage.isNotBlank() && cocktails.isEmpty()) {
            // Full-screen error only when there's no content to render —
            // with cached content on screen the top banner carries the
            // offline signal instead.
            NetworkErrorStateDisplay(
                errorMessage = errorMessage,
                isNetworkAvailable = isNetworkAvailable,
                onRetry = onRetry
            )
        } else if (cocktails.isEmpty()) {
            // Enhanced empty state message with animations
            EmptySearchResultsMessage(
                searchQuery = searchQuery,
                selectedCategory = selectedCategory,
                hasActiveFilters = hasActiveFilters,
                onClearSearch = onClearSearch,
                onClearCategory = onClearCategory,
                onClearFilters = onClearFilters
            )
        } else {
            // Use the reusable animated cocktail list component
            AnimatedCocktailList(
                cocktails = cocktails,
                isSearchActive = isSearchActive,
                selectedCategory = selectedCategory,
                hasMoreData = hasMoreData,
                favorites = favorites,
                onCocktailClick = onCocktailClick,
                onAddToCart = onAddToCart,
                onToggleFavorite = onToggleFavorite,
                onLoadMore = onLoadMore,
                ratingSummaries = ratingSummaries,
                leadingContent = if (recentlyViewed.isEmpty()) {
                    null
                } else {
                    {
                        RecentlyViewedRow(
                            cocktails = recentlyViewed,
                            onCocktailClick = onCocktailClick,
                            modifier = Modifier.padding(bottom = Spacing.sm),
                            // AnimatedCocktailList's default 16dp content padding
                            horizontalBleed = 16.dp
                        )
                    }
                }
            )
        }
    }
}

@Preview(name = "Home content — loaded", showBackground = true)
@Composable
private fun HomeContentSectionPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
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

@Preview(name = "Home content — loaded (dark)", showBackground = true)
@Composable
private fun HomeContentSectionDarkPreview() {
    AnimatedCocktailBarTheme(darkTheme = true) {
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

@Preview(name = "Home content — loaded, large font", showBackground = true, fontScale = 1.5f)
@Composable
private fun HomeContentSectionLargeFontPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
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
