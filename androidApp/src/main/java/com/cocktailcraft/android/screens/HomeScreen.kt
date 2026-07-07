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
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.math.abs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.cocktailcraft.android.navigation.Screen
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.android.ui.components.AnimatedCocktailItem
import com.cocktailcraft.android.ui.components.AnimatedCocktailList
import com.cocktailcraft.android.ui.components.CategoryFilterRow
import com.cocktailcraft.android.ui.components.CocktailItem
import com.cocktailcraft.android.ui.components.CocktailItemShimmer
import com.cocktailcraft.android.ui.components.CocktailLoadingShimmer
import com.cocktailcraft.android.ui.components.CocktailSearchBar
import com.cocktailcraft.android.ui.components.EmptySearchResultsMessage
import com.cocktailcraft.android.ui.components.ErrorBanner
import com.cocktailcraft.android.ui.components.ErrorDialog
import com.cocktailcraft.android.ui.components.ExpandableAdvancedSearchPanel
import com.cocktailcraft.android.ui.components.FilterChip
import com.cocktailcraft.android.ui.components.NetworkErrorStateDisplay
import com.cocktailcraft.android.ui.components.SearchFilterChips
import com.cocktailcraft.android.ui.components.shimmerEffect
import com.cocktailcraft.android.ui.theme.AppColors
import com.cocktailcraft.android.util.ErrorUtils
import com.cocktailcraft.android.util.ListOptimizations.OnBottomReached
import com.cocktailcraft.android.util.ListOptimizations.OnScrollPastThreshold
import com.cocktailcraft.android.util.ListOptimizations.itemKey
import com.cocktailcraft.viewmodel.SharedFavoritesViewModel
import com.cocktailcraft.viewmodel.SharedHomeViewModel
import android.util.Log

@OptIn(ExperimentalMaterialApi::class)
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

    // Add pull-to-refresh state
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = { viewModel.retry() }
    )

    // Track if this is the first composition
    var isFirstComposition by rememberSaveable { mutableStateOf(true) }
    
    // Effect to load cocktails by category when selected category changes
    LaunchedEffect(selectedCategory) {
        Log.d("HomeScreen", "LaunchedEffect(selectedCategory): category=$selectedCategory, isSearchActive=$isSearchActive, isFirst=$isFirstComposition")
        
        // Skip loading on first composition if we already have data
        if (isFirstComposition && cocktails.isNotEmpty()) {
            Log.d("HomeScreen", "Skipping load on first composition - already have ${cocktails.size} cocktails")
            isFirstComposition = false
            return@LaunchedEffect
        }
        
        isFirstComposition = false
        
        // Only load if not in search mode and category has changed
        if (!isSearchActive) {
            Log.d("HomeScreen", "Loading cocktails for category: $selectedCategory")
            viewModel.loadCocktailsByCategory(selectedCategory)
        }
    }
    
    // Effect to clear errors when screen is displayed with data
    LaunchedEffect(cocktails) {
        Log.d("HomeScreen", "LaunchedEffect(cocktails): count=${cocktails.size}, error='$errorMessage'")
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
        CocktailSearchBar(
            searchQuery = searchQuery,
            isAdvancedSearchActive = isAdvancedSearchActive,
            hasActiveFilters = searchFilters.hasActiveFilters(),
            onSearchQueryChange = { scope.launch { viewModel.searchCocktails(it) } },
            onClearSearch = { viewModel.toggleSearchMode(false) },
            onToggleAdvancedSearch = { viewModel.toggleAdvancedSearchMode(!isAdvancedSearchActive) }
        )

        // Active filters display
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
                scope.launch { viewModel.applyFilters(updatedFilters) }
            },
            onClearAllFilters = {
                viewModel.clearSearchFilters()
            }
        )

        // Advanced search panel

        // Load the shared filter-option lists (categories / ingredients)
        // into the ViewModel state once; the panel and category row read them below.
        LaunchedEffect(Unit) {
            viewModel.loadFilterOptions()
        }
        val categories = state.filterCategories
        val ingredients = state.filterIngredients

        // Single advanced-search UI: the inline expandable panel
        ExpandableAdvancedSearchPanel(
            isExpanded = isAdvancedSearchActive,
            currentFilters = searchFilters,
            categories = categories,
            ingredients = ingredients,
            onApplyFilters = { filters ->
                scope.launch { viewModel.applyFilters(filters) }
            },
            onClearFilters = {
                viewModel.clearSearchFilters()
            }
        )

        // Add Category Filter Chips - only shown when not searching.
        // Uses the shared curated list so the row matches iOS ("All" + curated
        // categories); the full API list above is only for the advanced panel.
        if (!isSearchActive) {
            CategoryFilterRow(
                categories = viewModel.curatedCategories,
                selectedCategory = selectedCategory,
                onCategorySelected = onCategorySelected
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main content wrapped in pull-to-refresh
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
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
                    onRetry = { viewModel.retry() },
                    onEnableOfflineMode = {
                        viewModel.setOfflineMode(true)
                        viewModel.retry()
                    },
                    onGoOnline = {
                        viewModel.setOfflineMode(false)
                        viewModel.retry()
                    }
                )
            } else if (cocktails.isEmpty()) {
                // Enhanced empty state message with animations
                EmptySearchResultsMessage(
                    searchQuery = searchQuery,
                    selectedCategory = selectedCategory,
                    hasActiveFilters = searchFilters.hasActiveFilters(),
                    onClearSearch = {
                        viewModel.toggleSearchMode(false)
                    },
                    onClearCategory = {
                        onCategorySelected(null)
                    }
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
                    onToggleFavorite = { cocktailToToggle ->
                        scope.launch { favoritesViewModel.toggleFavorite(cocktailToToggle) }
                    },
                    onLoadMore = {
                        scope.launch { viewModel.loadMoreCocktails() }
                    }
                )
            }

            // Pull refresh indicator
            PullRefreshIndicator(
                refreshing = isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = Color.White,
                contentColor = AppColors.Primary
            )
        }
    }
}