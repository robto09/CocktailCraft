package com.cocktailcraft.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.cocktailcraft.data.repository.CocktailRepository

/**
 * A utility class for loading filter options from the repository.
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
     * Loads filter options from the repository and returns them as a state.
     *
     * @param repository The cocktail repository to load options from
     * @param defaultCategories The default categories to use if loading fails
     * @param defaultIngredients The default ingredients to use if loading fails
     * @param defaultGlasses The default glasses to use if loading fails
     * @return A FilterOptions object containing the loaded options
     */
    @Composable
    fun rememberFilterOptions(
        repository: CocktailRepository,
        defaultCategories: List<String> = listOf(
            "Cocktail", "Ordinary Drink", "Shot", "Coffee / Tea",
            "Punch / Party Drink", "Homemade Liqueur", "Beer", "Soft Drink"
        ),
        defaultIngredients: List<String> = emptyList(),
        defaultGlasses: List<String> = emptyList()
    ): FilterOptions {
        var categories by remember { mutableStateOf(defaultCategories) }
        var ingredients by remember { mutableStateOf(defaultIngredients) }
        var glasses by remember { mutableStateOf(defaultGlasses) }
        
        LaunchedEffect(Unit) {
            try {
                repository.getCategories().collect { categoryList ->
                    categories = categoryList
                }
                
                repository.getIngredients().collect { ingredientList ->
                    ingredients = ingredientList
                }
                
                repository.getGlasses().collect { glassList ->
                    glasses = glassList
                }
            } catch (e: Exception) {
                // Use default values if loading fails
            }
        }
        
        return FilterOptions(
            categories = categories,
            ingredients = ingredients,
            glasses = glasses
        )
    }
}
