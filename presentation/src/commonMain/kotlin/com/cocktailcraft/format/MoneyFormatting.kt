package com.cocktailcraft.format

import com.cocktailcraft.domain.model.Money

/**
 * This amount as text — the one formatter both apps use (Android through
 * CurrencyFormatter, Swift as `money.formatted()`): US dollars with a plain
 * "$" in every region (never "US$"), laid out in the device's current
 * locale's number style: "$12.50" in en-US, "12,50 $" in de-DE.
 */
fun Money.formatted(): String = formatUsd(cents, languageTag = null)

/** Like [formatted], for the locale with the BCP 47 [languageTag] (e.g. "en-US"). */
fun Money.formatted(languageTag: String): String = formatUsd(cents, languageTag)

/** [Money.formatted] on each platform's own number formatter. */
internal expect fun formatUsd(cents: Long, languageTag: String?): String

/** The symbol every amount is shown with, whatever the locale. */
internal const val UsdSymbol = "$"
