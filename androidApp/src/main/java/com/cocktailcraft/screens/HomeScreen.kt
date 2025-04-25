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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.cocktailcraft.navigation.Screen
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.ui.components.AnimatedCocktailItem
import com.cocktailcraft.ui.components.CocktailItem
import com.cocktailcraft.ui.components.CocktailItemShimmer
import com.cocktailcraft.ui.components.shimmerEffect
import com.cocktailcraft.ui.components.ErrorBanner
import com.cocktailcraft.ui.components.ErrorDialog
import com.cocktailcraft.ui.components.FilterChip
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

    // State for error dialog
    var showErrorDialog by remember { mutableStateOf(false) }

    // Add state for selected category
    var selectedCategory by remember { mutableStateOf<String?>(null) }

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

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchCocktails(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
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
                            selectedCategory = if (category == "All") null else category
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
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isSearchActive)
                            "No cocktails found matching \"$searchQuery\""
                        else if (selectedCategory != null)
                            "No cocktails found in category \"$selectedCategory\""
                        else
                            "No cocktails found",
                        color = AppColors.TextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
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

                // Main content with optimized list rendering
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
                            index = index // Pass index for staggered animation
                        )
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