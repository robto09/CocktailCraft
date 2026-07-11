package com.cocktailcraft.android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cocktailcraft.android.R
import com.cocktailcraft.android.ui.theme.AppColors

/**
 * Section component for filter categories
 */
@Composable
fun FilterSection(
    title: String,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) stringResource(R.string.filter_collapse) else stringResource(R.string.filter_expand),
                tint = AppColors.TextSecondary
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
            ) {
                content()
            }
        }

        Divider(modifier = Modifier.padding(top = if (expanded) 8.dp else 16.dp))
    }
}

/**
 * Category selector component
 */
@Composable
fun CategorySelector(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = AppColors.LightGray,
                    shape = RoundedCornerShape(8.dp)
                )
                .clip(RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedCategory ?: stringResource(R.string.filter_select_category),
                style = MaterialTheme.typography.bodyMedium,
                color = if (selectedCategory != null) AppColors.TextPrimary else AppColors.TextSecondary
            )

            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = stringResource(R.string.filter_expand),
                tint = AppColors.TextSecondary
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            // Add "All" option to clear the category filter
            DropdownMenuItem(
                text = { Text(stringResource(R.string.filter_all_categories)) },
                onClick = {
                    onCategorySelected(null)
                    expanded = false
                }
            )

            // Add all categories
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Alcoholic filter content: tri-state choice chips (Any / Alcoholic / Non-Alcoholic).
 * null = any, true = alcoholic, false = non-alcoholic.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlcoholicFilterContent(
    alcoholic: Boolean?,
    onAlcoholicChanged: (Boolean?) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = alcoholic == null,
            onClick = { onAlcoholicChanged(null) },
            label = stringResource(R.string.filter_any)
        )
        FilterChip(
            selected = alcoholic == true,
            onClick = { onAlcoholicChanged(true) },
            label = stringResource(R.string.alcoholic)
        )
        FilterChip(
            selected = alcoholic == false,
            onClick = { onAlcoholicChanged(false) },
            label = stringResource(R.string.non_alcoholic)
        )
    }
}

/**
 * Single-select ingredient selection dialog with a search box.
 * Selecting a row (or "Any ingredient") applies the choice immediately and dismisses.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientSelectionDialog(
    ingredients: List<String>,
    selectedIngredient: String?,
    onDismiss: () -> Unit,
    onIngredientSelected: (String?) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AppColors.Surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                // Dialog title
                Text(
                    text = stringResource(R.string.filter_select_ingredient_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.search_ingredients_placeholder)) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = stringResource(R.string.search),
                            tint = AppColors.Gray
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.Primary,
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = AppColors.Primary,
                        focusedLeadingIconColor = AppColors.Primary,
                        unfocusedLeadingIconColor = Color.Gray,
                        focusedContainerColor = Color.White, unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Filtered ingredients list
                val filteredIngredients = ingredients.filter {
                    it.contains(searchQuery, ignoreCase = true)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // "Any" option clears the ingredient filter
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onIngredientSelected(null) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedIngredient == null,
                            onClick = { onIngredientSelected(null) }
                        )

                        Text(
                            text = stringResource(R.string.filter_any_ingredient),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    filteredIngredients.forEach { ingredient ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onIngredientSelected(ingredient) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedIngredient == ingredient,
                                onClick = { onIngredientSelected(ingredient) }
                            )

                            Text(
                                text = ingredient,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        }
    }
}

/**
 * Single-select ingredient selector: a dropdown-styled row that opens the
 * searchable [IngredientSelectionDialog].
 */
@Composable
fun IngredientSelector(
    ingredients: List<String>,
    selectedIngredient: String?,
    onIngredientSelected: (String?) -> Unit
) {
    var showIngredientDialog by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = AppColors.LightGray,
                    shape = RoundedCornerShape(8.dp)
                )
                .clip(RoundedCornerShape(8.dp))
                .clickable { showIngredientDialog = true }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedIngredient ?: stringResource(R.string.filter_select_an_ingredient),
                style = MaterialTheme.typography.bodyMedium,
                color = if (selectedIngredient != null) AppColors.TextPrimary else AppColors.TextSecondary
            )

            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = stringResource(R.string.filter_expand),
                tint = AppColors.TextSecondary
            )
        }

        // Ingredient selection dialog
        if (showIngredientDialog) {
            IngredientSelectionDialog(
                ingredients = ingredients,
                selectedIngredient = selectedIngredient,
                onDismiss = { showIngredientDialog = false },
                onIngredientSelected = { ingredient: String? ->
                    onIngredientSelected(ingredient)
                    showIngredientDialog = false
                }
            )
        }
    }
}
