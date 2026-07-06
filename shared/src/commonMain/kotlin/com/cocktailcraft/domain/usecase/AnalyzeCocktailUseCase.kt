package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.NutritionFacts

/**
 * Ingredient categorization and nutrition estimation for a cocktail.
 * Domain logic that previously lived in the detail presentation layer —
 * keyword-based heuristics over the ingredient list.
 */
internal class AnalyzeCocktailUseCase {

    fun categorizeIngredient(ingredientName: String): String {
        val name = ingredientName.lowercase()
        return when {
            name.contains("rum") || name.contains("vodka") || name.contains("gin") ||
            name.contains("whiskey") || name.contains("tequila") || name.contains("bourbon") -> "Spirits"
            name.contains("juice") || name.contains("lime") || name.contains("lemon") ||
            name.contains("orange") -> "Citrus & Juices"
            name.contains("syrup") || name.contains("sugar") || name.contains("honey") -> "Sweeteners"
            name.contains("bitters") || name.contains("salt") || name.contains("pepper") -> "Seasonings"
            else -> "Other"
        }
    }

    fun ingredientsByType(cocktail: Cocktail): Map<String, List<String>> {
        val grouped = mutableMapOf<String, MutableList<String>>()
        cocktail.ingredients.forEach { ingredient ->
            val type = categorizeIngredient(ingredient.name)
            grouped.getOrPut(type) { mutableListOf() }.add(ingredient.name)
        }
        return grouped.mapValues { entry -> entry.value.toList() }
    }

    fun nutritionFacts(cocktail: Cocktail): NutritionFacts {
        var calories = 0
        var alcohol = 0.0

        cocktail.ingredients.forEach { ingredient ->
            val name = ingredient.name.lowercase()
            when {
                name.contains("rum") || name.contains("vodka") || name.contains("gin") ||
                name.contains("whiskey") || name.contains("tequila") -> {
                    calories += 65 // ~65 calories per oz of spirits
                    alcohol += 14.0 // ~14g alcohol per oz
                }
                name.contains("juice") -> calories += 15 // ~15 calories per oz of juice
                name.contains("syrup") || name.contains("sugar") -> calories += 25 // ~25 calories per oz
            }
        }

        return NutritionFacts(
            calories = calories,
            alcoholGrams = alcohol.toInt(),
            carbsGrams = (calories * 0.1).toInt(),
            sugarGrams = (calories * 0.15).toInt()
        )
    }
}
