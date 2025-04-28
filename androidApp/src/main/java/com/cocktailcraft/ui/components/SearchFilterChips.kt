package com.cocktailcraft.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.ui.theme.AppColors

/**
 * Component to display active search filters as chips
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun SearchFilterChips(
    filters: SearchFilters,
    onClearFilter: (String) -> Unit,
    onClearAllFilters: () -> Unit
) {
    // Only show if we have active filters
    AnimatedVisibility(
        visible = filters.hasActiveFilters(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            color = AppColors.Surface,
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // Header with filter icon and clear all button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterAlt,
                        contentDescription = "Filters",
                        tint = AppColors.Primary,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Active Filters",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "Clear All",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.Primary,
                        modifier = Modifier.clickable { onClearAllFilters() }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Filter chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category filter
                    if (filters.category != null) {
                        ActiveFilterChip(
                            label = "Category: ${filters.category}",
                            onClear = { onClearFilter("category") }
                        )
                    }

                    // Ingredient filter
                    if (filters.ingredient != null) {
                        ActiveFilterChip(
                            label = "Ingredient: ${filters.ingredient}",
                            onClear = { onClearFilter("ingredient") }
                        )
                    }

                    // Multiple ingredients
                    if (filters.ingredients.isNotEmpty()) {
                        ActiveFilterChip(
                            label = "Ingredients: ${filters.ingredients.size}",
                            onClear = { onClearFilter("ingredients") }
                        )
                    }

                    // Excluded ingredients
                    if (filters.excludeIngredients.isNotEmpty()) {
                        ActiveFilterChip(
                            label = "Excluded: ${filters.excludeIngredients.size}",
                            onClear = { onClearFilter("excludeIngredients") }
                        )
                    }

                    // Alcoholic filter
                    if (filters.alcoholic != null) {
                        val alcoholicLabel = when (filters.alcoholic) {
                            true -> "Alcoholic"
                            false -> "Non-Alcoholic"
                            else -> "Alcoholic Filter"
                        }
                        ActiveFilterChip(
                            label = alcoholicLabel,
                            onClear = { onClearFilter("alcoholic") }
                        )
                    }

                    // Glass filter
                    if (filters.glass != null) {
                        ActiveFilterChip(
                            label = "Glass: ${filters.glass}",
                            onClear = { onClearFilter("glass") }
                        )
                    }

                    // Price range filter
                    if (filters.priceRange != null) {
                        val priceRange = filters.priceRange!! // Safe to use !! here as we've checked for null
                        val priceLabel = "Price: $${priceRange.start.toInt()}-$${priceRange.endInclusive.toInt()}"
                        ActiveFilterChip(
                            label = priceLabel,
                            onClear = { onClearFilter("priceRange") }
                        )
                    }

                    // Taste profile filter
                    if (filters.tasteProfile != null) {
                        ActiveFilterChip(
                            label = "Taste: ${filters.tasteProfile}",
                            onClear = { onClearFilter("tasteProfile") }
                        )
                    }

                    // Complexity filter
                    if (filters.complexity != null) {
                        ActiveFilterChip(
                            label = "Complexity: ${filters.complexity}",
                            onClear = { onClearFilter("complexity") }
                        )
                    }

                    // Preparation time filter
                    if (filters.preparationTime != null) {
                        ActiveFilterChip(
                            label = "Prep Time: ${filters.preparationTime}",
                            onClear = { onClearFilter("preparationTime") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveFilterChip(
    label: String,
    onClear: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.Primary.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.Primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Clear",
                tint = AppColors.Primary,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onClear() }
            )
        }
    }
}

@Composable
fun Column(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier
    ) {
        content()
    }
}
