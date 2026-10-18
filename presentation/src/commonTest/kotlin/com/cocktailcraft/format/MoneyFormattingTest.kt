package com.cocktailcraft.format

import com.cocktailcraft.domain.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Runs on both platforms' formatters: they must agree. */
class MoneyFormattingTest {

    @Test
    fun formatsUsDollarsForUsEnglish() {
        assertEquals("$12.50", Money(1250).formatted("en-US"))
        assertEquals("$0.00", Money.ZERO.formatted("en-US"))
        assertEquals("$0.05", Money(5).formatted("en-US"))
        assertEquals("$1,234,567.89", Money(123_456_789).formatted("en-US"))
    }

    @Test
    fun formatsNegativeAmountsWithASign() {
        val formatted = Money(-599).formatted("en-US")

        assertTrue(formatted.contains("5.99") && formatted.contains("$"), formatted)
        assertTrue(formatted.startsWith("-") || formatted.startsWith("(") || formatted.startsWith("−"), formatted)
    }

    @Test
    fun followsTheLocaleNumberStyleButStaysInDollars() {
        val german = Money(123_450).formatted("de-DE")

        // 1.234,50 with a dollar sign, whatever the exact spacing
        assertTrue(german.contains("1.234,50"), german)
        assertTrue(german.contains("$"), german)
        assertFalse(german.contains("€"), german)
    }

    @Test
    fun showsAPlainDollarSignOutsideTheUs() {
        // Android's locale symbol used to write "US$" here, iOS "$"
        listOf("en-GB", "en-AU", "de-DE", "fr-FR", "ja-JP").forEach { tag ->
            val formatted = Money(824).formatted(tag)
            assertTrue(formatted.contains("$"), "$tag: $formatted")
            assertFalse(formatted.contains("US$"), "$tag: $formatted")
        }
        assertEquals("$8.24", Money(824).formatted("en-GB"))
    }

    @Test
    fun formattedUsesTheCurrentLocale() {
        val formatted = Money(1250).formatted()

        assertTrue(formatted.contains("12") && formatted.contains("50"), formatted)
    }
}
