package com.cocktailcraft.android.ui.components

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.android.ui.theme.LocalReducedMotion
import com.cocktailcraft.android.util.ListOptimizations.OnBottomReached
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.android.util.toFavoriteIdSet
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.list_all_cocktails
import com.cocktailcraft.resources.list_category_cocktails
import com.cocktailcraft.resources.list_end_of_list_message
import com.cocktailcraft.resources.list_search_results
import com.cocktailcraft.viewmodel.state.CocktailRatingSummary

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
 * @param endOfListMessageFontSize The font size of the end of list message
 * @param ratingSummaries The user's review averages by cocktail id, shown on the cards
 * @param leadingContent Shown above the header, scrolling with the list (Home's "Recently viewed" row)
 */
@Composable
fun AnimatedCocktailList(
    cocktails: List<Cocktail>,
    isSearchActive: Boolean,
    selectedCategory: String?,
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
    endOfListMessageFontSize: Int = 14,
    ratingSummaries: Map<String, CocktailRatingSummary> = emptyMap(),
    leadingContent: (@Composable () -> Unit)? = null
) {
    // animateItem()'s default springs, or none under Reduce Motion
    val reducedMotion = LocalReducedMotion.current
    val fadeSpec: FiniteAnimationSpec<Float>? =
        if (reducedMotion) null else spring(stiffness = Spring.StiffnessMediumLow)
    val placementSpec: FiniteAnimationSpec<IntOffset>? = if (reducedMotion) {
        null
    } else {
        spring(
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = IntOffset.VisibilityThreshold
        )
    }

    // Identity-keyed items require unique ids; guard the unique-key contract
    // against overlapping pages from the API
    val distinctCocktails = remember(cocktails) { cocktails.distinctBy { it.id } }

    // O(1) membership per row instead of favorites.any {} per visible item
    val favoriteIds = remember(favorites) { favorites.toFavoriteIdSet() }

    // A category switch replaces every row: animateItem() would fade the old
    // rows out on top of the new ones fading in, a double-exposed flash. Start
    // a fresh list for a replacement instead (which also scrolls back to the
    // top); reorders and appended pages share rows and keep their animations.
    val listGeneration = rememberListGeneration(distinctCocktails)

    key(listGeneration) {
        // Remember scroll state for optimizations
        val listState = rememberLazyListState()

        // Detect when we're near the bottom to load more items
        listState.OnBottomReached(buffer = 5) {
            // Search results are complete; only the category listing pages
            if (!isSearchActive) {
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
            if (leadingContent != null) {
                item(key = "leading") { leadingContent() }
            }

            // Header item
            item(key = "header") {
                Text(
                    text = when {
                        isSearchActive -> stringResource(MR.strings.list_search_results)
                        selectedCategory != null -> stringResource(MR.strings.list_category_cocktails, selectedCategory)
                        else -> stringResource(MR.strings.list_all_cocktails)
                    },
                    fontSize = headerFontSize.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .semantics { heading() }
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
                        .animateItem(fadeInSpec = fadeSpec, placementSpec = placementSpec, fadeOutSpec = fadeSpec)
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
                        },
                        reviewRating = ratingSummaries[cocktail.id]
                    )
                }
            }

            // Show end of list message when no more data
            if (!hasMoreData && !isSearchActive && cocktails.isNotEmpty()) {
                item(key = "end_of_list") {
                    Text(
                        text = stringResource(MR.strings.list_end_of_list_message),
                        modifier = Modifier
                            .animateItem(fadeInSpec = fadeSpec, placementSpec = placementSpec, fadeOutSpec = fadeSpec)
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = endOfListMessageFontSize.sp
                    )
                }
            }
        }
    }
}

/**
 * Bumps each time [cocktails] shares no cocktail with the previous list, so
 * the caller can treat a wholesale replacement as a new list.
 */
@Composable
private fun rememberListGeneration(cocktails: List<Cocktail>): Int {
    val tracker = remember { ListGenerationTracker() }
    return remember(cocktails) { tracker.update(cocktails) }
}

private class ListGenerationTracker {
    private var ids: Set<String> = emptySet()
    private var generation = 0

    fun update(cocktails: List<Cocktail>): Int {
        val newIds = cocktails.mapTo(HashSet()) { it.id }
        if (ids.isNotEmpty() && newIds.isNotEmpty() && ids.none { it in newIds }) generation++
        ids = newIds
        return generation
    }
}
