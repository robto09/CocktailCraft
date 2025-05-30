package com.cocktailcraft.util

import com.cocktailcraft.domain.repository.CocktailRepository
import kotlinx.coroutines.flow.firstOrNull

/**
 * A utility class for loading filter options from the repository.
 * This is the KMP-compatible version without Compose-specific dependencies.
 */
object FilterOptionsLoader {
    
    /**
     * A data class representing filter options.
     */
    data class FilterOptions(
        val categories: List<String>,
        val ingredients: List<String>,
        val glasses: List<String>
    )
    
    /**
     * Default categories if loading fails
     */
    val defaultCategories = listOf(
        "Cocktail", "Ordinary Drink", "Shot", "Coffee / Tea",
        "Punch / Party Drink", "Homemade Liqueur", "Beer", "Soft Drink"
    )
    
    /**
     * Loads filter options from the repository.
     *
     * @param repository The cocktail repository to load options from
     * @return A FilterOptions object containing the loaded options
     */
    suspend fun loadFilterOptions(
        repository: CocktailRepository
    ): FilterOptions {
        return try {
            val categories = repository.getCategories().firstOrNull() ?: defaultCategories
            val ingredients = repository.getIngredients().firstOrNull() ?: emptyList()
            val glasses = repository.getGlasses().firstOrNull() ?: emptyList()
            
            FilterOptions(
                categories = categories,
                ingredients = ingredients,
                glasses = glasses
            )
        } catch (e: Exception) {
            // Return default values if loading fails
            FilterOptions(
                categories = defaultCategories,
                ingredients = emptyList(),
                glasses = emptyList()
            )
        }
    }
}