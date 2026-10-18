package com.cocktailcraft.domain.model

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

// NumberFormat construction isn't cheap, so one per locale is kept. They are
// not thread-safe: each is used under its own lock.
private val usdFormats = ConcurrentHashMap<Locale, NumberFormat>()

private val USD: Currency = Currency.getInstance("USD")

private const val CentsScale = 2

internal actual fun formatUsd(cents: Long, languageTag: String?): String {
    val locale = languageTag?.let(Locale::forLanguageTag) ?: Locale.getDefault()
    val format = usdFormats.getOrPut(locale) {
        NumberFormat.getCurrencyInstance(locale).apply { currency = USD }
    }
    // BigDecimal: the exact amount, never a binary fraction of a cent
    val amount = BigDecimal.valueOf(cents, CentsScale)
    return synchronized(format) { format.format(amount) }
}

/**
 * [Money.formatted] for [locale]: Compose passes the configuration locale
 * so an in-app language change re-formats right away.
 */
fun Money.formatted(locale: Locale): String = formatted(locale.toLanguageTag())
