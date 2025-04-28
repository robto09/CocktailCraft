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
import com.cocktailcraft.ui.components.ExpandableAdvancedSearchPanel
import com.cocktailcraft.ui.components.AnimatedCocktailItem
import com.cocktailcraft.ui.components.CocktailItem
import com.cocktailcraft.ui.components.CocktailItemShimmer
import com.cocktailcraft.ui.components.EmptySearchResultsMessage
import com.cocktailcraft.ui.components.shimmerEffect
import com.cocktailcraft.ui.components.ErrorBanner
import com.cocktailcraft.ui.components.ErrorDialog
import com.cocktailcraft.ui.components.FilterChip
import com.cocktailcraft.ui.components.SearchFilterChips
import com.cocktailcraft.ui.theme.AppColors
import com.cocktailcraft.util.ErrorUtils
import com.cocktailcraft.util.ListOptimizations.OnBottomReached
import com.cocktailcraft.util.ListOptimizations.OnScrollPastThreshold
import com.cocktailcraft.util.ListOptimizations.itemKey
import com.cocktailcraft.viewmodel.CartViewModel
import com.cocktailcraft.viewmodel.FavoritesViewModel
import com.cocktailcraft.viewmodel.HomeViewModel

/**
 * Get an appropriate icon for the error category
 */
@Composable
private fun getErrorIcon(category: ErrorUtils.ErrorCategory): ImageVector {
    return when (category) {
        ErrorUtils.ErrorCategory.NETWORK -> Icons.Default.WifiOff
        ErrorUtils.ErrorCategory.SERVER,
        ErrorUtils.ErrorCategory.CLIENT -> Icons.Default.Warning
        else -> Icons.Default.Error
    }
}

/**
 * Get an appropriate color for the error category
 */
@Composable
private fun getErrorColor(category: ErrorUtils.ErrorCategory): Color {
    return when (category) {
        ErrorUtils.ErrorCategory.NETWORK -> AppColors.Primary
        ErrorUtils.ErrorCategory.SERVER -> Color(0xFFF57C00) // Orange
        ErrorUtils.ErrorCategory.AUTHENTICATION -> Color(0xFFD32F2F) // Red
        else -> AppColors.Error
    }
}

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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchCocktails(it) },
                modifier = Modifier
                    .weight(1f),
                placeholder = { Text("Search cocktails...") },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = AppColors.Gray
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.toggleSearchMode(false) }) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Clear search",
                                tint = AppColors.Gray
                            )
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    cursorColor = AppColors.Primary,
                    focusedIndicatorColor = AppColors.Primary,
                    focusedLeadingIconColor = AppColors.Primary
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Advanced search button
            val isAdvancedSearchActive by viewModel.isAdvancedSearchActive.collectAsState()

            IconButton(
                onClick = {
                    // Toggle between dialog mode and expandable panel mode
                    if (isAdvancedSearchActive) {
                        // If already expanded, just collapse it
                        viewModel.toggleAdvancedSearchMode(false)
                    } else {
                        // If not expanded, toggle between dialog and expandable panel
                        if (searchFilters.hasActiveFilters()) {
                            // If filters are already applied, just expand the panel
                            viewModel.toggleAdvancedSearchMode(true)
                        } else {
                            // If no filters applied, show the dialog for better UX
                            showAdvancedSearch = true
                        }
                    }
                },
                modifier = Modifier
                    .background(
                        color = if (isAdvancedSearchActive || searchFilters.hasActiveFilters())
                            AppColors.Primary
                        else
                            AppColors.LightGray,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterAlt,
                    contentDescription = "Advanced Search",
                    tint = if (isAdvancedSearchActive || searchFilters.hasActiveFilters())
                        Color.White
                    else
                        AppColors.TextSecondary
                )
            }
        }

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

        // Load filter options
        var categories by remember { mutableStateOf(listOf("Cocktail", "Ordinary Drink", "Shot", "Coffee / Tea", "Punch / Party Drink", "Homemade Liqueur", "Beer", "Soft Drink")) }
        var ingredients by remember { mutableStateOf(listOf<String>()) }
        var glasses by remember { mutableStateOf(listOf<String>()) }

        // Load filter options from repository
        LaunchedEffect(Unit) {
            try {
                viewModel.repository.getCategories().collect { categoryList ->
                    categories = categoryList
                }

                viewModel.repository.getIngredients().collect { ingredientList ->
                    ingredients = ingredientList
                }

                viewModel.repository.getGlasses().collect { glassList ->
                    glasses = glassList
                }
            } catch (e: Exception) {
                // Use default values if loading fails
            }
        }

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
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category || (category == "All" && selectedCategory == null),
                        onClick = {
                            onCategorySelected(if (category == "All") null else category)
                        },
                        label = category
                    )
                }
            }
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
                ) {
                    // Shimmer header
                    item(key = "shimmer_header") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerEffect()
                        )
                    }

                    // Shimmer items
                    items(5) { index ->
                        CocktailItemShimmer()
                    }
                }
            } else if (legacyErrorString.isNotBlank()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Legacy error handling
                        Icon(
                            imageVector = if (!isNetworkAvailable || isOfflineMode) Icons.Default.WifiOff else Icons.Default.Error,
                            contentDescription = "Error",
                            tint = if (!isNetworkAvailable || isOfflineMode) AppColors.Primary else Color.Red,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = when {
                                isOfflineMode -> "Offline Mode Active"
                                !isNetworkAvailable -> "Network Unavailable"
                                else -> "Unable to load cocktails"
                            },
                            color = if (!isNetworkAvailable || isOfflineMode) AppColors.Primary else Color.Red,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Show error message or a fallback
                        Text(
                            text = when {
                                isOfflineMode && cocktails.isEmpty() ->
                                    "No cached cocktails available. Connect to the internet to download cocktails."
                                !isNetworkAvailable ->
                                    "You're currently offline. Enable Offline Mode to browse cached cocktails."
                                else -> legacyErrorString
                            },
                            color = AppColors.TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.retry() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.Primary
                                )
                            ) {
                                Text("Retry")
                            }

                            when {
                                // If network is unavailable and offline mode is not enabled
                                !isNetworkAvailable && !isOfflineMode -> {
                                    Button(
                                        onClick = {
                                            viewModel.setOfflineMode(true)
                                            viewModel.retry()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AppColors.Secondary
                                        )
                                    ) {
                                        Text("Enable Offline Mode")
                                    }
                                }
                                // If offline mode is enabled but we want to go back online
                                isOfflineMode && isNetworkAvailable -> {
                                    Button(
                                        onClick = {
                                            viewModel.setOfflineMode(false)
                                            viewModel.retry()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AppColors.Secondary
                                        )
                                    ) {
                                        Text("Go Online")
                                    }
                                }
                            }
                        }
                    }
                }
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
                // Remember scroll state for optimizations
                val listState = rememberLazyListState()

                // Detect when we're near the bottom to load more items
                listState.OnBottomReached(buffer = 5) {
                    // Only load more if not already loading and not searching
                    if (!isLoading && !isSearchActive) {
                        viewModel.loadMoreCocktails()
                    }
                }

                // Detect scroll position to potentially hide UI elements
                var isPastThreshold by remember { mutableStateOf(false) }
                listState.OnScrollPastThreshold(threshold = 1) { past ->
                    isPastThreshold = past
                }

                // Track which items have been loaded for batched animation
                val visibleItemsCount = remember { mutableStateOf(0) }

                // Update visible items based on scroll position
                LaunchedEffect(listState.firstVisibleItemIndex) {
                    // Calculate how many items should be visible based on current scroll position
                    // We add a buffer of 9 items (3 batches of 3) to ensure smooth scrolling
                    val targetVisible = minOf(
                        cocktails.size,
                        listState.firstVisibleItemIndex + 12 // Current visible + 3 batches ahead
                    )

                    // If we need to show more items, increase the count in batches of 3
                    if (targetVisible > visibleItemsCount.value) {
                        // Animate in batches of 3 items
                        val batchSize = 3
                        val currentBatch = visibleItemsCount.value / batchSize
                        val targetBatch = targetVisible / batchSize

                        // For each batch we need to show
                        for (batch in currentBatch until targetBatch) {
                            val newCount = minOf((batch + 1) * batchSize, cocktails.size)
                            visibleItemsCount.value = newCount
                            delay(100) // Small delay between batches for staggered effect
                        }

                        // Make sure we show any remaining items
                        visibleItemsCount.value = targetVisible
                    }
                }

                // Main content with optimized list rendering
                // We're using isFastScrolling to conditionally disable animations during fast scrolling
                // This significantly improves performance when scrolling quickly
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
                ) {
                    // All cocktails list or search results or filtered by category
                    item(key = "header") {
                        Text(
                            text = when {
                                isSearchActive -> "Search Results"
                                selectedCategory != null -> "$selectedCategory Cocktails"
                                else -> "All Cocktails"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Use the full list for the main items with indexed items to ensure unique keys
                    itemsIndexed(
                        items = cocktails,
                        key = { index, cocktail -> "cocktail_${index}_${cocktail.id}" }
                    ) { index, cocktail ->
                        // Only show items that have been loaded in our batched approach
                        val isVisible = index < visibleItemsCount.value

                        // Calculate the animation index based on the batch
                        // This ensures items within a batch animate together
                        val batchSize = 3
                        val batchIndex = index % batchSize

                        // If the item should be visible, show it with animation
                        if (isVisible) {
                            // Calculate animation parameters based on batch
                            val delayMillis = batchIndex * 100
                            val animationDuration = 300

                            // Animate alpha and offset for entry
                            val animatedAlpha by animateFloatAsState(
                                targetValue = 1f,
                                animationSpec = tween(
                                    durationMillis = animationDuration,
                                    delayMillis = delayMillis
                                ),
                                label = "item_alpha_$index"
                            )

                            val animatedOffset by animateFloatAsState(
                                targetValue = 0f,
                                animationSpec = tween(
                                    durationMillis = animationDuration,
                                    delayMillis = delayMillis
                                ),
                                label = "item_offset_$index"
                            )

                            Box(
                                modifier = Modifier
                                    .alpha(animatedAlpha)
                                    .offset(y = animatedOffset.dp)
                            ) {
                                AnimatedCocktailItem(
                                    cocktail = cocktail,
                                    onClick = {
                                        onCocktailClick(cocktail)
                                    },
                                    onAddToCart = {
                                        onAddToCart(it)
                                    },
                                    isFavorite = favorites.any { it.id == cocktail.id },
                                    onToggleFavorite = { cocktailToToggle ->
                                        favoritesViewModel.toggleFavorite(cocktailToToggle)
                                    },
                                    index = batchIndex // Use batch index for staggered animation within batch
                                )
                            }
                        }
                    }

                    // Show loading indicator at the bottom when loading more items
                    if (isLoadingMore) {
                        item(key = "loading_more") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Use a simple fade-in effect for the loading indicator
                                val animatedAlpha by animateFloatAsState(
                                    targetValue = 1f,
                                    animationSpec = tween(durationMillis = 300),
                                    label = "loading_alpha"
                                )

                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .alpha(animatedAlpha),
                                    color = AppColors.Primary
                                )
                            }
                        }
                    }

                    // Show end of list message when no more data
                    if (!hasMoreData && !isSearchActive && cocktails.isNotEmpty()) {
                        item(key = "end_of_list") {
                            // Use a simple animation for the end of list message
                            val animatedOffset by animateFloatAsState(
                                targetValue = 0f,
                                animationSpec = tween(
                                    durationMillis = 500,
                                    delayMillis = 300,
                                    easing = FastOutSlowInEasing
                                ),
                                label = "end_of_list_offset"
                            )

                            val animatedAlpha by animateFloatAsState(
                                targetValue = 1f,
                                animationSpec = tween(
                                    durationMillis = 500,
                                    delayMillis = 300
                                ),
                                label = "end_of_list_alpha"
                            )

                            Text(
                                text = "You've reached the end of the list",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .offset(y = animatedOffset.dp)
                                    .alpha(animatedAlpha),
                                textAlign = TextAlign.Center,
                                color = AppColors.TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
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