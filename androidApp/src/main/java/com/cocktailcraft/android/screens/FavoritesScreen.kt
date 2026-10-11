package com.cocktailcraft.android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocktailcraft.android.ui.components.CocktailItem
import com.cocktailcraft.android.ui.components.EmptyStateComponent
import com.cocktailcraft.android.ui.components.LoadingStateComponent
import com.cocktailcraft.android.ui.components.SectionHeader
import com.cocktailcraft.android.util.refreshAccessibilityAction
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.refresh
import com.cocktailcraft.resources.browse_cocktails
import com.cocktailcraft.resources.cart_try_again
import com.cocktailcraft.resources.error
import com.cocktailcraft.resources.favorites_empty_message
import com.cocktailcraft.resources.favorites_section_title
import com.cocktailcraft.resources.no_favorites
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.viewmodel.SharedCartViewModel
import com.cocktailcraft.viewmodel.SharedFavoritesViewModel
import com.cocktailcraft.viewmodel.state.FavoritesUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    cartViewModel: SharedCartViewModel,
    favoritesViewModel: SharedFavoritesViewModel,
    onBrowseProducts: () -> Unit,
    onAddToCart: (Cocktail) -> Unit
) {
    val state by favoritesViewModel.uiState.collectAsStateWithLifecycle()
    val error by favoritesViewModel.error.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    FavoritesContent(
        state = state,
        error = error,
        onRefresh = { favoritesViewModel.refresh() },
        onBrowseProducts = onBrowseProducts,
        onAddToCart = onAddToCart,
        onToggleFavorite = { cocktail -> scope.launch { favoritesViewModel.toggleFavorite(cocktail) } }
    )
}

/**
 * Stateless favorites content: renders [state] and reports every intent
 * upward, so it can be previewed/screenshot-tested without a ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FavoritesContent(
    state: FavoritesUiState,
    error: ErrorHandler.UserFriendlyError?,
    onRefresh: () -> Unit,
    onBrowseProducts: () -> Unit,
    onAddToCart: (Cocktail) -> Unit,
    onToggleFavorite: (Cocktail) -> Unit
) {
    val isLoading = state.isLoading
    val favorites = state.favorites

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Show loading state
        LoadingStateComponent(isLoading = isLoading)

        // Show error state
        if (!isLoading && error != null) {
            EmptyStateComponent(
                title = stringResource(MR.strings.error),
                message = error.toString(),
                actionButtonText = stringResource(MR.strings.cart_try_again),
                onActionButtonClick = { /* Add retry logic here */ }
            )
        }
        // Show empty favorites state
        else if (!isLoading && favorites.isEmpty()) {
            EmptyStateComponent(
                title = stringResource(MR.strings.no_favorites),
                message = stringResource(MR.strings.favorites_empty_message),
                actionButtonText = stringResource(MR.strings.browse_cocktails),
                onActionButtonClick = onBrowseProducts,
                icon = Icons.Filled.Favorite
            )
        }
        // Show favorites list (pull-to-refresh, parity with iOS Favorites)
        else if (!isLoading) {
            val pullToRefreshState = rememberPullToRefreshState()
            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .refreshAccessibilityAction(stringResource(MR.strings.refresh), onRefresh),
                state = pullToRefreshState,
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = pullToRefreshState,
                        isRefreshing = isLoading,
                        modifier = Modifier.align(Alignment.TopCenter),
                        containerColor = Color.White,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SectionHeader(
                        title = stringResource(MR.strings.favorites_section_title),
                        fontSize = 20,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                itemsIndexed(favorites) { _, cocktail ->
                    CocktailItem(
                        cocktail = cocktail,
                        onClick = { /* Navigate to detail */ },
                        // onAddToCart both mutates the cart and confirms via
                        // snackbar (MainScreen); the extra local addToCart here
                        // was double-adding every item.
                        onAddToCart = { _ ->
                            onAddToCart(cocktail)
                        },
                        isFavorite = true,
                        onToggleFavorite = { _ ->
                            onToggleFavorite(cocktail)
                        }
                    )
                }
            }
            }
        }
    }
}