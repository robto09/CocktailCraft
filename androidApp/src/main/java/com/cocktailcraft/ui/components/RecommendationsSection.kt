package com.cocktailcraft.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.ui.theme.AppColors
import com.cocktailcraft.viewmodel.CocktailDetailViewModel

/**
 * A section that displays cocktail recommendations.
 */
@Composable
fun RecommendationsSection(
    viewModel: CocktailDetailViewModel,
    onCocktailClick: (Cocktail) -> Unit,
    modifier: Modifier = Modifier
) {
    val recommendations by viewModel.recommendations.collectAsState()
    val isLoading by viewModel.isLoadingRecommendations.collectAsState()
    
    // Only show the section if we have recommendations or are loading
    if (recommendations.isEmpty() && !isLoading) return
    
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(durationMillis = 500)) +
                slideInHorizontally(
                    animationSpec = tween(durationMillis = 500),
                    initialOffsetX = { it / 2 }
                )
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            // Section title
            Text(
                text = "You might also like",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isLoading) {
                // Show loading state
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(3) {
                        RecommendationItemShimmer()
                    }
                }
            } else {
                // Show recommendations
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(recommendations) { index, cocktail ->
                        RecommendationItem(
                            cocktail = cocktail,
                            onClick = { onCocktailClick(cocktail) },
                            index = index
                        )
                    }
                }
            }
        }
    }
}

/**
 * A card displaying a recommended cocktail.
 */
@Composable
fun RecommendationItem(
    cocktail: Cocktail,
    onClick: () -> Unit,
    index: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column {
            // Cocktail image
            Box(
                modifier = Modifier
                    .height(140.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(AppColors.LightGray)
            ) {
                // Use our optimized image component
                OptimizedImage(
                    url = cocktail.imageUrl,
                    contentDescription = cocktail.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    targetSize = 200,
                    showLoadingIndicator = false
                )
                
                // Show alcoholic badge if applicable
                if (cocktail.alcoholic == "Alcoholic") {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(AppColors.Primary.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalBar,
                            contentDescription = "Alcoholic",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            // Cocktail details
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                // Cocktail name
                Text(
                    text = cocktail.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = AppColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Cocktail category
                cocktail.category?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Price
                Text(
                    text = "$${String.format("%.2f", cocktail.price)}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = AppColors.Primary
                )
            }
        }
    }
}

/**
 * A shimmer loading placeholder for recommendation items.
 */
@Composable
fun RecommendationItemShimmer(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(140.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column {
            // Image placeholder
            Box(
                modifier = Modifier
                    .height(140.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .shimmerEffect()
            )
            
            // Content placeholders
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                // Title placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Category placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Price placeholder
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
            }
        }
    }
}
