package com.cocktailcraft.android.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.math.abs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.cocktailcraft.android.navigation.Screen
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.android.ui.components.AnimatedCocktailList
import com.cocktailcraft.android.ui.components.CategoryFilterRow
import com.cocktailcraft.android.ui.components.CocktailLoadingShimmer
import com.cocktailcraft.android.ui.components.CocktailSearchBar
import com.cocktailcraft.android.ui.components.EmptySearchResultsMessage
import com.cocktailcraft.android.ui.components.AdvancedSearchBottomSheet
import com.cocktailcraft.android.ui.components.NetworkErrorStateDisplay
import com.cocktailcraft.android.ui.components.SearchFilterChips
import com.cocktailcraft.android.ui.preview.PreviewData
import com.cocktailcraft.android.ui.theme.AnimatedCocktailBarTheme
import com.cocktailcraft.android.ui.theme.AppColors
import com.cocktailcraft.android.ui.theme.Spacing
import com.cocktailcraft.android.util.ListOptimizations.OnBottomReached
import com.cocktailcraft.android.util.ListOptimizations.OnScrollPastThreshold
import com.cocktailcraft.android.util.ListOptimizations.itemKey
import com.cocktailcraft.viewmodel.SharedFavoritesViewModel
import com.cocktailcraft.viewmodel.SharedHomeViewModel
import android.util.Log
import com.cocktailcraft.android.BuildConfig

@Composable
fun HomeScreen(
    viewModel: SharedHomeViewModel,
    favoritesViewModel: SharedFavoritesViewModel,
    onAddToCart: (Cocktail) -> Unit,
    onCocktailClick: (Cocktail) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading = state.isLoading
    val favoritesState by favoritesViewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val cocktails = state.cocktails
    val isLoadingMore = state.isLoadingMore
    val hasMoreData = state.hasMoreData
    val error by viewModel.error.collectAsStateWithLifecycle()
    val errorMessage = error?.message ?: ""
    val isSearchActive = state.isSearchActive
    val searchQuery = state.searchQuery
    val favorites = favoritesState.favorites
    val isOfflineMode = state.isOfflineMode
    val isNetworkAvailable = state.isNetworkAvailable
    val searchFilters = state.searchFilters
    val isAdvancedSearchActive = state.isAdvancedSearchActive

    // State for advanced search panel

    // Add state for selected category - use rememberSaveable to persist across navigation.
    // null = "All" chip selected (matches iOS); the shared VM browses the
    // default "Cocktail" category for null.
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }

    // Function to handle category selection
    val onCategorySelected: (String?) -> Unit = { category ->
        selectedCategory = category
    }

    // Track if this is the first composition
    var isFirstComposition by rememberSaveable { mutableStateOf(true) }

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
    
    // Effect to load cocktails by category when selected category changes
    LaunchedEffect(selectedCategory) {
        if (BuildConfig.DEBUG) {
            Log.d("HomeScreen", "LaunchedEffect(selectedCategory): category=$selectedCategory, isSearchActive=$isSearchActive, isFirst=$isFirstComposition")
        }

        // Skip loading on first composition if we already have data
        if (isFirstComposition && cocktails.isNotEmpty()) {
            if (BuildConfig.DEBUG) {
                Log.d("HomeScreen", "Skipping load on first composition - already have ${cocktails.size} cocktails")
            }
            isFirstComposition = false
            return@LaunchedEffect
        }

        isFirstComposition = false

        // Only load if not in search mode and category has changed
        if (!isSearchActive) {
            if (BuildConfig.DEBUG) {
                Log.d("HomeScreen", "Loading cocktails for category: $selectedCategory")
            }
            viewModel.loadCocktailsByCategory(selectedCategory)
        }
    }

    // Clear stale errors once data is on screen — keyed on the error rather
    // than the list, so it doesn't re-run on every list mutation (AN-9).
    LaunchedEffect(errorMessage) {
        if (BuildConfig.DEBUG) {
            Log.d("HomeScreen", "LaunchedEffect(errorMessage): count=${cocktails.size}, error='$errorMessage'")
        }
        if (cocktails.isNotEmpty() && errorMessage.isNotBlank()) {
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        // For now, we'll skip the error banner since we're using legacy error handling
        // We'll implement it in the future when we fully migrate to the new error system

        // Search Bar with Advanced Search Button
        HomeSearchBarSection(
            searchQuery = searchQuery,
            isAdvancedSearchActive = isAdvancedSearchActive,
            hasActiveFilters = searchFilters.hasActiveFilters(),
            // Shared debounced pipeline (SH-13): no per-keystroke coroutines.
            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
            onClearSearch = { viewModel.toggleSearchMode(false) },
            onToggleAdvancedSearch = { viewModel.toggleAdvancedSearchMode(!isAdvancedSearchActive) }
        )

        // Active filters display
        HomeActiveFiltersSection(
            searchFilters = searchFilters,
            onApplyFilters = { updatedFilters ->
                scope.launch { viewModel.applyFilters(updatedFilters) }
            },
            onClearAllFilters = {
                viewModel.clearSearchFilters()
            }
        )

        // Advanced search panel

        // Load the shared filter-option lists (categories / ingredients)
        // into the ViewModel state once; the sheet and category row read them below.
        LaunchedEffect(Unit) {
            viewModel.loadFilterOptions()
        }
        val categories = state.filterCategories
        val ingredients = state.filterIngredients

        // Advanced search as a modal bottom sheet, matching iOS. Apply and
        // Clear both close the sheet; filtering runs in the shared ViewModel.
        HomeAdvancedSearchSheet(
            isVisible = isAdvancedSearchActive,
            currentFilters = searchFilters,
            categories = categories,
            ingredients = ingredients,
            onApplyFilters = { filters ->
                scope.launch { viewModel.applyFilters(filters) }
            },
            onClearFilters = {
                viewModel.clearSearchFilters()
            },
            onDismiss = {
                viewModel.toggleAdvancedSearchMode(false)
            }
        )

        // Add Category Filter Chips - only shown when not searching.
        // Uses the shared curated list so the row matches iOS ("All" + curated
        // categories); the full API list above is only for the advanced panel.
        HomeCategoryChipsRow(
            isSearchActive = isSearchActive,
            categories = viewModel.curatedCategories,
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
            isLoadingMore = isLoadingMore,
            hasMoreData = hasMoreData,
            favorites = favorites,
            isOfflineMode = isOfflineMode,
            isNetworkAvailable = isNetworkAvailable,
            onRetry = { viewModel.retry() },
            onEnableOfflineMode = {
                viewModel.setOfflineMode(true)
                viewModel.retry()
            },
            onGoOnline = {
                viewModel.setOfflineMode(false)
                viewModel.retry()
            },
            onClearSearch = {
                viewModel.toggleSearchMode(false)
            },
            onClearCategory = {
                onCategorySelected(null)
            },
            onClearFilters = {
                // Drop only the advanced filters; any active query re-runs
                // on its own (empty query + no filters = default listing).
                scope.launch {
                    viewModel.applyFilters(
                        searchFilters.copy(category = null, ingredient = null, alcoholic = null)
                    )
                }
            },
            onCocktailClick = onCocktailClick,
            onAddToCart = onAddToCart,
            onToggleFavorite = { cocktailToToggle ->
                scope.launch { favoritesViewModel.toggleFavorite(cocktailToToggle) }
            },
            onLoadMore = {
                scope.launch { viewModel.loadMoreCocktails() }
            }
        )
    }
}

// Search Bar with Advanced Search Button
@Composable
private fun HomeSearchBarSection(
    searchQuery: String,
    isAdvancedSearchActive: Boolean,
    hasActiveFilters: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onToggleAdvancedSearch: () -> Unit
) {
    CocktailSearchBar(
        searchQuery = searchQuery,
        isAdvancedSearchActive = isAdvancedSearchActive,
        hasActiveFilters = hasActiveFilters,
        onSearchQueryChange = onSearchQueryChange,
        onClearSearch = onClearSearch,
        onToggleAdvancedSearch = onToggleAdvancedSearch
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
private fun HomeContentSection(
    isLoading: Boolean,
    errorMessage: String,
    cocktails: List<Cocktail>,
    searchQuery: String,
    selectedCategory: String?,
    hasActiveFilters: Boolean,
    isSearchActive: Boolean,
    isLoadingMore: Boolean,
    hasMoreData: Boolean,
    favorites: List<Cocktail>,
    isOfflineMode: Boolean,
    isNetworkAvailable: Boolean,
    onRetry: () -> Unit,
    onEnableOfflineMode: () -> Unit,
    onGoOnline: () -> Unit,
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

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = onRetry,
        modifier = Modifier.fillMaxSize(),
        state = pullToRefreshState,
        indicator = {
            // Preserve the previous Material2 indicator colors
            // (white container, primary spinner).
            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = isLoading,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = Color.White,
                color = AppColors.Primary
            )
        }
    ) {
        if (isLoading && cocktails.isEmpty()) {
            // Show shimmer loading effect instead of spinner
            CocktailLoadingShimmer()
        } else if (errorMessage.isNotBlank()) {
            NetworkErrorStateDisplay(
                errorMessage = errorMessage,
                isOfflineMode = isOfflineMode,
                isNetworkAvailable = isNetworkAvailable,
                hasContent = cocktails.isNotEmpty(),
                onRetry = onRetry,
                onEnableOfflineMode = onEnableOfflineMode,
                onGoOnline = onGoOnline
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
                isLoadingMore = isLoadingMore,
                hasMoreData = hasMoreData,
                favorites = favorites,
                onCocktailClick = onCocktailClick,
                onAddToCart = onAddToCart,
                onToggleFavorite = onToggleFavorite,
                onLoadMore = onLoadMore
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
            onSearchQueryChange = {},
            onClearSearch = {},
            onToggleAdvancedSearch = {}
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
            onSearchQueryChange = {},
            onClearSearch = {},
            onToggleAdvancedSearch = {}
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
            isLoadingMore = false,
            hasMoreData = false,
            favorites = emptyList(),
            isOfflineMode = false,
            isNetworkAvailable = true,
            onRetry = {},
            onEnableOfflineMode = {},
            onGoOnline = {},
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
            isLoadingMore = false,
            hasMoreData = false,
            favorites = emptyList(),
            isOfflineMode = false,
            isNetworkAvailable = true,
            onRetry = {},
            onEnableOfflineMode = {},
            onGoOnline = {},
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
            isLoadingMore = false,
            hasMoreData = false,
            favorites = emptyList(),
            isOfflineMode = false,
            isNetworkAvailable = true,
            onRetry = {},
            onEnableOfflineMode = {},
            onGoOnline = {},
            onClearSearch = {},
            onClearCategory = {},
            onCocktailClick = {},
            onAddToCart = {},
            onToggleFavorite = {},
            onLoadMore = {}
        )
    }
}