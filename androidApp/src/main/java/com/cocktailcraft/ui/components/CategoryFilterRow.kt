package com.cocktailcraft.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A reusable component for displaying a horizontal row of category filter chips.
 *
 * @param categories The list of categories to display
 * @param selectedCategory The currently selected category, if any
 * @param onCategorySelected Callback when a category is selected
 * @param modifier The modifier for the component
 * @param horizontalPadding The horizontal padding for the row
 * @param verticalPadding The vertical padding for the row
 * @param chipSpacing The spacing between chips
 * @param allCategoryLabel The label for the "All" category
 */
@Composable
fun CategoryFilterRow(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
    horizontalPadding: Int = 16,
    verticalPadding: Int = 8,
    chipSpacing: Int = 8,
    allCategoryLabel: String = "All"
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = verticalPadding.dp, horizontal = horizontalPadding.dp),
        horizontalArrangement = Arrangement.spacedBy(chipSpacing.dp)
    ) {
        items(categories) { category ->
            val isSelected = if (category == allCategoryLabel) {
                selectedCategory == null
            } else {
                selectedCategory == category
            }
            
            FilterChip(
                selected = isSelected,
                onClick = {
                    onCategorySelected(if (category == allCategoryLabel) null else category)
                },
                label = category
            )
        }
    }
}
