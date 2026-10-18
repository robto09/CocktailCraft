package com.cocktailcraft.domain.model

import platform.Foundation.NSDecimalNumber
import platform.Foundation.NSLocale
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.Foundation.currentLocale
import platform.Foundation.localeIdentifier
import kotlin.native.concurrent.ThreadLocal

/** The formatter for the locale last used on this thread (formatter setup isn't cheap). */
@ThreadLocal
private object UsdFormatterCache {
    var localeIdentifier: String? = null
    var formatter: NSNumberFormatter? = null
}

private fun usdFormatter(locale: NSLocale): NSNumberFormatter {
    val identifier = locale.localeIdentifier
    UsdFormatterCache.formatter?.takeIf { UsdFormatterCache.localeIdentifier == identifier }?.let { return it }
    val formatter = NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterCurrencyStyle
        currencyCode = "USD"
        this.locale = locale
        // Plain "$" in every region, as the iOS app always showed: outside
        // the US the locale would otherwise write "US$8.24"
        currencySymbol = "$"
    }
    UsdFormatterCache.localeIdentifier = identifier
    UsdFormatterCache.formatter = formatter
    return formatter
}

internal actual fun formatUsd(cents: Long, languageTag: String?): String {
    val locale = languageTag?.let { NSLocale(localeIdentifier = it) } ?: NSLocale.currentLocale
    // NSDecimalNumber: the exact amount, never a binary fraction of a cent
    val amount = NSDecimalNumber(
        mantissa = (if (cents < 0) -cents else cents).toULong(),
        exponent = -2,
        isNegative = cents < 0
    )
    return usdFormatter(locale).stringFromNumber(amount) ?: "$" + amount.stringValue
}
