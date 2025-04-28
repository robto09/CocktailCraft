package com.cocktailcraft.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.ui.theme.AppColors

/**
 * A reusable rating display component that shows a rating with stars and review count.
 *
 * @param rating The rating value to display
 * @param reviewCount The number of reviews
 * @param modifier The modifier for the component
 * @param showReviewCount Whether to show the review count
 * @param starSize The size of each star
 * @param ratingTextSize The font size of the rating text
 * @param reviewCountTextSize The font size of the review count text
 * @param starsColor The color of the stars
 * @param textColor The color of the text
 * @param useHalfStars Whether to use half-star icons for more precise display
 */
@Composable
fun RatingDisplay(
    rating: Float,
    reviewCount: Int,
    modifier: Modifier = Modifier,
    showReviewCount: Boolean = true,
    starSize: Int = 16,
    ratingTextSize: Int = 14,
    reviewCountTextSize: Int = 14,
    starsColor: androidx.compose.ui.graphics.Color = AppColors.Secondary,
    textColor: androidx.compose.ui.graphics.Color = AppColors.TextSecondary,
    useHalfStars: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Display the rating value
        Text(
            text = String.format("%.1f", rating),
            fontSize = ratingTextSize.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        // Display the stars
        RatingBar(
            rating = rating,
            starSize = starSize.dp,
            starsColor = starsColor,
            useHalfStars = useHalfStars
        )
        
        // Display the review count if requested
        if (showReviewCount && reviewCount > 0) {
            Spacer(modifier = Modifier.width(4.dp))
            
            Text(
                text = "($reviewCount ${if (reviewCount == 1) "review" else "reviews"})",
                fontSize = reviewCountTextSize.sp,
                color = textColor
            )
        }
    }
}
