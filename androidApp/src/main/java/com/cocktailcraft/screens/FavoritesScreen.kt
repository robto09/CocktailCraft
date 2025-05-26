package com.cocktailcraft.screens

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.ui.components.CocktailItem
import com.cocktailcraft.ui.components.EmptyStateComponent
import com.cocktailcraft.ui.components.LoadingStateComponent
import com.cocktailcraft.ui.components.SectionHeader
import com.cocktailcraft.ui.theme.AppColors
import com.cocktailcraft.viewmodel.CartViewModel
import com.cocktailcraft.viewmodel.FavoritesViewModel

@Composable
fun FavoritesScreen(
    cartViewModel: CartViewModel,
    favoritesViewModel: FavoritesViewModel,
    onBrowseProducts: () -> Unit,
    onAddToCart: (Cocktail) -> Unit
) {
    val favorites by favoritesViewModel.favorites.collectAsState()
    val isLoading by favoritesViewModel.isLoading.collectAsState()
    val error by favoritesViewModel.error.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        // Show loading state
        LoadingStateComponent(isLoading = isLoading)

        // Show error state
        if (!isLoading && error?.isNotEmpty() == true) {
            EmptyStateComponent(
                title = "Error",
                message = error ?: "An unknown error occurred",
                actionButtonText = "Try Again",
                onActionButtonClick = { /* Add retry logic here */ }
            )
        }
        // Show empty favorites state
        else if (!isLoading && favorites.isEmpty()) {
            EmptyStateComponent(
                title = "No favorites yet",
                message = "Add cocktails to your favorites to see them here",
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
                        title = "Your Favorite Cocktails",
                        fontSize = 20,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                itemsIndexed(favorites) { _, cocktail ->
                    CocktailItem(
                        cocktail = cocktail,
                        onClick = { /* Navigate to detail */ },
                        onAddToCart = { _ ->
                            cartViewModel.addToCart(cocktail)
                            onAddToCart(cocktail)
                        },
                        isFavorite = true,
                        onToggleFavorite = { _ ->
                            favoritesViewModel.toggleFavorite(cocktail)
                        }
                    )
                }
            }
        }
    }
}