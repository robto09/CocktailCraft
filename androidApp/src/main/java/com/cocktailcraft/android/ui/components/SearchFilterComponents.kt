package com.cocktailcraft.android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

        HorizontalDivider(modifier = Modifier.padding(top = if (expanded) 8.dp else 16.dp))
    }
}

/**
 * Category selector: an exposed-dropdown field (Material's pattern for a
 * read-only dropdown anchor) listing "All Categories" plus the API-backed
 * category list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        SelectorField(
            value = selectedCategory,
            placeholder = stringResource(R.string.filter_select_category),
            expanded = expanded
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // "All" option clears the category filter
            DropdownMenuItem(
                text = { Text(stringResource(R.string.filter_all_categories)) },
                onClick = {
                    onCategorySelected(null)
                    expanded = false
                }
            )

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
 * Shared read-only anchor field for the selector dropdowns, styled to the
 * app's outlined look (8dp corners, light gray outline).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposedDropdownMenuBoxScope.SelectorField(
    value: String?,
    placeholder: String,
    expanded: Boolean
) {
    OutlinedTextField(
        value = value.orEmpty(),
        onValueChange = {},
        readOnly = true,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextSecondary
            )
        },
        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AppColors.TextPrimary,
            unfocusedTextColor = AppColors.TextPrimary,
            focusedBorderColor = AppColors.Primary,
            unfocusedBorderColor = AppColors.LightGray,
            focusedTrailingIconColor = AppColors.TextSecondary,
            unfocusedTrailingIconColor = AppColors.TextSecondary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
    )
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
 * Single-select ingredient picker: a full-screen dialog — Material's pattern
 * for choosing from a long list — with the app's branded top bar, a search
 * box, and a lazy radio list. Selecting a row (or "Any ingredient") applies
 * the choice immediately and dismisses; back closes without changes.
 */
@Composable
fun IngredientSelectionDialog(
    ingredients: List<String>,
    selectedIngredient: String?,
    onDismiss: () -> Unit,
    onIngredientSelected: (String?) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppColors.Surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
                AppTopBar(
                    title = stringResource(R.string.filter_select_ingredient_title),
                    showBackButton = true,
                    onBackClick = onDismiss
                )

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
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

                val filteredIngredients = ingredients.filter {
                    it.contains(searchQuery, ignoreCase = true)
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    // "Any" option clears the ingredient filter
                    item {
                        IngredientRow(
                            label = null,
                            selected = selectedIngredient == null,
                            onClick = { onIngredientSelected(null) }
                        )
                    }

                    items(filteredIngredients) { ingredient ->
                        IngredientRow(
                            label = ingredient,
                            selected = selectedIngredient == ingredient,
                            onClick = { onIngredientSelected(ingredient) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * A selectable ingredient row with a radio indicator; null = "Any ingredient".
 */
@Composable
private fun IngredientRow(
    label: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )

        Text(
            text = label ?: stringResource(R.string.filter_any_ingredient),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

/**
 * Single-select ingredient selector: the same exposed-dropdown anchor field
 * as the category selector, but "expanding" it opens the searchable
 * [IngredientSelectionDialog] — the ingredient list is far too long for an
 * inline dropdown menu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientSelector(
    ingredients: List<String>,
    selectedIngredient: String?,
    onIngredientSelected: (String?) -> Unit
) {
    var showIngredientDialog by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = false,
        onExpandedChange = { showIngredientDialog = true }
    ) {
        SelectorField(
            value = selectedIngredient,
            placeholder = stringResource(R.string.filter_select_an_ingredient),
            expanded = false
        )
    }

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
