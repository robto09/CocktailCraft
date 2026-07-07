package com.cocktailcraft.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cocktailcraft.android.ui.theme.AppColors
import com.cocktailcraft.domain.model.SearchFilters
import kotlinx.coroutines.launch

/**
 * Advanced-search filter sheet, mirroring the iOS AdvancedSearchSheet: a modal
 * bottom sheet with Clear / Apply actions in the header and the three filter
 * sections (Category, Ingredient, Alcoholic). Apply and Clear both close the
 * sheet; the actual filtering runs through the shared ViewModel.
 *
 * @param currentFilters The filters the sheet opens with
 * @param categories List of available categories
 * @param ingredients List of available ingredients
 * @param onApplyFilters Callback with the chosen filters (sheet closes itself)
 * @param onClearFilters Callback when filters are cleared (sheet closes itself)
 * @param onDismiss Callback once the sheet is fully hidden
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSearchBottomSheet(
    currentFilters: SearchFilters,
    categories: List<String>,
    ingredients: List<String>,
    onApplyFilters: (SearchFilters) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    fun animateClose() {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppColors.Surface
    ) {
        var filters by remember { mutableStateOf(currentFilters) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header: Clear | title | Apply (mirrors the iOS sheet toolbar)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        onClearFilters()
                        animateClose()
                    }
                ) {
                    Text("Clear")
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Advanced Search",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        onApplyFilters(filters)
                        animateClose()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
                ) {
                    Text("Apply")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            FilterSection(title = "Category") {
                CategorySelector(
                    categories = categories,
                    selectedCategory = filters.category,
                    onCategorySelected = { category: String? ->
                        filters = filters.copy(category = category)
                    }
                )
            }

            FilterSection(title = "Ingredient") {
                IngredientSelector(
                    ingredients = ingredients,
                    selectedIngredient = filters.ingredient,
                    onIngredientSelected = { ingredient: String? ->
                        filters = filters.copy(ingredient = ingredient)
                    }
                )
            }

            FilterSection(title = "Alcoholic") {
                AlcoholicFilterContent(
                    alcoholic = filters.alcoholic,
                    onAlcoholicChanged = { alcoholicValue ->
                        filters = filters.copy(alcoholic = alcoholicValue)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
