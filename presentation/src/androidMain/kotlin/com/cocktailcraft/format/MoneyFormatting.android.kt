package com.cocktailcraft.format

import com.cocktailcraft.domain.model.Money
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

// NumberFormat construction isn't cheap, so one per locale is kept. They are
// not thread-safe: each is used under its own lock.
private val usdFormats = ConcurrentHashMap<Locale, NumberFormat>()

private val USD: Currency = Currency.getInstance("USD")

private const val CentsScale = 2

private fun usdFormat(locale: Locale): NumberFormat =
    NumberFormat.getCurrencyInstance(locale).apply {
        currency = USD
        // Plain "$" in every region, like iOS: outside the US the locale
        // would otherwise write "US$8.24"
        (this as? DecimalFormat)?.let { format ->
            format.decimalFormatSymbols = format.decimalFormatSymbols.apply { currencySymbol = UsdSymbol }
        }
    }

internal actual fun formatUsd(cents: Long, languageTag: String?): String {
    val locale = languageTag?.let(Locale::forLanguageTag) ?: Locale.getDefault()
    val format = usdFormats.getOrPut(locale) { usdFormat(locale) }
    // BigDecimal: the exact amount, never a binary fraction of a cent
    val amount = BigDecimal.valueOf(cents, CentsScale)
    return synchronized(format) { format.format(amount) }
}

/**
 * [Money.formatted] for [locale]: Compose passes the configuration locale
 * so an in-app language change re-formats right away.
 */
fun Money.formatted(locale: Locale): String = formatted(locale.toLanguageTag())
