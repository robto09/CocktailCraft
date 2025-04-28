package com.cocktailcraft.ui.components

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
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.ui.theme.AppColors
import com.cocktailcraft.ui.components.LightweightOptimizedImage
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
 * @param backgroundColor Background color for the card (default: Color.White)
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
    backgroundColor: Color = Color.White,
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
                        color = AppColors.TextPrimary,
                        modifier = Modifier.weight(1f),
                        maxLines = maxLines,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Favorites button (optional)
                    if (showFavoriteButton) {
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = if (isFavorite) AppColors.Secondary else AppColors.Gray,
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
                    color = AppColors.Primary,
                    fontSize = 14.sp
                )

                // Optional alcoholic status
                item.cocktail.alcoholic?.let { alcoholicStatus ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = alcoholicStatus,
                        fontSize = 12.sp,
                        color = AppColors.TextSecondary
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
                            IconButton(
                                onClick = onDecreaseQuantity,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(AppColors.LightGray, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease",
                                    tint = AppColors.TextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = item.quantity.toString(),
                                modifier = Modifier.padding(horizontal = 16.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            IconButton(
                                onClick = onIncreaseQuantity,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(AppColors.Primary, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        // If quantity controls are not shown, at least show the quantity
                        Text(
                            text = "Quantity: ${item.quantity}",
                            fontSize = 14.sp,
                            color = AppColors.TextSecondary
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
                            color = AppColors.TextPrimary
                        )

                        // Delete button (optional)
                        if (showDeleteButton) {
                            IconButton(
                                onClick = onRemove,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove",
                                    tint = AppColors.Error,
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