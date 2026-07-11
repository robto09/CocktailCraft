package com.cocktailcraft.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.ui.animation.AnimationUtils
import com.cocktailcraft.ui.theme.AppColors

/**
 * An enhanced version of CocktailItem with animations
 */
@Composable
fun AnimatedCocktailItem(
    cocktail: Cocktail,
    onClick: () -> Unit,
    onAddToCart: (Cocktail) -> Unit,
    isFavorite: Boolean = false,
    onToggleFavorite: (Cocktail) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Animation states
    var isHovered by remember { mutableStateOf(false) }

    // Scale animation for hover effect
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.03f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "hover_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                onClick = onClick,
                onClickLabel = "View ${cocktail.name} details"
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Surface
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
            onToggleFavorite = onToggleFavorite
        )
    }
}

/**
 * Extracted content of the cocktail item to avoid duplication
 */
@Composable
fun CocktailItemContent(
    cocktail: Cocktail,
    isFavorite: Boolean,
    onAddToCart: (Cocktail) -> Unit,
    onToggleFavorite: (Cocktail) -> Unit
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
                .background(AppColors.LightGray)
        ) {
            // Use our optimized image component
            OptimizedImage(
                url = cocktail.imageUrl,
                contentDescription = cocktail.name,
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
                        text = "Out of Stock",
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
                color = AppColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Display alcoholic info with category info
            Text(
                text = buildString {
                    append(cocktail.alcoholic ?: "Unknown")
                    cocktail.category?.let {
                        append(" • ")
                        append(it)
                    }
                },
                fontSize = 14.sp,
                color = AppColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Use safe call for ingredients that might be null or empty
            Text(
                text = if (cocktail.ingredients.isNotEmpty()) {
                    // Join first 2 ingredients with safe operators
                    cocktail.ingredients.take(2).joinToString(", ") { it.name }
                        .let { if (cocktail.ingredients.size > 2) "$it..." else it }
                } else {
                    "No ingredients listed"
                },
                fontSize = 12.sp,
                color = AppColors.TextSecondary,
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
                    text = "$${String.format("%.2f", cocktail.price)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AppColors.Primary
                )

                Spacer(modifier = Modifier.weight(1f))

                // Favorite button with animation
                AnimatedIconButton(
                    onClick = { onToggleFavorite(cocktail) },
                    icon = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) AppColors.Secondary else AppColors.Gray
                )

                // Add to cart button with animation
                AnimatedIconButton(
                    onClick = { onAddToCart(cocktail) },
                    icon = Icons.Default.ShoppingCart,
                    contentDescription = "Add to Cart",
                    tint = if (cocktail.stockCount > 0) AppColors.Primary else AppColors.Gray,
                    enabled = cocktail.stockCount > 0
                )
            }
        }
    }
}