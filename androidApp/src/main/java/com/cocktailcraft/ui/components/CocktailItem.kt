package com.cocktailcraft.ui.components

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
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.ui.theme.AppColors

@Composable
fun CocktailItem(
    cocktail: Cocktail,
    onClick: () -> Unit,
    onAddToCart: (Cocktail) -> Unit,
    isFavorite: Boolean = false,
    onToggleFavorite: (Cocktail) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Surface
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
                    .background(AppColors.LightGray)
            ) {
                // Use SubcomposeAsyncImage for better error handling
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(cocktail.imageUrl ?: "")
                        .crossfade(true)
                        .placeholder(ColorDrawable(AppColors.LightGray.toArgb()))
                        .error(ColorDrawable(AppColors.LightGray.toArgb()))
                        .fallback(ColorDrawable(AppColors.LightGray.toArgb()))
                        .build(),
                    contentDescription = cocktail.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = AppColors.Primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalBar,
                                contentDescription = "Image not available",
                                tint = AppColors.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
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
                
                // Category chip - updated to match the app's primary color
                cocktail.category?.let { category ->
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        Surface(
                            color = AppColors.Primary.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = category,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
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
                
                // Using safe operator for alcoholic field that might be null
                Text(
                    text = cocktail.alcoholic ?: "Unknown",
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
                    
                    // Favorite button with actual functionality
                    IconButton(
                        onClick = { onToggleFavorite(cocktail) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) AppColors.Secondary else AppColors.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // Add to cart button
                    IconButton(
                        onClick = { onAddToCart(cocktail) },
                        modifier = Modifier.size(32.dp),
                        enabled = cocktail.stockCount > 0
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Add to Cart",
                            tint = if (cocktail.stockCount > 0) AppColors.Primary else AppColors.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
} 