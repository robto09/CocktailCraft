package com.cocktailcraft.domain.model

/**
 * Model representing search filters for cocktails
 */
data class SearchFilters(
    val query: String = "",
    val category: String? = null,
    val ingredient: String? = null,
    val alcoholic: Boolean? = null,
    val priceRange: ClosedFloatingPointRange<Float>? = null,
    
    // Additional filters for advanced search
    val ingredients: List<String> = emptyList(), // Multiple ingredients
    val excludeIngredients: List<String> = emptyList() // Ingredients to exclude
) {
    // Helper function to check if any filters are active
    fun hasActiveFilters(): Boolean {
        return category != null || 
               ingredient != null || 
               alcoholic != null || 
               priceRange != null ||
               ingredients.isNotEmpty() ||
               excludeIngredients.isNotEmpty()
    }
    
    // Helper function to check if basic search is active
    fun hasBasicSearch(): Boolean {
        return query.isNotBlank()
    }
    
    // Helper function to get a description of active filters
    fun getActiveFiltersDescription(): String {
        val activeFilters = mutableListOf<String>()
        
        if (category != null) activeFilters.add("Category: $category")
        if (ingredient != null) activeFilters.add("Ingredient: $ingredient")
        if (alcoholic != null) activeFilters.add(if (alcoholic) "Alcoholic" else "Non-Alcoholic")
        if (priceRange != null) activeFilters.add("Price: $${priceRange.start} - $${priceRange.endInclusive}")
        if (ingredients.isNotEmpty()) activeFilters.add("Ingredients: ${ingredients.joinToString(", ")}")
        if (excludeIngredients.isNotEmpty()) activeFilters.add("Exclude: ${excludeIngredients.joinToString(", ")}")
        return activeFilters.joinToString(" • ")
    }
    
    // Helper function to clear all filters
    fun clearAllFilters(): SearchFilters {
        return copy(
            category = null,
            ingredient = null,
            alcoholic = null,
            priceRange = null,
            ingredients = emptyList(),
            excludeIngredients = emptyList()
        )
    }
}

