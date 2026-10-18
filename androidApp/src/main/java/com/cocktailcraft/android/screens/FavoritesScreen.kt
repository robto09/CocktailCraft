package com.cocktailcraft.android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocktailcraft.android.ui.components.CocktailItem
import com.cocktailcraft.android.ui.components.CocktailLoadingShimmer
import com.cocktailcraft.android.ui.components.EmptyStateComponent
import com.cocktailcraft.android.ui.components.ErrorStateView
import com.cocktailcraft.android.ui.components.ErrorToastEffect
import com.cocktailcraft.android.util.refreshAccessibilityAction
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.refresh
import com.cocktailcraft.resources.browse_cocktails
import com.cocktailcraft.resources.favorites_empty_message
import com.cocktailcraft.resources.no_favorites
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.viewmodel.SharedFavoritesViewModel
import com.cocktailcraft.viewmodel.state.FavoritesUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    favoritesViewModel: SharedFavoritesViewModel,
    onBrowseProducts: () -> Unit,
    onAddToCart: (Cocktail) -> Unit,
    onCocktailClick: (Cocktail) -> Unit
) {
    val state by favoritesViewModel.uiState.collectAsStateWithLifecycle()
    val error by favoritesViewModel.error.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // With favorites on screen a failure (e.g. a toggle) is a toast; with
    // none it stays for the full-screen error state
    ErrorToastEffect(
        error = error.takeIf { state.favorites.isNotEmpty() },
        onConsumed = { favoritesViewModel.clearError() }
    )

    FavoritesContent(
        state = state,
        error = error,
        onRefresh = { favoritesViewModel.refresh() },
        onBrowseProducts = onBrowseProducts,
        onAddToCart = onAddToCart,
        // Every heart here is filled: a tap unfavorites, with an Undo toast
        // (shown by MainScreen, which collects the favorites' events)
        onToggleFavorite = { cocktail -> scope.launch { favoritesViewModel.removeFavorite(cocktail) } },
        onCocktailClick = onCocktailClick
    )
}

/**
 * Stateless favorites content: renders [state] and reports every intent
 * upward, so it can be previewed/screenshot-tested without a ViewModel.
 * [error] is rendered full-screen only when there are no favorites to show.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FavoritesContent(
    state: FavoritesUiState,
    error: ErrorHandler.UserFriendlyError?,
    onRefresh: () -> Unit,
    onBrowseProducts: () -> Unit,
    onAddToCart: (Cocktail) -> Unit,
    onToggleFavorite: (Cocktail) -> Unit,
    onCocktailClick: (Cocktail) -> Unit = {}
) {
    val isLoading = state.isLoading
    val favorites = state.favorites

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Skeleton cards while favorites load; the list has no section title
        if (isLoading) {
            CocktailLoadingShimmer(showHeader = false)
        }

        // Nothing to show but the failure: full-screen error with Retry
        if (!isLoading && error != null && favorites.isEmpty()) {
            ErrorStateView(error = error, onRetry = onRefresh)
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
                        containerColor = MaterialTheme.colorScheme.surface,
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
                // Keyed by id, so unfavoriting a card doesn't rebind the
                // following rows to the wrong cocktail
                items(favorites, key = { it.id }) { cocktail ->
                    CocktailItem(
                        cocktail = cocktail,
                        onClick = { onCocktailClick(cocktail) },
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
