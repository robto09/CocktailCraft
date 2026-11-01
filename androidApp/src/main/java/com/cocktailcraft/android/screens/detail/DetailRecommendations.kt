package com.cocktailcraft.android.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.android.ui.components.OptimizedImage
import com.cocktailcraft.android.ui.components.formatPrice
import com.cocktailcraft.android.ui.preview.PreviewData
import com.cocktailcraft.android.ui.theme.AnimatedCocktailBarTheme
import com.cocktailcraft.android.ui.theme.CocktailCraftTheme
import com.cocktailcraft.android.ui.theme.Spacing
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.detail_loading_recommendations
import com.cocktailcraft.resources.detail_you_might_also_like
import com.cocktailcraft.resources.list_view_details_label
import com.revenuecat.placeholder.PlaceholderSurface
import com.revenuecat.placeholder.placeholder

// Recommendations card with shimmer loading, empty and loaded states
@Composable
internal fun DetailRecommendationsSection(
    similarCocktails: List<Cocktail>,
    isLoadingRecommendations: Boolean,
    onRecommendationClick: (Cocktail) -> Unit
) {
    // Same card, spacing and title as the other sections and as iOS
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg)
        ) {
            Text(
                text = stringResource(MR.strings.detail_you_might_also_like),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() }
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            // Show loading state or recommendations
            if (isLoadingRecommendations) {
                RecommendationsLoadingRow()
            } else if (similarCocktails.isEmpty()) {
                // Show a message if no recommendations
                Text(
                    text = stringResource(MR.strings.detail_loading_recommendations),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = Spacing.lg)
                )
            } else {
                // Show recommendations
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    items(similarCocktails, key = { it.id }) { recommendation ->
                        // Simple recommendation card
                        RecommendationCard(
                            recommendation = recommendation,
                            onClick = { onRecommendationClick(recommendation) }
                        )
                    }
                }
            }
        }
    }
}

// Shimmer placeholder row shown while recommendations load
@Composable
internal fun RecommendationsLoadingRow() {
    // One animation clock drives all three placeholders
    PlaceholderSurface {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            items(3) {
                // Loading shimmer
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(200.dp)
                        .placeholder(shape = RoundedCornerShape(12.dp))
                )
            }
        }
    }
}

// Small card for a single recommended cocktail
@Composable
internal fun RecommendationCard(
    recommendation: Cocktail,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(
                onClickLabel = stringResource(MR.strings.list_view_details_label, recommendation.name),
                onClick = onClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column {
            // Cocktail image with fallback
            Box(
                modifier = Modifier
                    .height(140.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(CocktailCraftTheme.extendedColors.neutralSubtle),
                contentAlignment = Alignment.Center
            ) {
                // Show a placeholder icon if image URL is empty
                if (recommendation.imageUrl.isNullOrEmpty()) {
                    Icon(
                        imageVector = Icons.Default.LocalBar,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                } else {
                    // Use our optimized image component (decorative: the
                    // name is read from the text below it)
                    OptimizedImage(
                        url = recommendation.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        targetSize = 200,
                        showLoadingIndicator = true
                    )
                }
            }

            // Cocktail details
            Column(
                modifier = Modifier.padding(Spacing.sm)
            ) {
                // Cocktail name
                Text(
                    text = recommendation.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                // Price
                Text(
                    text = formatPrice(recommendation.price),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ---- Design-time previews ----

@Preview(name = "Recommendation card", showBackground = true)
@Composable
private fun RecommendationCardPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        RecommendationCard(
            recommendation = PreviewData.cocktail,
            onClick = {}
        )
    }
}
