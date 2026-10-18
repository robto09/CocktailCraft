package com.cocktailcraft.domain

import com.cocktailcraft.domain.config.DeliveryPolicy
import com.cocktailcraft.domain.model.OrderStatus
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.common_unknown
import com.cocktailcraft.resources.delivery_estimate_minutes
import com.cocktailcraft.resources.delivery_estimate_no_items
import com.cocktailcraft.resources.order_status_delivered
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format
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
        assertEquals(MR.strings.delivery_estimate_no_items.desc(), DeliveryPolicy.estimatedDeliveryTime(0))
        assertEquals(MR.strings.delivery_estimate_minutes.format(15, 20), DeliveryPolicy.estimatedDeliveryTime(3))
        assertEquals(MR.strings.delivery_estimate_minutes.format(20, 25), DeliveryPolicy.estimatedDeliveryTime(6))
        assertEquals(MR.strings.delivery_estimate_minutes.format(25, 30), DeliveryPolicy.estimatedDeliveryTime(7))
    }

    @Test
    fun statusBasedEstimateCoversAllStatuses() {
        assertEquals(MR.strings.order_status_delivered.desc(), DeliveryPolicy.estimatedDeliveryTimeForStatus(OrderStatus.DELIVERED))
        assertEquals(MR.strings.delivery_estimate_minutes.format(15, 25), DeliveryPolicy.estimatedDeliveryTimeForStatus(OrderStatus.PROCESSING))
        assertEquals(MR.strings.common_unknown.desc(), DeliveryPolicy.estimatedDeliveryTimeForStatus(null))
    }
}
