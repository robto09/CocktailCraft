package com.cocktailcraft.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.android.ui.components.LightweightOptimizedImage
import com.cocktailcraft.android.ui.theme.CocktailCraftTheme
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.cart_decrease_quantity
import com.cocktailcraft.resources.cart_increase_quantity
import com.cocktailcraft.resources.cart_quantity_label
import com.cocktailcraft.resources.list_add_to_favorites
import com.cocktailcraft.resources.list_remove_from_favorites
import com.cocktailcraft.resources.remove_from_cart
import java.text.NumberFormat
import java.util.Locale

/**
 * A reusable card component to display items in the shopping cart.
 *
 * @param item The cart item to display
 * @param onIncreaseQuantity Callback when the user increases the quantity
 * @param onDecreaseQuantity Callback when the user decreases the quantity
 * @param onRemove Callback when the user removes the item from cart
 * @param isFavorite Whether the item is in favorites
 * @param onToggleFavorite Callback when the user toggles favorite status
 * @param showFavoriteButton Whether to show the favorite button (default: true)
 * @param showDeleteButton Whether to show the delete button (default: true)
 * @param showQuantityControls Whether to show the quantity controls (default: true)
 * @param cardElevation Elevation for the card (default: 1.dp)
 * @param backgroundColor Background color for the card (default: theme surface)
 * @param cornerRadius Corner radius for the card (default: 12.dp)
 * @param imageSize Size of the product image (default: 80.dp)
 * @param maxLines Maximum lines for the product name (default: 1)
 * @param onClick Optional callback when the user clicks on the card (default: null)
 */
@Composable
fun CartItemCard(
    item: CocktailCartItem,
    onIncreaseQuantity: () -> Unit,
    onDecreaseQuantity: () -> Unit,
    onRemove: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    showFavoriteButton: Boolean = true,
    showDeleteButton: Boolean = true,
    showQuantityControls: Boolean = true,
    cardElevation: Dp = 1.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    cornerRadius: Dp = 12.dp,
    imageSize: Dp = 80.dp,
    maxLines: Int = 1,
    onClick: (() -> Unit)? = null
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image - using lightweight optimized image
            LightweightOptimizedImage(
                url = item.cocktail.imageUrl,
                contentDescription = item.cocktail.name,
                modifier = Modifier
                    .size(imageSize)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                targetSize = 150 // Target size for better memory usage
            )

            // Product Details and Controls
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                // Product Name and Favorite Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.cocktail.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = maxLines,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Favorites button (optional) — default IconButton size
                    // keeps the 48 dp minimum touch target
                    if (showFavoriteButton) {
                        IconButton(
                            onClick = onToggleFavorite
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (isFavorite) stringResource(MR.strings.list_remove_from_favorites) else stringResource(MR.strings.list_add_to_favorites),
                                tint = if (isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Unit Price
                Text(
                    text = currencyFormatter.format(item.cocktail.price),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )

                // Optional alcoholic status
                item.cocktail.alcoholic?.let { alcoholicStatus ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = alcoholicStatus,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Price & Quantity Controls Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Quantity Controls (optional)
                    if (showQuantityControls) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // The visible 36 dp circle lives on an inner Box so
                            // the IconButton keeps its full 48 dp touch target
                            IconButton(onClick = onDecreaseQuantity) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(CocktailCraftTheme.extendedColors.neutralSubtle, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = stringResource(MR.strings.cart_decrease_quantity),
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Text(
                                text = item.quantity.toString(),
                                modifier = Modifier.padding(horizontal = 16.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            IconButton(onClick = onIncreaseQuantity) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = stringResource(MR.strings.cart_increase_quantity),
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // If quantity controls are not shown, at least show the quantity
                        Text(
                            text = stringResource(MR.strings.cart_quantity_label, item.quantity),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Total Price and Remove
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currencyFormatter.format(item.cocktail.price * item.quantity),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Delete button (optional)
                        if (showDeleteButton) {
                            IconButton(
                                onClick = onRemove,
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(MR.strings.remove_from_cart),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}