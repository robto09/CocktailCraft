package com.cocktailcraft.android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocktailcraft.android.ui.components.AdvancedSearchBottomSheet
import com.cocktailcraft.android.ui.components.AnimatedCocktailList
import com.cocktailcraft.android.ui.components.CategoryFilterRow
import com.cocktailcraft.android.ui.components.CocktailLoadingShimmer
import com.cocktailcraft.android.ui.components.CocktailSearchBar
import com.cocktailcraft.android.ui.components.EmptySearchResultsMessage
import com.cocktailcraft.android.ui.components.ErrorToastEffect
import com.cocktailcraft.android.ui.components.HomeSortButton
import com.cocktailcraft.android.ui.components.NetworkErrorStateDisplay
import com.cocktailcraft.android.ui.components.RecentlyViewedRow
import com.cocktailcraft.android.ui.components.SearchFilterChips
import com.cocktailcraft.android.ui.preview.PreviewData
import com.cocktailcraft.android.ui.theme.AnimatedCocktailBarTheme
import com.cocktailcraft.android.ui.theme.Spacing
import com.cocktailcraft.android.util.refreshAccessibilityAction
import com.cocktailcraft.android.util.resolve
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.refresh
import com.cocktailcraft.viewmodel.SharedHomeViewModel
import com.cocktailcraft.viewmodel.state.CocktailRatingSummary
import com.cocktailcraft.viewmodel.state.HomeSortOption
import com.cocktailcraft.viewmodel.state.HomeUiState

@Composable
fun HomeScreen(
    viewModel: SharedHomeViewModel,
    onAddToCart: (Cocktail) -> Unit,
    onCocktailClick: (Cocktail) -> Unit
) {
    // HomeUiState carries the favorites too (same repository flow the
    // favorites VM observes), so the screen collects one state.
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val cocktails = state.cocktails
    val error by viewModel.error.collectAsStateWithLifecycle()
    val errorMessage = error?.message?.resolve() ?: ""
    val searchQuery = state.searchQuery

    // The selected chip, rememberSaveable so it survives navigation and
    // process death. null = "All" chip selected (matches iOS); the shared VM
    // browses the default "Cocktail" category for null.
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }

    // Function to handle category selection
    val onCategorySelected: (String?) -> Unit = { category ->
        selectedCategory = category
    }

    // Process-death recovery (AN-4): the shared VM is a process-wide singleton
    // with no SavedStateHandle, so an in-progress search dies with the
    // process. Shadow the query in the Activity's saved-state bundle and
    // replay it once into the fresh VM after a restore.
    var savedSearchQuery by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) {
        if (savedSearchQuery.isNotBlank() && searchQuery.isBlank()) {
            viewModel.updateSearchQuery(savedSearchQuery)
        }
    }
    LaunchedEffect(searchQuery) { savedSearchQuery = searchQuery }

    // The sort choice gets the same process-death shadow (saved by name).
    var savedSortOption by rememberSaveable { mutableStateOf(HomeSortOption.DEFAULT.name) }
    LaunchedEffect(Unit) {
        val restored = HomeSortOption.entries.firstOrNull { it.name == savedSortOption }
        if (restored != null && restored != HomeSortOption.DEFAULT && state.sortOption == HomeSortOption.DEFAULT) {
            viewModel.setSortOption(restored)
        }
    }
    LaunchedEffect(state.sortOption) { savedSortOption = state.sortOption.name }

    // Recently viewed has no change flow: re-read it whenever Home resumes,
    // e.g. coming back from a cocktail's detail page
    LifecycleResumeEffect(viewModel) {
        viewModel.refreshRecentlyViewed()
        onPauseOrDispose { }
    }

    // Apply the selection: on a chip tap, and again whenever Home comes
    // back (from a detail page, another tab, a restored process). The
    // ViewModel loads the initial list itself and ignores re-selecting what
    // it already shows (or a selection under an active search), so coming
    // back keeps the list, its loaded pages and scroll position.
    LaunchedEffect(selectedCategory) {
        viewModel.selectCategory(selectedCategory)
    }

    // With cocktails on screen, a failure (load more, filters, a category
    // switch, a favorite toggle) is a toast — with Retry when the error
    // carries one — instead of being cleared unseen. With nothing on screen
    // it stays in place for the full-screen error state below.
    ErrorToastEffect(
        error = error.takeIf { cocktails.isNotEmpty() },
        onConsumed = { viewModel.clearError() }
    )

    HomeContent(
        state = state,
        errorMessage = errorMessage,
        curatedCategories = viewModel.curatedCategories,
        selectedCategory = selectedCategory,
        onCategorySelected = onCategorySelected,
        // Shared debounced pipeline (SH-13): no per-keystroke coroutines.
        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
        onExitSearch = { viewModel.toggleSearchMode(false) },
        // Opening the sheet loads its option lists, once
        onSetAdvancedSearchActive = { viewModel.toggleAdvancedSearchMode(it) },
        onApplyFilters = { filters -> viewModel.applyFilters(filters) },
        onClearSearchFilters = { viewModel.clearSearchFilters() },
        onSortChange = { viewModel.setSortOption(it) },
        onRetry = { viewModel.retry() },
        onCocktailClick = onCocktailClick,
        onAddToCart = onAddToCart,
        // Through the Home ViewModel (as on iOS), so a failed toggle reports
        // on this screen's error channel rather than surfacing on Favorites
        onToggleFavorite = { cocktailToToggle -> viewModel.toggleFavorite(cocktailToToggle) },
        onLoadMore = { viewModel.loadMoreCocktails() }
    )
}

/**
 * Stateless Home content: renders [state] and reports every intent upward,
 * so it can be previewed/screenshot-tested without a ViewModel. The
 * selected category is screen-owned (rememberSaveable) and passed in.
 */
@Composable
internal fun HomeContent(
    state: HomeUiState,
    errorMessage: String,
    curatedCategories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onExitSearch: () -> Unit,
    onSetAdvancedSearchActive: (Boolean) -> Unit,
    onApplyFilters: (SearchFilters) -> Unit,
    onClearSearchFilters: () -> Unit,
    onSortChange: (HomeSortOption) -> Unit,
    onRetry: () -> Unit,
    onCocktailClick: (Cocktail) -> Unit,
    onAddToCart: (Cocktail) -> Unit,
    onToggleFavorite: (Cocktail) -> Unit,
    onLoadMore: () -> Unit
) {
    val isLoading = state.isLoading
    val cocktails = state.cocktails
    val hasMoreData = state.hasMoreData
    val isSearchActive = state.isSearchActive
    val searchQuery = state.searchQuery
    val favorites = state.favorites
    val isNetworkAvailable = state.isNetworkAvailable
    val searchFilters = state.searchFilters
    val isAdvancedSearchActive = state.isAdvancedSearchActive

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Errors over a loaded list are toasts (HomeScreen); the full-screen
        // error state below covers a load with nothing to show.

        // Search Bar with Advanced Search Button
        HomeSearchBarSection(
            searchQuery = searchQuery,
            isAdvancedSearchActive = isAdvancedSearchActive,
            hasActiveFilters = searchFilters.hasActiveFilters(),
            sortOption = state.sortOption,
            onSearchQueryChange = onSearchQueryChange,
            onClearSearch = onExitSearch,
            onToggleAdvancedSearch = { onSetAdvancedSearchActive(!isAdvancedSearchActive) },
            onSortChange = onSortChange
        )

        // Active filters display
        HomeActiveFiltersSection(
            searchFilters = searchFilters,
            onApplyFilters = onApplyFilters,
            onClearAllFilters = onClearSearchFilters
        )

        // Advanced search panel

        val categories = state.filterCategories
        val ingredients = state.filterIngredients

        // Advanced search as a modal bottom sheet, matching iOS. Apply and
        // Clear both close the sheet; filtering runs in the shared ViewModel.
        HomeAdvancedSearchSheet(
            isVisible = isAdvancedSearchActive,
            currentFilters = searchFilters,
            categories = categories,
            ingredients = ingredients,
            onApplyFilters = onApplyFilters,
            onClearFilters = onClearSearchFilters,
            onDismiss = {
                onSetAdvancedSearchActive(false)
            }
        )

        // Add Category Filter Chips - only shown when not searching.
        // Uses the shared curated list so the row matches iOS ("All" + curated
        // categories); the full API list above is only for the advanced panel.
        HomeCategoryChipsRow(
            isSearchActive = isSearchActive,
            categories = curatedCategories,
            selectedCategory = selectedCategory,
            onCategorySelected = onCategorySelected
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        // Main content wrapped in pull-to-refresh
        HomeContentSection(
            isLoading = isLoading,
            errorMessage = errorMessage,
            cocktails = cocktails,
            searchQuery = searchQuery,
            selectedCategory = selectedCategory,
            hasActiveFilters = searchFilters.hasActiveFilters(),
            isSearchActive = isSearchActive,
            hasMoreData = hasMoreData,
            favorites = favorites,
            isNetworkAvailable = isNetworkAvailable,
            recentlyViewed = if (state.showRecentlyViewed) state.recentlyViewed else emptyList(),
            ratingSummaries = state.ratingSummaries,
            onRetry = onRetry,
            onClearSearch = onExitSearch,
            onClearCategory = {
                onCategorySelected(null)
            },
            onClearFilters = {
                // Drop only the advanced filters; any active query re-runs
                // on its own (empty query + no filters = default listing).
                onApplyFilters(
                    searchFilters.copy(category = null, ingredient = null, alcoholic = null)
                )
            },
            onCocktailClick = onCocktailClick,
            onAddToCart = onAddToCart,
            onToggleFavorite = onToggleFavorite,
            onLoadMore = onLoadMore
        )
    }
}

// Search Bar with the Advanced Search and Sort buttons
@Composable
internal fun HomeSearchBarSection(
    searchQuery: String,
    isAdvancedSearchActive: Boolean,
    hasActiveFilters: Boolean,
    sortOption: HomeSortOption,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onToggleAdvancedSearch: () -> Unit,
    onSortChange: (HomeSortOption) -> Unit
) {
    CocktailSearchBar(
        searchQuery = searchQuery,
        isAdvancedSearchActive = isAdvancedSearchActive,
        hasActiveFilters = hasActiveFilters,
        onSearchQueryChange = onSearchQueryChange,
        onClearSearch = onClearSearch,
        onToggleAdvancedSearch = onToggleAdvancedSearch,
        trailingContent = { HomeSortButton(selected = sortOption, onSelect = onSortChange) }
    )
}

// Active filters display
@Composable
private fun HomeActiveFiltersSection(
    searchFilters: SearchFilters,
    onApplyFilters: (SearchFilters) -> Unit,
    onClearAllFilters: () -> Unit
) {
    SearchFilterChips(
        filters = searchFilters,
        onClearFilter = { filterType ->
            // Create a copy of current filters with the specified filter cleared
            val updatedFilters = when (filterType) {
                "category" -> searchFilters.copy(category = null)
                "ingredient" -> searchFilters.copy(ingredient = null)
                "alcoholic" -> searchFilters.copy(alcoholic = null)
                else -> searchFilters
            }
            onApplyFilters(updatedFilters)
        },
        onClearAllFilters = onClearAllFilters
    )
}

// Advanced search panel shown as a modal bottom sheet while active
@Composable
private fun HomeAdvancedSearchSheet(
    isVisible: Boolean,
    currentFilters: SearchFilters,
    categories: List<String>,
    ingredients: List<String>,
    onApplyFilters: (SearchFilters) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        AdvancedSearchBottomSheet(
            currentFilters = currentFilters,
            categories = categories,
            ingredients = ingredients,
            onApplyFilters = onApplyFilters,
            onClearFilters = onClearFilters,
            onDismiss = onDismiss
        )
    }
}

// Category Filter Chips row - only shown when not searching
@Composable
private fun HomeCategoryChipsRow(
    isSearchActive: Boolean,
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    if (!isSearchActive) {
        // No extra vertical padding: the Material chips' 48dp touch target
        // already extends ~8dp beyond their 32dp visual, which lands the
        // visible gaps on the same 12dp rhythm as the iOS header.
        CategoryFilterRow(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = onCategorySelected,
            verticalPadding = 0
        )
    }
}

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

// ---- Design-time previews ----

@Preview(name = "Home search bar — query + filters active", showBackground = true)
@Composable
private fun HomeSearchBarSectionPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        HomeSearchBarSection(
            searchQuery = "Margarita",
            isAdvancedSearchActive = false,
            hasActiveFilters = true,
            sortOption = HomeSortOption.PRICE_ASC,
            onSearchQueryChange = {},
            onClearSearch = {},
            onToggleAdvancedSearch = {},
            onSortChange = {}
        )
    }
}

@Preview(name = "Home search bar (dark)", showBackground = true)
@Composable
private fun HomeSearchBarSectionDarkPreview() {
    AnimatedCocktailBarTheme(darkTheme = true) {
        HomeSearchBarSection(
            searchQuery = "",
            isAdvancedSearchActive = false,
            hasActiveFilters = false,
            sortOption = HomeSortOption.DEFAULT,
            onSearchQueryChange = {},
            onClearSearch = {},
            onToggleAdvancedSearch = {},
            onSortChange = {}
        )
    }
}

@Preview(name = "Home active filters", showBackground = true)
@Composable
private fun HomeActiveFiltersSectionPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        HomeActiveFiltersSection(
            searchFilters = SearchFilters(
                category = "Cocktail",
                ingredient = "Tequila",
                alcoholic = true
            ),
            onApplyFilters = {},
            onClearAllFilters = {}
        )
    }
}

@Preview(name = "Home active filters (dark)", showBackground = true)
@Composable
private fun HomeActiveFiltersSectionDarkPreview() {
    AnimatedCocktailBarTheme(darkTheme = true) {
        HomeActiveFiltersSection(
            searchFilters = SearchFilters(
                category = "Cocktail",
                ingredient = "Tequila",
                alcoholic = true
            ),
            onApplyFilters = {},
            onClearAllFilters = {}
        )
    }
}

@Preview(name = "Home category chips", showBackground = true)
@Composable
private fun HomeCategoryChipsRowPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        HomeCategoryChipsRow(
            isSearchActive = false,
            categories = listOf("Cocktail", "Ordinary Drink", "Shot"),
            selectedCategory = "Cocktail",
            onCategorySelected = {}
        )
    }
}

@Preview(name = "Home category chips (dark)", showBackground = true)
@Composable
private fun HomeCategoryChipsRowDarkPreview() {
    AnimatedCocktailBarTheme(darkTheme = true) {
        HomeCategoryChipsRow(
            isSearchActive = false,
            categories = listOf("Cocktail", "Ordinary Drink", "Shot"),
            selectedCategory = "Cocktail",
            onCategorySelected = {}
        )
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