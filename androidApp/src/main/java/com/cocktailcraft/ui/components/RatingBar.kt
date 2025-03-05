package com.cocktailcraft.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cocktailcraft.ui.theme.AppColors

/**
 * A reusable rating bar component that displays a row of stars representing a rating.
 * 
 * @param rating The rating value to display (0.0 to max number of stars)
 * @param modifier Optional Modifier for the component
 * @param stars Number of total stars to display
 * @param starsColor Color for the stars
 * @param starSize Size of each star
 * @param spaceBetween Space between stars
 * @param useHalfStars Whether to use half-star icons for more precise display
 */
@Composable
fun RatingBar(
    rating: Float,
    modifier: Modifier = Modifier,
    stars: Int = 5,
    starsColor: Color = AppColors.Secondary,
    starSize: Dp = 16.dp,
    spaceBetween: Dp = 2.dp,
    useHalfStars: Boolean = false
) {
    Row(modifier = modifier) {
        if (useHalfStars) {
            // Implementation with half stars for more precise display
            repeat(stars) { index ->
                val starValue = index + 1
                val starIcon = when {
                    starValue <= rating -> Icons.Filled.Star
                    starValue - 0.5f <= rating -> Icons.Filled.StarHalf
                    else -> Icons.Filled.StarOutline
                }
                
                Icon(
                    imageVector = starIcon,
                    contentDescription = null,
                    tint = starsColor,
                    modifier = Modifier.size(starSize)
                )
                
                if (index < stars - 1) {
                    Spacer(modifier = Modifier.width(spaceBetween))
                }
            }
        } else {
            // Original implementation with alpha for partial stars
            repeat(stars) { index ->
                val starAlpha = when {
                    index < rating.toInt() -> 1f
                    index == rating.toInt() && rating % 1 != 0f -> rating % 1
                    else -> 0.3f
                }
                
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = starsColor.copy(alpha = starAlpha),
                    modifier = Modifier.size(starSize)
                )
                
                if (index < stars - 1) {
                    Spacer(modifier = Modifier.width(spaceBetween))
                }
            }
        }
    }
} 