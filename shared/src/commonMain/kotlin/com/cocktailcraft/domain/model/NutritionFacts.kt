package com.cocktailcraft.domain.model

/**
 * Estimated nutrition facts for a cocktail, typed so the Swift surface gets
 * real fields instead of a [String: String] dictionary. Values are rough
 * keyword-based estimates, not measured data.
 */
data class NutritionFacts(
    val calories: Int,
    val alcoholGrams: Int,
    val carbsGrams: Int,
    val sugarGrams: Int
)
