package com.cocktailcraft.android.ui.components

import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.android.ui.components.OptimizedImage
import com.cocktailcraft.android.ui.theme.CocktailCraftTheme
import com.cocktailcraft.android.util.rememberHapticHandler
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.add_to_cart_item_a11y
import com.cocktailcraft.resources.list_out_of_stock
import com.cocktailcraft.resources.list_tap_to_view_ingredients
import com.cocktailcraft.resources.list_view_details_label

@Composable
fun CocktailItem(
    cocktail: Cocktail,
    onClick: () -> Unit,
    onAddToCart: (Cocktail) -> Unit,
    isFavorite: Boolean = false,
    onToggleFavorite: (Cocktail) -> Unit = {}
) {
    val hapticHandler = rememberHapticHandler()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                onClickLabel = stringResource(MR.strings.list_view_details_label, cocktail.name)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
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
                // Use our optimized image component (decorative: the name
                // is read from the text beside it)
                OptimizedImage(
                    url = cocktail.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    targetSize = 200 // Target size for better memory usage
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

                // Use safe call for ingredients that might be null or empty
                Text(
                    text = if (cocktail.ingredients.isNotEmpty()) {
                        val firstIngredient = cocktail.ingredients.first().name
                        // Check if it's a placeholder
                        if (firstIngredient == "Tap to view ingredients") {
                            firstIngredient
                        } else {
                            // Join first 2 ingredients with safe operators
                            cocktail.ingredients.take(2).joinToString(", ") { it.name }
                                .let { if (cocktail.ingredients.size > 2) "$it..." else it }
                        }
                    } else {
                        stringResource(MR.strings.list_tap_to_view_ingredients)
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

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
                        }
                    )

                    // Add to cart button
                    IconButton(
                        onClick = { onAddToCart(cocktail) },
                        enabled = cocktail.stockCount > 0
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = stringResource(MR.strings.add_to_cart_item_a11y, cocktail.name),
                            tint = if (cocktail.stockCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}