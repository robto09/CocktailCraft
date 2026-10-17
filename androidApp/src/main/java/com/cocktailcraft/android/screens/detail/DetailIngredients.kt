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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.android.ui.components.DetailInfoCard
import com.cocktailcraft.android.ui.preview.PreviewData
import com.cocktailcraft.android.ui.theme.AnimatedCocktailBarTheme
import com.cocktailcraft.android.ui.theme.Spacing
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.domain.model.CocktailIngredient
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.alcoholic
import com.cocktailcraft.resources.category_label
import com.cocktailcraft.resources.details
import com.cocktailcraft.resources.glass_label
import com.cocktailcraft.resources.ingredients
import com.cocktailcraft.resources.non_alcoholic

// Ingredients list rendered inside an info card
@Composable
internal fun DetailIngredientsSection(ingredients: List<CocktailIngredient>) {
    DetailInfoCard(
        title = stringResource(MR.strings.ingredients),
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
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )

                    Spacer(modifier = Modifier.width(Spacing.md))

                    // Ingredient name and measure
                    Text(
                        text = "${ingredient.name} ${ingredient.measure}",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
        title = stringResource(MR.strings.details),
        modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        content = {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                // Category chip
                category?.let {
                    item {
                        DetailChip(label = stringResource(MR.strings.category_label, it))
                    }
                }

                // Glass type chip
                glass?.let {
                    item {
                        DetailChip(label = stringResource(MR.strings.glass_label, it))
                    }
                }

                // Alcoholic chip
                item {
                    DetailChip(label = if (alcoholic == "Alcoholic") stringResource(MR.strings.alcoholic) else stringResource(MR.strings.non_alcoholic))
                }
            }
        },
        elevation = 0
    )
}

// Read-only attribute chip. SuggestionChip is always clickable, so its
// semantics are replaced with plain text — TalkBack would otherwise announce
// a button that does nothing.
@Composable
private fun DetailChip(label: String) {
    SuggestionChip(
        onClick = {},
        label = { Text(label) },
        modifier = Modifier.clearAndSetSemantics { contentDescription = label },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
            labelColor = MaterialTheme.colorScheme.onSurface
        )
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
