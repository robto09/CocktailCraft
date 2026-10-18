package com.cocktailcraft.android.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import java.text.NumberFormat
import java.util.Currency

/**
 * Formatter for prices: US dollars laid out for the user's locale, like the
 * iOS `asPrice` helper. Built once per locale instead of on every
 * recomposition (NumberFormat construction isn't cheap).
 */
@Composable
fun rememberCurrencyFormatter(): NumberFormat {
    val locale = LocalConfiguration.current.locales[0]
    return remember(locale) {
        NumberFormat.getCurrencyInstance(locale).apply {
            currency = Currency.getInstance("USD")
        }
    }
}
