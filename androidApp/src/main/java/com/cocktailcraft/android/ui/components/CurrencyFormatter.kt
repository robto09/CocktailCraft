package com.cocktailcraft.android.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import com.cocktailcraft.domain.model.Money
import com.cocktailcraft.domain.model.formatted
import java.util.Locale

/**
 * Formats prices for one locale through the shared [Money] formatter: US
 * dollars laid out for the user's locale ("$12.50" in en-US, "12,50 $" in
 * de-DE), from the exact number of cents.
 */
@Immutable
class CurrencyFormatter internal constructor(private val locale: Locale) {
    fun format(amount: Money): String = amount.formatted(locale)
}

/**
 * The [CurrencyFormatter] for the configuration's locale, so an in-app
 * language change re-formats every price on screen.
 */
@Composable
fun rememberCurrencyFormatter(): CurrencyFormatter {
    val locale = LocalConfiguration.current.locales[0]
    return remember(locale) { CurrencyFormatter(locale) }
}

/** [amount] formatted for the configuration's locale, for a one-off price. */
@Composable
fun formatPrice(amount: Money): String = rememberCurrencyFormatter().format(amount)
