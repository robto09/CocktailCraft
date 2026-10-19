package com.cocktailcraft.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.android.ui.theme.CocktailCraftTheme
import com.cocktailcraft.android.util.rememberHapticHandler
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.add_to_cart_item_a11y
import com.cocktailcraft.resources.list_out_of_stock
import com.cocktailcraft.resources.list_tap_to_view_ingredients
import com.cocktailcraft.resources.list_view_details_label
import com.cocktailcraft.viewmodel.state.CocktailRatingSummary

/**
 * An enhanced version of CocktailItem with animations
 */
@Composable
fun AnimatedCocktailItem(
    cocktail: Cocktail,
    onClick: () -> Unit,
    onAddToCart: (Cocktail) -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onToggleFavorite: (Cocktail) -> Unit = {},
    reviewRating: CocktailRatingSummary? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                onClickLabel = stringResource(MR.strings.list_view_details_label, cocktail.name)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp,
            hoveredElevation = 3.dp
        )
    ) {
        CocktailItemContent(
            cocktail = cocktail,
            isFavorite = isFavorite,
            onAddToCart = onAddToCart,
            onToggleFavorite = onToggleFavorite,
            reviewRating = reviewRating
        )
    }
}

/**
 * Extracted content of the cocktail item to avoid duplication.
 * [reviewRating], when the user has reviewed the cocktail, shows their
 * average under the ingredients (the API itself has no ratings to show).
 */
@Composable
fun CocktailItemContent(
    cocktail: Cocktail,
    isFavorite: Boolean,
    onAddToCart: (Cocktail) -> Unit,
    onToggleFavorite: (Cocktail) -> Unit,
    reviewRating: CocktailRatingSummary? = null
) {
    // Haptic feedback handler for favorite toggle
    val hapticHandler = rememberHapticHandler()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cocktail image with placeholder
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CocktailCraftTheme.extendedColors.neutralSubtle)
        ) {
            // Use our optimized image component (decorative: the name is
            // read from the text beside it)
            OptimizedImage(
                url = cocktail.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                targetSize = 200, // Target size for better memory usage
                showLoadingIndicator = false // Disable loading indicator for better performance
            )

            // Stock badge for out of stock items
            if (cocktail.stockCount <= 0) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(MR.strings.list_out_of_stock),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Cocktail details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = cocktail.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // "Alcoholic • Cocktail"; an alcoholic value the list endpoint
            // didn't supply is left out rather than shown as "Unknown"
            Text(
                text = listOfNotNull(cocktail.knownAlcoholic, cocktail.category).joinToString(" • "),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // The first two ingredients; a partial record (from a list
            // endpoint) has none yet, so point at the detail screen instead
            Text(
                text = if (cocktail.hasFullDetails && cocktail.ingredients.isNotEmpty()) {
                    cocktail.ingredients.take(2).joinToString(", ") { it.name }
                        .let { if (cocktail.ingredients.size > 2) "$it..." else it }
                } else {
                    stringResource(MR.strings.list_tap_to_view_ingredients)
                },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = FontStyle.Italic,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (reviewRating != null) {
                Spacer(modifier = Modifier.height(4.dp))
                RatingDisplay(
                    rating = reviewRating.rating,
                    reviewCount = reviewRating.count,
                    starSize = 14,
                    ratingTextSize = 12,
                    reviewCountTextSize = 12
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Price
                Text(
                    text = formatPrice(cocktail.price),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.weight(1f))

                // Favorite toggle with haptic feedback
                FavoriteToggleButton(
                    isFavorite = isFavorite,
                    onToggle = {
                        hapticHandler.performToggleFavorite(isFavorite)
                        onToggleFavorite(cocktail)
                    },
                    iconSize = 24.dp
                )

                // Add to cart button with animation
                AnimatedIconButton(
                    onClick = { onAddToCart(cocktail) },
                    icon = Icons.Default.ShoppingCart,
                    contentDescription = stringResource(MR.strings.add_to_cart_item_a11y, cocktail.name),
                    tint = if (cocktail.stockCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    enabled = cocktail.stockCount > 0
                )
            }
        }
    }
}