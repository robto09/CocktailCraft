package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.CocktailIngredient
import com.cocktailcraft.domain.model.NutritionFacts
import com.cocktailcraft.testutil.testCocktail
import kotlin.test.Test
import kotlin.test.assertEquals

class AnalyzeCocktailUseCaseTest {

    private val useCase = AnalyzeCocktailUseCase()

    private fun cocktailWith(vararg ingredientNames: String) =
        testCocktail("1").copy(ingredients = ingredientNames.map { CocktailIngredient(it, "1 oz") })

    @Test
    fun categorizeIngredientMatchesKeywordBuckets() {
        assertEquals("Spirits", useCase.categorizeIngredient("White Rum"))
        assertEquals("Spirits", useCase.categorizeIngredient("Bourbon"))
        assertEquals("Citrus & Juices", useCase.categorizeIngredient("Lime juice"))
        assertEquals("Citrus & Juices", useCase.categorizeIngredient("Orange peel"))
        assertEquals("Sweeteners", useCase.categorizeIngredient("Simple Syrup"))
        assertEquals("Sweeteners", useCase.categorizeIngredient("Honey"))
        assertEquals("Seasonings", useCase.categorizeIngredient("Angostura Bitters"))
        assertEquals("Seasonings", useCase.categorizeIngredient("Salt"))
        assertEquals("Other", useCase.categorizeIngredient("Mint"))
        assertEquals("Other", useCase.categorizeIngredient("Cola"))
    }

    @Test
    fun ingredientsByTypeGroupsNamesUnderTheirBuckets() {
        val cocktail = cocktailWith("Gin", "Lime juice", "Sugar syrup", "Cola")

        val grouped = useCase.ingredientsByType(cocktail)

        assertEquals(
            mapOf(
                "Spirits" to listOf("Gin"),
                "Citrus & Juices" to listOf("Lime juice"),
                "Sweeteners" to listOf("Sugar syrup"),
                "Other" to listOf("Cola")
            ),
            grouped
        )
    }

    @Test
    fun nutritionFactsSumPerIngredientEstimates() {
        // Rum: 65 kcal + 14 g alcohol; juice: 15 kcal; syrup: 25 kcal.
        val cocktail = cocktailWith("White Rum", "Lime juice", "Sugar syrup")

        val facts = useCase.nutritionFacts(cocktail)

        assertEquals(
            NutritionFacts(
                calories = 105,
                alcoholGrams = 14,
                carbsGrams = 10, // 10% of calories, truncated
                sugarGrams = 15 // 15% of calories, truncated
            ),
            facts
        )
    }

    @Test
    fun nutritionFactsAreZeroWhenNoIngredientMatchesAKeyword() {
        val facts = useCase.nutritionFacts(cocktailWith("Cola", "Mint"))

        assertEquals(NutritionFacts(calories = 0, alcoholGrams = 0, carbsGrams = 0, sugarGrams = 0), facts)
    }
}
