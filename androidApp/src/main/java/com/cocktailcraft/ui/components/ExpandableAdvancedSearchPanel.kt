package com.cocktailcraft.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cocktailcraft.domain.model.Complexity
import com.cocktailcraft.domain.model.PreparationTime
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.domain.model.TasteProfile
import com.cocktailcraft.ui.theme.AppColors

/**
 * An expandable advanced search panel that can be integrated directly into the HomeScreen.
 * This component provides the same functionality as the dialog-based AdvancedSearchPanel
 * but is designed to be embedded in the screen layout.
 *
 * @param isExpanded Whether the panel is expanded
 * @param currentFilters The current search filters
 * @param categories List of available categories
 * @param ingredients List of available ingredients
 * @param glasses List of available glasses
 * @param onApplyFilters Callback when filters are applied
 * @param onClearFilters Callback when filters are cleared
 * @param modifier The modifier for the component
 */
@Composable
fun ExpandableAdvancedSearchPanel(
    isExpanded: Boolean,
    currentFilters: SearchFilters,
    categories: List<String>,
    ingredients: List<String>,
    glasses: List<String>,
    onApplyFilters: (SearchFilters) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Advanced Search",
                        tint = AppColors.Primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Advanced Search",
                        style = MaterialTheme.typography.titleMedium,
                        color = AppColors.TextPrimary
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Filter sections
                var filters by remember { mutableStateOf(currentFilters) }

                // Category filter
                FilterSection(title = "Category") {
                    CategorySelector(
                        categories = categories,
                        selectedCategory = filters.category,
                        onCategorySelected = { category: String? ->
                            filters = filters.copy(category = category)
                        }
                    )
                }

                // Ingredient filter
                FilterSection(title = "Ingredients") {
                    IngredientSelector(
                        ingredients = ingredients,
                        selectedIngredients = filters.ingredients,
                        excludedIngredients = filters.excludeIngredients,
                        onIngredientsChanged = { included: List<String>, excluded: List<String> ->
                            filters = filters.copy(
                                ingredients = included,
                                excludeIngredients = excluded
                            )
                        }
                    )
                }

                // Alcoholic filter
                FilterSection(title = "Alcoholic") {
                    AlcoholicFilterContent(
                        alcoholic = filters.alcoholic,
                        onAlcoholicChanged = { alcoholicValue ->
                            filters = filters.copy(alcoholic = alcoholicValue)
                        }
                    )
                }

                // Glass filter
                FilterSection(title = "Glass Type") {
                    GlassSelector(
                        glasses = glasses,
                        selectedGlass = filters.glass,
                        onGlassSelected = { glass: String? ->
                            filters = filters.copy(glass = glass)
                        }
                    )
                }

                // Price range filter
                FilterSection(title = "Price Range") {
                    PriceRangeFilterContent(
                        priceRange = filters.priceRange,
                        onPriceRangeChanged = { newPriceRange ->
                            filters = filters.copy(priceRange = newPriceRange)
                        }
                    )
                }

                // Taste profile filter
                FilterSection(title = "Taste Profile") {
                    TasteProfileSelector(
                        selectedProfile = filters.tasteProfile,
                        onProfileSelected = { profile: TasteProfile? ->
                            filters = filters.copy(tasteProfile = profile)
                        }
                    )
                }

                // Complexity filter
                FilterSection(title = "Complexity") {
                    ComplexitySelector(
                        selectedComplexity = filters.complexity,
                        onComplexitySelected = { complexity: Complexity? ->
                            filters = filters.copy(complexity = complexity)
                        }
                    )
                }

                // Preparation time filter
                FilterSection(title = "Preparation Time") {
                    PrepTimeSelector(
                        selectedPrepTime = filters.preparationTime,
                        onPrepTimeSelected = { prepTime: PreparationTime? ->
                            filters = filters.copy(preparationTime = prepTime)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            onClearFilters()
                            filters = SearchFilters(query = filters.query)
                        }
                    ) {
                        Text("Clear All")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onApplyFilters(filters) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Primary
                        )
                    ) {
                        Text("Apply Filters")
                    }
                }
            }
        }
    }
}
