package com.cocktailcraft.android.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.android.R
import com.cocktailcraft.android.ui.components.DetailInfoCard
import com.cocktailcraft.android.ui.preview.PreviewData
import com.cocktailcraft.android.ui.theme.AnimatedCocktailBarTheme
import com.cocktailcraft.android.ui.theme.AppColors
import com.cocktailcraft.android.ui.theme.Spacing
import com.cocktailcraft.domain.model.CocktailIngredient

// Ingredients list rendered inside an info card
@Composable
internal fun DetailIngredientsSection(ingredients: List<CocktailIngredient>) {
    DetailInfoCard(
        title = stringResource(R.string.ingredients),
        modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        content = {

            // Handle different types of ingredients list
            (ingredients as? List<CocktailIngredient>)?.forEach { ingredient ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ingredient bullet point
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(AppColors.Primary, CircleShape)
                    )

                    Spacer(modifier = Modifier.width(Spacing.md))

                    // Ingredient name and measure
                    Text(
                        text = "${ingredient.name} ${ingredient.measure}",
                        fontSize = 16.sp,
                        color = AppColors.TextSecondary
                    )
                }
            }

        },
        elevation = 0
    )
}

// Category, glass and alcoholic detail chips
@Composable
internal fun DetailChipsSection(
    category: String?,
    glass: String?,
    alcoholic: String?
) {
    DetailInfoCard(
        title = stringResource(R.string.details),
        modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        content = {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                // Category chip
                category?.let {
                    item {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(stringResource(R.string.category_label, it)) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = AppColors.ChipBackground.copy(alpha = 0.2f),
                                labelColor = AppColors.TextPrimary
                            )
                        )
                    }
                }

                // Glass type chip
                glass?.let {
                    item {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(stringResource(R.string.glass_label, it)) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = AppColors.ChipBackground.copy(alpha = 0.2f),
                                labelColor = AppColors.TextPrimary
                            )
                        )
                    }
                }

                // Alcoholic chip
                item {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(if (alcoholic == "Alcoholic") stringResource(R.string.alcoholic) else stringResource(R.string.non_alcoholic)) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = AppColors.ChipBackground.copy(alpha = 0.2f),
                            labelColor = AppColors.TextPrimary
                        )
                    )
                }
            }
        },
        elevation = 0
    )
}

// ---- Design-time previews ----

@Preview(name = "Ingredients section", showBackground = true)
@Composable
private fun DetailIngredientsSectionPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        DetailIngredientsSection(ingredients = PreviewData.cocktail.ingredients)
    }
}

@Preview(name = "Details chips section", showBackground = true)
@Composable
private fun DetailChipsSectionPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        DetailChipsSection(
            category = PreviewData.cocktail.category,
            glass = PreviewData.cocktail.glass,
            alcoholic = PreviewData.cocktail.alcoholic
        )
    }
}
