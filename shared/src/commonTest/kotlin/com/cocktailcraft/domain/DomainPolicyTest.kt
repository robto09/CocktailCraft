package com.cocktailcraft.domain

import com.cocktailcraft.domain.config.DeliveryPolicy
import com.cocktailcraft.domain.usecase.AnalyzeCocktailUseCase
import com.cocktailcraft.testutil.testCocktail
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeliveryPolicyTest {

    @Test
    fun feeIsWaivedAtThresholdAndChargedBelow() {
        assertFalse(DeliveryPolicy.isFreeDelivery(49.99))
        assertTrue(DeliveryPolicy.isFreeDelivery(50.0))
        assertEquals(DeliveryPolicy.STANDARD_FEE, DeliveryPolicy.deliveryFee(10.0))
        assertEquals(0.0, DeliveryPolicy.deliveryFee(75.0))
        assertEquals(10.0 + DeliveryPolicy.STANDARD_FEE, DeliveryPolicy.finalTotal(10.0))
        assertEquals(75.0, DeliveryPolicy.finalTotal(75.0))
    }

    @Test
    fun estimatedTimeScalesWithItemCount() {
        assertEquals("No items", DeliveryPolicy.estimatedDeliveryTime(0))
        assertEquals("15-20 minutes", DeliveryPolicy.estimatedDeliveryTime(3))
        assertEquals("20-25 minutes", DeliveryPolicy.estimatedDeliveryTime(6))
        assertEquals("25-30 minutes", DeliveryPolicy.estimatedDeliveryTime(7))
    }

    @Test
    fun statusBasedEstimateCoversAllStatuses() {
        assertEquals("Delivered", DeliveryPolicy.estimatedDeliveryTimeForStatus("DELIVERED"))
        assertEquals("15-25 minutes", DeliveryPolicy.estimatedDeliveryTimeForStatus("processing"))
        assertEquals("Unknown", DeliveryPolicy.estimatedDeliveryTimeForStatus(null))
    }
}

class AnalyzeCocktailUseCaseTest {

    private val useCase = AnalyzeCocktailUseCase()

    @Test
    fun categorizesIngredientsByKeyword() {
        assertEquals("Spirits", useCase.categorizeIngredient("White Rum"))
        assertEquals("Citrus & Juices", useCase.categorizeIngredient("Lime juice"))
        assertEquals("Sweeteners", useCase.categorizeIngredient("Simple Syrup"))
        assertEquals("Seasonings", useCase.categorizeIngredient("Angostura Bitters"))
        assertEquals("Other", useCase.categorizeIngredient("Mint"))
    }

    @Test
    fun groupsIngredientsByType() {
        val grouped = useCase.ingredientsByType(testCocktail("1"))
        assertEquals(listOf("Gin"), grouped["Spirits"])
    }

    @Test
    fun estimatesNutritionFromIngredients() {
        val facts = useCase.nutritionFacts(testCocktail("1")) // one spirit ingredient
        assertEquals(65, facts.calories)
        assertEquals(14, facts.alcoholGrams)
    }
}
