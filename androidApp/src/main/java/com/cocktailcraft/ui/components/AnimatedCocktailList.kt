package com.cocktailcraft.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.ui.theme.AppColors
import com.cocktailcraft.util.ListOptimizations.OnBottomReached
import kotlinx.coroutines.delay

/**
 * A reusable component for displaying a list of cocktails with animations.
 *
 * @param cocktails The list of cocktails to display
 * @param isSearchActive Whether search is currently active
 * @param selectedCategory The currently selected category, if any
 * @param isLoadingMore Whether more items are being loaded
 * @param hasMoreData Whether there is more data to load
 * @param favorites The list of favorite cocktails
 * @param onCocktailClick Callback when a cocktail is clicked
 * @param onAddToCart Callback when a cocktail is added to the cart
 * @param onToggleFavorite Callback when a cocktail's favorite status is toggled
 * @param onLoadMore Callback to load more cocktails
 * @param modifier The modifier for the component
 * @param headerFontSize The font size of the header
 * @param itemSpacing The spacing between items
 * @param contentPadding The padding for the content
 * @param loadingIndicatorSize The size of the loading indicator
 * @param endOfListMessageFontSize The font size of the end of list message
 */
@Composable
fun AnimatedCocktailList(
    cocktails: List<Cocktail>,
    isSearchActive: Boolean,
    selectedCategory: String?,
    isLoadingMore: Boolean,
    hasMoreData: Boolean,
    favorites: List<Cocktail>,
    onCocktailClick: (Cocktail) -> Unit,
    onAddToCart: (Cocktail) -> Unit,
    onToggleFavorite: (Cocktail) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    headerFontSize: Int = 18,
    itemSpacing: Int = 16,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    loadingIndicatorSize: Int = 32,
    endOfListMessageFontSize: Int = 14
) {
    // Remember scroll state for optimizations
    val listState = rememberLazyListState()

    // Detect when we're near the bottom to load more items
    listState.OnBottomReached(buffer = 5) {
        // Only load more if not already loading and not searching
        if (!isLoadingMore && !isSearchActive) {
            onLoadMore()
        }
    }

    // Track which items have been loaded for batched animation
    val visibleItemsCount = remember { mutableStateOf(0) }

    // Update visible items based on scroll position
    LaunchedEffect(listState.firstVisibleItemIndex, cocktails.size) {
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
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(itemSpacing.dp),
        contentPadding = contentPadding
    ) {
        // Header item
        item(key = "header") {
            Text(
                text = when {
                    isSearchActive -> "Search Results"
                    selectedCategory != null -> "$selectedCategory Cocktails"
                    else -> "All Cocktails"
                },
                fontSize = headerFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Cocktail items with animations
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
                            onToggleFavorite(cocktailToToggle)
                        }
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
                            .size(loadingIndicatorSize.dp)
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
                    fontSize = endOfListMessageFontSize.sp
                )
            }
        }
    }
}
