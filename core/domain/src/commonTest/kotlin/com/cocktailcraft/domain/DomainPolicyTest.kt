package com.cocktailcraft.domain

import com.cocktailcraft.domain.config.DeliveryPolicy
import com.cocktailcraft.domain.model.Money
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
    fun thresholdAndFeeAreFiftyDollarsAndFiveNinetyNine() {
        assertEquals(Money(5000), DeliveryPolicy.FREE_DELIVERY_THRESHOLD)
        assertEquals(Money(599), DeliveryPolicy.STANDARD_FEE)
    }

    @Test
    fun feeIsWaivedAtThresholdAndChargedBelow() {
        // One cent either side of the threshold: no floating-point fuzz
        assertFalse(DeliveryPolicy.isFreeDelivery(Money(4999)))
        assertTrue(DeliveryPolicy.isFreeDelivery(Money(5000)))
        assertTrue(DeliveryPolicy.isFreeDelivery(Money(5001)))
        assertEquals(Money(599), DeliveryPolicy.deliveryFee(Money(1000)))
        assertEquals(Money(599), DeliveryPolicy.deliveryFee(Money(4999)))
        assertEquals(Money.ZERO, DeliveryPolicy.deliveryFee(Money(5000)))
        assertEquals(Money.ZERO, DeliveryPolicy.deliveryFee(Money(7500)))
    }

    @Test
    fun finalTotalAddsTheFeeInWholeCents() {
        assertEquals(Money(1599), DeliveryPolicy.finalTotal(Money(1000)))
        // 49.99 + 5.99 = 55.98 exactly, with no binary-fraction remainder
        assertEquals(Money(5598), DeliveryPolicy.finalTotal(Money(4999)))
        assertEquals(Money(7500), DeliveryPolicy.finalTotal(Money(7500)))
    }

    @Test
    fun emptyCartHasTheStandardFee() {
        assertEquals(Money(599), DeliveryPolicy.finalTotal(Money.ZERO))
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
