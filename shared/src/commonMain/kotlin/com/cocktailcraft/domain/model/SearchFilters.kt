package com.cocktailcraft.domain.model

/**
 * Model representing search filters for cocktails
 */
data class SearchFilters(
    val query: String = "",
    val category: String? = null,
    val ingredient: String? = null,
    val alcoholic: Boolean? = null,
    val glass: String? = null,
    val priceRange: ClosedFloatingPointRange<Float>? = null,
    
    // Additional filters for advanced search
    val ingredients: List<String> = emptyList(), // Multiple ingredients
    val excludeIngredients: List<String> = emptyList(), // Ingredients to exclude
    val tasteProfile: TasteProfile? = null, // Taste profile preferences
    val complexity: Complexity? = null, // Complexity level
    val preparationTime: PreparationTime? = null // Preparation time
) {
    // Helper function to check if any filters are active
    fun hasActiveFilters(): Boolean {
        return category != null || 
               ingredient != null || 
               alcoholic != null || 
               glass != null || 
               priceRange != null ||
               ingredients.isNotEmpty() ||
               excludeIngredients.isNotEmpty() ||
               tasteProfile != null ||
               complexity != null ||
               preparationTime != null
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
        if (glass != null) activeFilters.add("Glass: $glass")
        if (priceRange != null) activeFilters.add("Price: $${priceRange.start} - $${priceRange.endInclusive}")
        if (ingredients.isNotEmpty()) activeFilters.add("Ingredients: ${ingredients.joinToString(", ")}")
        if (excludeIngredients.isNotEmpty()) activeFilters.add("Exclude: ${excludeIngredients.joinToString(", ")}")
        if (tasteProfile != null) activeFilters.add("Taste: $tasteProfile")
        if (complexity != null) activeFilters.add("Complexity: $complexity")
        if (preparationTime != null) activeFilters.add("Prep Time: $preparationTime")
        
        return activeFilters.joinToString(" • ")
    }
    
    // Helper function to clear all filters
    fun clearAllFilters(): SearchFilters {
        return copy(
            category = null,
            ingredient = null,
            alcoholic = null,
            glass = null,
            priceRange = null,
            ingredients = emptyList(),
            excludeIngredients = emptyList(),
            tasteProfile = null,
            complexity = null,
            preparationTime = null
        )
    }
}

/**
 * Enum representing taste profile preferences
 */
enum class TasteProfile {
    SWEET,
    SOUR,
    BITTER,
    SPICY,
    FRUITY,
    HERBAL,
    CREAMY;
    
    override fun toString(): String {
        return name.lowercase().replaceFirstChar { it.uppercase() }
    }
}

/**
 * Enum representing complexity level
 */
enum class Complexity {
    EASY,
    MEDIUM,
    COMPLEX;
    
    override fun toString(): String {
        return name.lowercase().replaceFirstChar { it.uppercase() }
    }
}

/**
 * Enum representing preparation time
 */
enum class PreparationTime {
    QUICK,
    MEDIUM,
    LONG;
    
    override fun toString(): String {
        return when(this) {
            QUICK -> "< 5 min"
            MEDIUM -> "5-10 min"
            LONG -> "> 10 min"
        }
    }
}
