package com.cocktailcraft.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.android.R
import com.cocktailcraft.android.ui.theme.AppColors
import com.cocktailcraft.android.util.toFavoriteIdSet
import com.cocktailcraft.android.util.ListOptimizations.OnBottomReached

/**
 * A reusable component for displaying a list of cocktails with animations.
 *
 * Item entrance/exit/placement animation is delegated to
 * [androidx.compose.foundation.lazy.LazyItemScope.animateItem] — the previous
 * hand-rolled batching system never produced a visible animation (its
 * animateFloatAsState calls initialized at their target values) and its
 * stagger counter jammed after the first list swap.
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

    // Identity-keyed items require unique ids; guard the unique-key contract
    // against overlapping pages from the API
    val distinctCocktails = remember(cocktails) { cocktails.distinctBy { it.id } }

    // O(1) membership per row instead of favorites.any {} per visible item (AN-3)
    val favoriteIds = remember(favorites) { favorites.toFavoriteIdSet() }

    // Detect when we're near the bottom to load more items
    listState.OnBottomReached(buffer = 5) {
        // Only load more if not already loading and not searching
        if (!isLoadingMore && !isSearchActive) {
            onLoadMore()
        }
    }

    // Main content with optimized list rendering
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .testTag("home_cocktail_list"),
        verticalArrangement = Arrangement.spacedBy(itemSpacing.dp),
        contentPadding = contentPadding
    ) {
        // Header item
        item(key = "header") {
            Text(
                text = when {
                    isSearchActive -> stringResource(R.string.list_search_results)
                    selectedCategory != null -> stringResource(R.string.list_category_cocktails, selectedCategory)
                    else -> stringResource(R.string.list_all_cocktails)
                },
                fontSize = headerFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Cocktail items — animateItem() fades new items in and animates
        // position changes when the list shifts. Keys must carry only the
        // cocktail's identity: embedding the index would rename every
        // surviving key on any shift and turn moves into remove+insert
        // (no placement animation, all item state discarded).
        itemsIndexed(
            items = distinctCocktails,
            key = { _, cocktail -> "cocktail_${cocktail.id}" }
        ) { _, cocktail ->
            Box(
                modifier = Modifier
                    .animateItem()
                    // Shared tag on every row: UiAutomator By.res matching only
                    // needs "a" list item, not a specific one.
                    .testTag("cocktail_list_item")
            ) {
                AnimatedCocktailItem(
                    cocktail = cocktail,
                    onClick = {
                        onCocktailClick(cocktail)
                    },
                    onAddToCart = {
                        onAddToCart(it)
                    },
                    isFavorite = cocktail.id in favoriteIds,
                    onToggleFavorite = { cocktailToToggle ->
                        onToggleFavorite(cocktailToToggle)
                    }
                )
            }
        }

        // Show loading indicator at the bottom when loading more items
        if (isLoadingMore) {
            item(key = "loading_more") {
                Box(
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(loadingIndicatorSize.dp),
                        color = AppColors.Primary
                    )
                }
            }
        }

        // Show end of list message when no more data
        if (!hasMoreData && !isSearchActive && cocktails.isNotEmpty()) {
            item(key = "end_of_list") {
                Text(
                    text = stringResource(R.string.list_end_of_list_message),
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center,
                    color = AppColors.TextSecondary,
                    fontSize = endOfListMessageFontSize.sp
                )
            }
        }
    }
}
