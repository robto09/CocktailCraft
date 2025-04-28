package com.cocktailcraft.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cocktailcraft.domain.model.Complexity
import com.cocktailcraft.domain.model.PreparationTime
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.domain.model.TasteProfile
import com.cocktailcraft.ui.theme.AppColors
import com.cocktailcraft.ui.components.FilterChip

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
                contentDescription = if (expanded) "Collapse" else "Expand",
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
                text = selectedCategory ?: "Select a category",
                style = MaterialTheme.typography.bodyMedium,
                color = if (selectedCategory != null) AppColors.TextPrimary else AppColors.TextSecondary
            )

            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = "Expand",
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
                text = { Text("All Categories") },
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
 * Glass selector component
 */
@Composable
fun GlassSelector(
    glasses: List<String>,
    selectedGlass: String?,
    onGlassSelected: (String?) -> Unit
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
                text = selectedGlass ?: "Select a glass type",
                style = MaterialTheme.typography.bodyMedium,
                color = if (selectedGlass != null) AppColors.TextPrimary else AppColors.TextSecondary
            )

            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = "Expand",
                tint = AppColors.TextSecondary
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            // Add "All" option to clear the glass filter
            DropdownMenuItem(
                text = { Text("All Glass Types") },
                onClick = {
                    onGlassSelected(null)
                    expanded = false
                }
            )

            // Add all glass types
            glasses.forEach { glass ->
                DropdownMenuItem(
                    text = { Text(glass) },
                    onClick = {
                        onGlassSelected(glass)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Alcoholic filter content component
 */
@Composable
fun AlcoholicFilterContent(
    alcoholic: Boolean?,
    onAlcoholicChanged: (Boolean?) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Show only alcoholic drinks",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = alcoholic == true,
                onCheckedChange = { isChecked ->
                    onAlcoholicChanged(if (isChecked) true else null)
                }
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Show only non-alcoholic drinks",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = alcoholic == false,
                onCheckedChange = { isChecked ->
                    onAlcoholicChanged(if (isChecked) false else null)
                }
            )
        }
    }
}

/**
 * Price range filter content component
 */
@Composable
fun PriceRangeFilterContent(
    priceRange: ClosedFloatingPointRange<Float>?,
    onPriceRangeChanged: (ClosedFloatingPointRange<Float>?) -> Unit
) {
    var currentPriceRange by remember {
        mutableStateOf(priceRange ?: 5f..15f)
    }
    var isPriceFilterActive by remember {
        mutableStateOf(priceRange != null)
    }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Filter by price",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = isPriceFilterActive,
                onCheckedChange = { isChecked ->
                    isPriceFilterActive = isChecked
                    onPriceRangeChanged(if (isChecked) currentPriceRange else null)
                }
            )
        }

        if (isPriceFilterActive) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                RangeSlider(
                    value = currentPriceRange,
                    onValueChange = { range ->
                        currentPriceRange = range
                        onPriceRangeChanged(range)
                    },
                    valueRange = 5f..30f,
                    steps = 25,
                    colors = SliderDefaults.colors(
                        thumbColor = AppColors.Primary,
                        activeTrackColor = AppColors.Primary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$${currentPriceRange.start.toInt()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "$${currentPriceRange.endInclusive.toInt()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * Taste profile selector component
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TasteProfileSelector(
    selectedProfile: TasteProfile?,
    onProfileSelected: (TasteProfile?) -> Unit
) {
    Column {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Add "All" option
            FilterChip(
                selected = selectedProfile == null,
                onClick = { onProfileSelected(null) },
                label = "All"
            )

            // Add all taste profiles
            TasteProfile.values().forEach { profile ->
                FilterChip(
                    selected = selectedProfile == profile,
                    onClick = { onProfileSelected(profile) },
                    label = profile.toString()
                )
            }
        }
    }
}

/**
 * Complexity selector component
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComplexitySelector(
    selectedComplexity: Complexity?,
    onComplexitySelected: (Complexity?) -> Unit
) {
    Column {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Add "All" option
            FilterChip(
                selected = selectedComplexity == null,
                onClick = { onComplexitySelected(null) },
                label = "All"
            )

            // Add all complexity levels
            Complexity.values().forEach { complexity ->
                FilterChip(
                    selected = selectedComplexity == complexity,
                    onClick = { onComplexitySelected(complexity) },
                    label = complexity.toString()
                )
            }
        }
    }
}

/**
 * Preparation time selector component
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PrepTimeSelector(
    selectedPrepTime: PreparationTime?,
    onPrepTimeSelected: (PreparationTime?) -> Unit
) {
    Column {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Add "All" option
            FilterChip(
                selected = selectedPrepTime == null,
                onClick = { onPrepTimeSelected(null) },
                label = "All"
            )

            // Add all preparation times
            PreparationTime.values().forEach { prepTime ->
                FilterChip(
                    selected = selectedPrepTime == prepTime,
                    onClick = { onPrepTimeSelected(prepTime) },
                    label = prepTime.toString()
                )
            }
        }
    }
}

/**
 * Ingredient selection dialog component
 */
@Composable
fun IngredientSelectionDialog(
    ingredients: List<String>,
    selectedIngredients: List<String>,
    dialogTitle: String,
    onDismiss: () -> Unit,
    onIngredientsSelected: (List<String>) -> Unit
) {
    val selected = remember { mutableStateListOf<String>().apply { addAll(selectedIngredients) } }
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
                    text = dialogTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search ingredients") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = AppColors.Gray
                        )
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        cursorColor = AppColors.Primary,
                        focusedIndicatorColor = AppColors.Primary,
                        focusedLeadingIconColor = AppColors.Primary
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
                    filteredIngredients.forEach { ingredient ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    if (selected.contains(ingredient)) {
                                        selected.remove(ingredient)
                                    } else {
                                        selected.add(ingredient)
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selected.contains(ingredient),
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        selected.add(ingredient)
                                    } else {
                                        selected.remove(ingredient)
                                    }
                                }
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
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onIngredientsSelected(selected.toList()) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Primary
                        )
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

/**
 * Ingredient selector component
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun IngredientSelector(
    ingredients: List<String>,
    selectedIngredients: List<String>,
    excludedIngredients: List<String>,
    onIngredientsChanged: (List<String>, List<String>) -> Unit
) {
    var showIngredientDialog by remember { mutableStateOf(false) }
    var dialogMode by remember { mutableStateOf("include") }

    Column {
        // Selected ingredients
        if (selectedIngredients.isNotEmpty()) {
            Text(
                text = "Must include:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedIngredients.forEach { ingredient ->
                    FilterChip(
                        selected = true,
                        onClick = {
                            val updated = selectedIngredients.toMutableList().apply {
                                remove(ingredient)
                            }
                            onIngredientsChanged(updated, excludedIngredients)
                        },
                        label = ingredient,
                        trailingIcon = Icons.Default.Clear
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Excluded ingredients
        if (excludedIngredients.isNotEmpty()) {
            Text(
                text = "Must exclude:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                excludedIngredients.forEach { ingredient ->
                    FilterChip(
                        selected = true,
                        onClick = {
                            val updated = excludedIngredients.toMutableList().apply {
                                remove(ingredient)
                            }
                            onIngredientsChanged(selectedIngredients, updated)
                        },
                        label = ingredient,
                        trailingIcon = Icons.Default.Clear,
                        selectedColor = Color.Red.copy(alpha = 0.1f),
                        selectedTextColor = Color.Red,
                        selectedIconColor = Color.Red
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Add ingredient buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    dialogMode = "include"
                    showIngredientDialog = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Primary.copy(alpha = 0.1f)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Ingredient",
                    tint = AppColors.Primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Include",
                    color = AppColors.Primary
                )
            }

            Button(
                onClick = {
                    dialogMode = "exclude"
                    showIngredientDialog = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red.copy(alpha = 0.1f)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Exclude Ingredient",
                    tint = Color.Red
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Exclude",
                    color = Color.Red
                )
            }
        }

        // Ingredient selection dialog
        if (showIngredientDialog) {
            IngredientSelectionDialog(
                ingredients = ingredients,
                selectedIngredients = if (dialogMode == "include") selectedIngredients else excludedIngredients,
                dialogTitle = if (dialogMode == "include") "Include Ingredients" else "Exclude Ingredients",
                onDismiss = { showIngredientDialog = false },
                onIngredientsSelected = { selected: List<String> ->
                    if (dialogMode == "include") {
                        onIngredientsChanged(selected, excludedIngredients)
                    } else {
                        onIngredientsChanged(selectedIngredients, selected)
                    }
                    showIngredientDialog = false
                }
            )
        }
    }
}

/**
 * Advanced search panel that allows users to filter cocktails by various criteria
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun AdvancedSearchPanel(
    isVisible: Boolean,
    currentFilters: SearchFilters,
    categories: List<String>,
    ingredients: List<String>,
    glasses: List<String>,
    onApplyFilters: (SearchFilters) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f),
                shape = RoundedCornerShape(16.dp),
                color = AppColors.Surface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                // Header with title and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
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
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = AppColors.TextSecondary
                        )
                    }
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
}}
