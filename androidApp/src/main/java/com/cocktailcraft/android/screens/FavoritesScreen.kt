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
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.android.R
import com.cocktailcraft.android.ui.components.CocktailItem
import com.cocktailcraft.android.ui.components.EmptyStateComponent
import com.cocktailcraft.android.ui.components.LoadingStateComponent
import com.cocktailcraft.android.ui.components.SectionHeader
import com.cocktailcraft.android.ui.theme.AppColors
import com.cocktailcraft.viewmodel.SharedCartViewModel
import com.cocktailcraft.viewmodel.SharedFavoritesViewModel
import kotlinx.coroutines.launch

@Composable
fun FavoritesScreen(
    cartViewModel: SharedCartViewModel,
    favoritesViewModel: SharedFavoritesViewModel,
    onBrowseProducts: () -> Unit,
    onAddToCart: (Cocktail) -> Unit
) {
    val state by favoritesViewModel.uiState.collectAsStateWithLifecycle()
    val isLoading = state.isLoading
    val favorites = state.favorites
    val error by favoritesViewModel.error.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        // Show loading state
        LoadingStateComponent(isLoading = isLoading)

        // Show error state
        if (!isLoading && error != null) {
            EmptyStateComponent(
                title = stringResource(R.string.error),
                message = error?.toString() ?: "An unknown error occurred",
                actionButtonText = "Try Again",
                onActionButtonClick = { /* Add retry logic here */ }
            )
        }
        // Show empty favorites state
        else if (!isLoading && favorites.isEmpty()) {
            EmptyStateComponent(
                title = stringResource(R.string.no_favorites),
                message = stringResource(R.string.favorites_empty_message),
                actionButtonText = "Browse Cocktails",
                onActionButtonClick = onBrowseProducts,
                icon = Icons.Filled.Favorite
            )
        }
        // Show favorites list
        else if (!isLoading) {
            // Favorites list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SectionHeader(
                        title = stringResource(R.string.favorites_section_title),
                        fontSize = 20,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                itemsIndexed(favorites) { _, cocktail ->
                    CocktailItem(
                        cocktail = cocktail,
                        onClick = { /* Navigate to detail */ },
                        onAddToCart = { _ ->
                            scope.launch { cartViewModel.addToCart(cocktail) }
                            onAddToCart(cocktail)
                        },
                        isFavorite = true,
                        onToggleFavorite = { _ ->
                            scope.launch { favoritesViewModel.toggleFavorite(cocktail) }
                        }
                    )
                }
            }
        }
    }
}