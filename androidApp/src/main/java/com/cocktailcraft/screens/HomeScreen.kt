package com.cocktailcraft.screens

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.math.abs
import kotlinx.coroutines.delay
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
import com.cocktailcraft.navigation.Screen
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.ui.components.AdvancedSearchPanel
import com.cocktailcraft.ui.components.AnimatedCocktailItem
import com.cocktailcraft.ui.components.AnimatedCocktailList
import com.cocktailcraft.ui.components.CategoryFilterRow
import com.cocktailcraft.ui.components.CocktailItem
import com.cocktailcraft.ui.components.CocktailItemShimmer
import com.cocktailcraft.ui.components.CocktailLoadingShimmer
import com.cocktailcraft.ui.components.CocktailSearchBar
import com.cocktailcraft.ui.components.EmptySearchResultsMessage
import com.cocktailcraft.ui.components.ErrorBanner
import com.cocktailcraft.ui.components.ErrorDialog
import com.cocktailcraft.ui.components.ExpandableAdvancedSearchPanel
import com.cocktailcraft.ui.components.FilterChip
import com.cocktailcraft.ui.components.NetworkErrorStateDisplay
import com.cocktailcraft.ui.components.SearchFilterChips
import com.cocktailcraft.ui.components.shimmerEffect
import com.cocktailcraft.util.FilterOptionsLoader
import com.cocktailcraft.ui.theme.AppColors
import com.cocktailcraft.util.ErrorUtils
import com.cocktailcraft.util.ListOptimizations.OnBottomReached
import com.cocktailcraft.util.ListOptimizations.OnScrollPastThreshold
import com.cocktailcraft.util.ListOptimizations.itemKey
import com.cocktailcraft.viewmodel.CartViewModel
import com.cocktailcraft.viewmodel.FavoritesViewModel
import com.cocktailcraft.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    cartViewModel: CartViewModel,
    favoritesViewModel: FavoritesViewModel,
    onAddToCart: (Cocktail) -> Unit,
    onCocktailClick: (Cocktail) -> Unit
) {
    val cocktails by viewModel.cocktails.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val hasMoreData by viewModel.hasMoreData.collectAsState()
    val legacyErrorString by viewModel.errorString.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val favorites by favoritesViewModel.favorites.collectAsState()
    val isOfflineMode by viewModel.isOfflineMode.collectAsState()
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsState()
    val searchFilters by viewModel.searchFilters.collectAsState()
    val isAdvancedSearchActive by viewModel.isAdvancedSearchActive.collectAsState()

    // State for error dialog
    var showErrorDialog by remember { mutableStateOf(false) }

    // State for advanced search panel
    var showAdvancedSearch by remember { mutableStateOf(false) }

    // Add state for selected category
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    // Function to handle category selection
    val onCategorySelected: (String?) -> Unit = { category ->
        selectedCategory = category
    }

    // Add pull-to-refresh state
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = { viewModel.retry() }
    )

    // Effect to load cocktails by category when selected category changes
    LaunchedEffect(selectedCategory) {
        if (!isSearchActive) {
            viewModel.loadCocktailsByCategory(selectedCategory)
        }
    }

    // Define common cocktail categories
    val categories = listOf(
        "All", "Cocktail", "Ordinary Drink", "Shot", "Coffee / Tea",
        "Punch / Party Drink", "Homemade Liqueur", "Beer", "Soft Drink"
    )

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
            onSearchQueryChange = { viewModel.searchCocktails(it) },
            onClearSearch = { viewModel.toggleSearchMode(false) },
            onToggleAdvancedSearch = { viewModel.toggleAdvancedSearchMode(!isAdvancedSearchActive) },
            onShowAdvancedSearchDialog = { showAdvancedSearch = true }
        )

        // Active filters display
        SearchFilterChips(
            filters = searchFilters,
            onClearFilter = { filterType ->
                // Create a copy of current filters with the specified filter cleared
                val updatedFilters = when (filterType) {
                    "category" -> searchFilters.copy(category = null)
                    "ingredient" -> searchFilters.copy(ingredient = null)
                    "ingredients" -> searchFilters.copy(ingredients = emptyList())
                    "excludeIngredients" -> searchFilters.copy(excludeIngredients = emptyList())
                    "alcoholic" -> searchFilters.copy(alcoholic = null)
                    "glass" -> searchFilters.copy(glass = null)
                    "priceRange" -> searchFilters.copy(priceRange = null)
                    "tasteProfile" -> searchFilters.copy(tasteProfile = null)
                    "complexity" -> searchFilters.copy(complexity = null)
                    "preparationTime" -> searchFilters.copy(preparationTime = null)
                    else -> searchFilters
                }
                viewModel.updateSearchFilters(updatedFilters)
            },
            onClearAllFilters = {
                viewModel.clearSearchFilters()
            }
        )

        // Advanced search panel

        // Load filter options using the utility
        val filterOptions = FilterOptionsLoader.rememberFilterOptions(repository = viewModel.repository)
        val categories = filterOptions.categories
        val ingredients = filterOptions.ingredients
        val glasses = filterOptions.glasses

        // Use the dialog version when in dialog mode
        if (showAdvancedSearch) {
            AdvancedSearchPanel(
                isVisible = true,
                currentFilters = searchFilters,
                categories = categories,
                ingredients = ingredients,
                glasses = glasses,
                onApplyFilters = { filters ->
                    viewModel.updateSearchFilters(filters)
                    showAdvancedSearch = false
                },
                onClearFilters = {
                    viewModel.clearSearchFilters()
                },
                onDismiss = {
                    showAdvancedSearch = false
                }
            )
        }

        // Use the expandable panel version for inline display
        ExpandableAdvancedSearchPanel(
            isExpanded = isAdvancedSearchActive,
            currentFilters = searchFilters,
            categories = categories,
            ingredients = ingredients,
            glasses = glasses,
            onApplyFilters = { filters ->
                viewModel.updateSearchFilters(filters)
            },
            onClearFilters = {
                viewModel.clearSearchFilters()
            }
        )

        // Add Category Filter Chips - only shown when not searching
        if (!isSearchActive) {
            CategoryFilterRow(
                categories = categories,
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
            } else if (legacyErrorString.isNotBlank()) {
                NetworkErrorStateDisplay(
                    errorMessage = legacyErrorString,
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
                        favoritesViewModel.toggleFavorite(cocktailToToggle)
                    },
                    onLoadMore = {
                        viewModel.loadMoreCocktails()
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