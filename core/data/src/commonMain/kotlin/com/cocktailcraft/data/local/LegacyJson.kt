package com.cocktailcraft.data.local

import com.cocktailcraft.domain.model.Money
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderItem
import com.cocktailcraft.domain.model.OrderStatus
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer

// JSON shapes older app versions stored: payloads with prices in floating-
// point dollars, and the cart and orders they kept in Settings. Read once
// (the Settings import) or as a fallback (CocktailCodec); never written.

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

/** A cart line as the Settings cart blob ("cocktail_cart_items") held it. */
@Serializable
internal data class LegacyCartItem(
    @Serializable(with = LegacyCocktailSerializer::class)
    val cocktail: CocktailEntity,
    val quantity: Int
)

/**
 * An order as the Settings orders blob ("orders_data") held it, in either
 * the pre-cents shape (`"total": 33.99` dollars) or the cents one.
 */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = LegacyOrderSerializer::class)
internal data class LegacyOrder(
    val id: String,
    val date: String,
    val items: List<LegacyOrderItem>,
    val totalCents: Long,
    // OrderStatus.storageValue, e.g. "Processing"
    val status: String
) {
    fun toDomain() = Order(
        id = id,
        date = date,
        items = items.map { it.toDomain() },
        total = Money(totalCents),
        status = OrderStatus.fromStorageValue(status)
    )
}

@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = LegacyOrderItemSerializer::class)
internal data class LegacyOrderItem(
    val name: String,
    val quantity: Int,
    val priceCents: Long,
    val cocktailId: String? = null
) {
    fun toDomain() = OrderItem(name = name, quantity = quantity, price = Money(priceCents), cocktailId = cocktailId)
}

@OptIn(ExperimentalSerializationApi::class)
internal object LegacyOrderSerializer : JsonTransformingSerializer<LegacyOrder>(LegacyOrder.generatedSerializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement =
        element.withLegacyDollarsAsCents(dollarsKey = "total", centsKey = "totalCents")
}

@OptIn(ExperimentalSerializationApi::class)
internal object LegacyOrderItemSerializer :
    JsonTransformingSerializer<LegacyOrderItem>(LegacyOrderItem.generatedSerializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement =
        element.withLegacyDollarsAsCents(dollarsKey = "price", centsKey = "priceCents")
}
