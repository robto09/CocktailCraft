package com.cocktailcraft.domain

import com.cocktailcraft.domain.config.DeliveryPolicy
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

// AnalyzeCocktailUseCase coverage moved to
// domain/usecase/AnalyzeCocktailUseCaseTest.kt alongside the other use-case
// tests (AR-5), which supersedes the class that used to live here.
