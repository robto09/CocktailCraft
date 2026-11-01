package com.cocktailcraft.data.local

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer

// The JSON shape cocktail payloads had before money moved to cents: prices
// in floating-point dollars. Database rows written then (and carried over
// by migrations/4.sqm, which converts only the columns) still hold it, so
// CocktailCodec falls back to it; never written.

/**
 * Until money moved to cents, payloads stored amounts as floating-point
 * dollars under [dollarsKey] (e.g. `"price": 12.5`); they now store whole
 * cents under [centsKey] (`"priceCents": 1250`). Rewrites an old object
 * into the new shape so stored payloads written by earlier versions keep
 * decoding with the same amounts. An object that already has [centsKey] is
 * left as it is.
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
    if (dollars != null) rewritten[centsKey] = JsonPrimitive(legacyDollarsToCents(dollars))
    return JsonObject(rewritten)
}

private const val CentsPerDollar = 100
private const val Half = 0.5

/**
 * Converts a legacy floating-point dollar amount (what the app stored before
 * money was kept in cents) to cents, rounding half away from zero exactly
 * like SQLite's ROUND() in migrations/4.sqm, so a price read from an old JSON
 * payload matches the same price migrated in a database column.
 */
internal fun legacyDollarsToCents(dollars: Double): Long {
    val scaled = dollars * CentsPerDollar
    return (scaled + if (scaled < 0) -Half else Half).toLong()
}

/** A [CocktailEntity] payload, also reading the pre-cents shape (`"price": 12.5` dollars). */
internal object LegacyCocktailSerializer : JsonTransformingSerializer<CocktailEntity>(CocktailEntity.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement =
        element.withLegacyDollarsAsCents(dollarsKey = "price", centsKey = "priceCents")
}
