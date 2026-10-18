package com.cocktailcraft.domain.model

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Until money moved to cents, payloads stored amounts as floating-point
 * dollars under [dollarsKey] (e.g. `"price": 12.5`); they now store whole
 * cents under [centsKey] (`"priceCents": 1250`). Rewrites an old object
 * into the new shape so cart, favorite, cache and legacy Settings payloads
 * written by earlier versions keep decoding with the same amounts. An object
 * that already has [centsKey] is left as it is.
 */
internal fun JsonElement.withLegacyDollarsAsCents(dollarsKey: String, centsKey: String): JsonElement {
    if (this !is JsonObject || centsKey in this || dollarsKey !in this) return this
    // A null or unreadable amount falls back to the field's default, as
    // the lenient, coercing decoder did for the old Double field
    val dollars = (this[dollarsKey] as? JsonPrimitive)
        ?.takeUnless { it is JsonNull }
        ?.content
        ?.toDoubleOrNull()
    val rewritten = toMutableMap()
    rewritten.remove(dollarsKey)
    if (dollars != null) rewritten[centsKey] = JsonPrimitive(Money.fromDollars(dollars).cents)
    return JsonObject(rewritten)
}
