package com.cocktailcraft.android.screens.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.android.ui.components.DetailInfoCard
import com.cocktailcraft.android.ui.preview.PreviewData
import com.cocktailcraft.android.ui.theme.AnimatedCocktailBarTheme
import com.cocktailcraft.android.ui.theme.CocktailCraftTheme
import com.cocktailcraft.android.ui.theme.Spacing
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.domain.model.CocktailIngredient
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.category_label
import com.cocktailcraft.resources.detail_iba_label
import com.cocktailcraft.resources.details
import com.cocktailcraft.resources.glass_label
import com.cocktailcraft.resources.ingredients

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

                    // "1 oz Vodka", the shared format both platforms use
                    Text(
                        text = ingredient.displayText,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    )
}

// Category, glass, alcoholic and IBA chips (each only when the API has it)
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DetailChipsSection(
    category: String?,
    glass: String?,
    alcoholic: String?,
    iba: String? = null
) {
    DetailInfoCard(
        title = stringResource(MR.strings.details),
        modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        content = {
            // Wraps onto new lines instead of scrolling, so no chip is cut off.
            // The chips aren't tappable, so drop Material's 48dp touch-target
            // padding that would otherwise push the rows 24dp apart.
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    // Category chip
                    category?.let {
                        DetailChip(label = stringResource(MR.strings.category_label, it))
                    }

                    // Glass type chip
                    glass?.let {
                        DetailChip(label = stringResource(MR.strings.glass_label, it))
                    }

                    // The API's own value ("Alcoholic", "Non alcoholic", ...)
                    alcoholic?.let {
                        DetailChip(label = it)
                    }

                    iba?.let {
                        DetailChip(label = stringResource(MR.strings.detail_iba_label, it))
                    }
                }
            }
        }
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
        ),
        // Same border token as the iOS chips, not Material's default outline
        border = BorderStroke(1.dp, CocktailCraftTheme.extendedColors.neutralSubtle)
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
            alcoholic = PreviewData.cocktail.knownAlcoholic,
            iba = PreviewData.cocktail.iba
        )
    }
}
