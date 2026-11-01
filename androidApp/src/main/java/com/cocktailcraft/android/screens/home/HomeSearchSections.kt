package com.cocktailcraft.android.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.cocktailcraft.android.ui.components.AdvancedSearchBottomSheet
import com.cocktailcraft.android.ui.components.CategoryFilterRow
import com.cocktailcraft.android.ui.components.CocktailSearchBar
import com.cocktailcraft.android.ui.components.HomeSortButton
import com.cocktailcraft.android.ui.components.SearchFilterChips
import com.cocktailcraft.android.ui.theme.AnimatedCocktailBarTheme
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.viewmodel.state.HomeSortOption

// The Home screen's search controls: the search bar, the active-filter
// chips, the advanced-search sheet and the category chips.

// Search Bar with the Advanced Search and Sort buttons
@Composable
internal fun HomeSearchBarSection(
    searchQuery: String,
    isAdvancedSearchActive: Boolean,
    hasActiveFilters: Boolean,
    sortOption: HomeSortOption,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onToggleAdvancedSearch: () -> Unit,
    onSortChange: (HomeSortOption) -> Unit
) {
    CocktailSearchBar(
        searchQuery = searchQuery,
        isAdvancedSearchActive = isAdvancedSearchActive,
        hasActiveFilters = hasActiveFilters,
        onSearchQueryChange = onSearchQueryChange,
        onClearSearch = onClearSearch,
        onToggleAdvancedSearch = onToggleAdvancedSearch,
        trailingContent = { HomeSortButton(selected = sortOption, onSelect = onSortChange) }
    )
}

// Active filters display
@Composable
internal fun HomeActiveFiltersSection(
    searchFilters: SearchFilters,
    onApplyFilters: (SearchFilters) -> Unit,
    onClearAllFilters: () -> Unit
) {
    SearchFilterChips(
        filters = searchFilters,
        onClearFilter = { filterType ->
            // Create a copy of current filters with the specified filter cleared
            val updatedFilters = when (filterType) {
                "category" -> searchFilters.copy(category = null)
                "ingredient" -> searchFilters.copy(ingredient = null)
                "alcoholic" -> searchFilters.copy(alcoholic = null)
                else -> searchFilters
            }
            onApplyFilters(updatedFilters)
        },
        onClearAllFilters = onClearAllFilters
    )
}

// Advanced search panel shown as a modal bottom sheet while active
@Composable
internal fun HomeAdvancedSearchSheet(
    isVisible: Boolean,
    currentFilters: SearchFilters,
    categories: List<String>,
    ingredients: List<String>,
    onApplyFilters: (SearchFilters) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        AdvancedSearchBottomSheet(
            currentFilters = currentFilters,
            categories = categories,
            ingredients = ingredients,
            onApplyFilters = onApplyFilters,
            onClearFilters = onClearFilters,
            onDismiss = onDismiss
        )
    }
}

// Category Filter Chips row - only shown when not searching
@Composable
internal fun HomeCategoryChipsRow(
    isSearchActive: Boolean,
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    if (!isSearchActive) {
        // No extra vertical padding: the Material chips' 48dp touch target
        // already extends ~8dp beyond their 32dp visual, which lands the
        // visible gaps on the same 12dp rhythm as the iOS header.
        CategoryFilterRow(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = onCategorySelected,
            verticalPadding = 0
        )
    }
}

@Preview(name = "Home search bar — query + filters active", showBackground = true)
@Composable
private fun HomeSearchBarSectionPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        HomeSearchBarSection(
            searchQuery = "Margarita",
            isAdvancedSearchActive = false,
            hasActiveFilters = true,
            sortOption = HomeSortOption.PRICE_ASC,
            onSearchQueryChange = {},
            onClearSearch = {},
            onToggleAdvancedSearch = {},
            onSortChange = {}
        )
    }
}

@Preview(name = "Home search bar (dark)", showBackground = true)
@Composable
private fun HomeSearchBarSectionDarkPreview() {
    AnimatedCocktailBarTheme(darkTheme = true) {
        HomeSearchBarSection(
            searchQuery = "",
            isAdvancedSearchActive = false,
            hasActiveFilters = false,
            sortOption = HomeSortOption.DEFAULT,
            onSearchQueryChange = {},
            onClearSearch = {},
            onToggleAdvancedSearch = {},
            onSortChange = {}
        )
    }
}

@Preview(name = "Home active filters", showBackground = true)
@Composable
private fun HomeActiveFiltersSectionPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        HomeActiveFiltersSection(
            searchFilters = SearchFilters(
                category = "Cocktail",
                ingredient = "Tequila",
                alcoholic = true
            ),
            onApplyFilters = {},
            onClearAllFilters = {}
        )
    }
}

@Preview(name = "Home active filters (dark)", showBackground = true)
@Composable
private fun HomeActiveFiltersSectionDarkPreview() {
    AnimatedCocktailBarTheme(darkTheme = true) {
        HomeActiveFiltersSection(
            searchFilters = SearchFilters(
                category = "Cocktail",
                ingredient = "Tequila",
                alcoholic = true
            ),
            onApplyFilters = {},
            onClearAllFilters = {}
        )
    }
}

@Preview(name = "Home category chips", showBackground = true)
@Composable
private fun HomeCategoryChipsRowPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        HomeCategoryChipsRow(
            isSearchActive = false,
            categories = listOf("Cocktail", "Ordinary Drink", "Shot"),
            selectedCategory = "Cocktail",
            onCategorySelected = {}
        )
    }
}

@Preview(name = "Home category chips (dark)", showBackground = true)
@Composable
private fun HomeCategoryChipsRowDarkPreview() {
    AnimatedCocktailBarTheme(darkTheme = true) {
        HomeCategoryChipsRow(
            isSearchActive = false,
            categories = listOf("Cocktail", "Ordinary Drink", "Shot"),
            selectedCategory = "Cocktail",
            onCategorySelected = {}
        )
    }
}
