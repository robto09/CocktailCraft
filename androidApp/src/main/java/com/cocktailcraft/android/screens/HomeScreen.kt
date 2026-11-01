package com.cocktailcraft.android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocktailcraft.android.screens.home.HomeActiveFiltersSection
import com.cocktailcraft.android.screens.home.HomeAdvancedSearchSheet
import com.cocktailcraft.android.screens.home.HomeCategoryChipsRow
import com.cocktailcraft.android.screens.home.HomeContentSection
import com.cocktailcraft.android.screens.home.HomeSearchBarSection
import com.cocktailcraft.android.ui.components.ErrorToastEffect
import com.cocktailcraft.android.ui.theme.Spacing
import com.cocktailcraft.android.util.resolve
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.viewmodel.SharedHomeViewModel
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

    // Process-death recovery: the shared VM is a process-wide singleton
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
        // Shared debounced pipeline: no per-keystroke coroutines.
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
