package com.cocktailcraft.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class MoneyTest {

    @Test
    fun arithmeticIsExactInCents() {
        assertEquals(Money(30), Money(10) + Money(20))
        assertEquals(Money(401), Money(1000) - Money(599))
        assertEquals(Money(3747), Money(1249) * 3)
        assertEquals(Money.ZERO, Money(1249) * 0)
    }

    @Test
    fun sumOfMoneyAddsEverySelectedAmount() {
        val lines = listOf(Money(10) to 3, Money(1999) to 2, Money(1) to 1)

        assertEquals(Money(30 + 3998 + 1), lines.sumOfMoney { (price, quantity) -> price * quantity })
        assertEquals(Money.ZERO, emptyList<Money>().sumOfMoney { it })
    }

    @Test
    fun comparesAndOrdersByCents() {
        assertTrue(Money(4999) < Money(5000))
        assertTrue(Money(5000) >= Money(5000))
        assertEquals(listOf(Money(1), Money(500), Money(1500)), listOf(Money(1500), Money(1), Money(500)).sorted())
        assertEquals(Money.ZERO, Money(-25).coerceAtLeast(Money.ZERO))
        assertEquals(Money(25), Money(25).coerceAtLeast(Money.ZERO))
    }

    @Test
    fun equalAmountsAreEqual() {
        assertEquals(Money(1250), Money(1250))
        assertEquals(Money(1250).hashCode(), Money(1250).hashCode())
        assertNotEquals(Money(1250), Money(1251))
    }

    @Test
    fun legacyDollarsRoundHalfAwayFromZeroToTheNearestCent() {
        assertEquals(Money(1250), Money.fromDollars(12.5))
        assertEquals(Money(599), Money.fromDollars(5.99))
        // Binary doubles a hair below the cent still land on it
        assertEquals(Money(529), Money.fromDollars(5.0 + 29 / 100.0))
        assertEquals(Money(1015), Money.fromDollars(5.0 + 515 / 100.0))
        assertEquals(Money(13), Money.fromDollars(0.125))
        assertEquals(Money(-13), Money.fromDollars(-0.125))
        assertEquals(Money(0), Money.fromDollars(0.004))
        assertEquals(Money(1), Money.fromDollars(0.005))
    }
}
