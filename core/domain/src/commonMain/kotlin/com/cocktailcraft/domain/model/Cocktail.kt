package com.cocktailcraft.domain.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlin.native.HiddenFromObjC

// CocktailSerializer wraps the generated serializer to read payloads stored
// before prices were in cents
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = CocktailSerializer::class)
data class Cocktail(
    val id: String,
    val name: String,
    val alternateName: String? = null,
    val tags: List<String>? = null,
    val category: String? = null,
    val iba: String? = null,
    val alcoholic: String? = null,
    val glass: String? = null,
    val instructions: String? = null,
    val imageUrl: String? = null,
    val ingredients: List<CocktailIngredient> = emptyList(),
    val imageSource: String? = null,
    val imageAttribution: String? = null,
    val creativeCommonsConfirmed: Boolean? = null,
    val dateModified: String? = null,
    // Stored as "priceCents"; payloads written before that hold "price" in
    // floating-point dollars, converted on read (see CocktailSerializer)
    @SerialName("priceCents")
    val price: Money = DEFAULT_PRICE,
    val inStock: Boolean = true,
    val stockCount: Int = 50,
    val rating: Float = 4.5f,
    val popularity: Int = 0,
    // Epoch millis, or 0 when unknown. TheCocktailDB supplies no such date;
    // the default used to be construction time, which made two mappings of
    // the same API record compare unequal. Kept (not removed) so cached
    // payloads and platform callers that pass it stay compatible.
    val dateAdded: Long = 0L
) {
    /**
     * False when this instance came from a list endpoint, which fabricates
     * placeholder ingredients/instructions. Persisting such instances is
     * what put "Tap to view ingredients" into carts and favorites.
     */
    val hasFullDetails: Boolean
        get() = ingredients.isNotEmpty() &&
            ingredients.first().name != PLACEHOLDER_INGREDIENT_NAME

    /**
     * The alcoholic label ("Alcoholic", "Non alcoholic", ...) when the API
     * supplied one, or null. List endpoints omit it and the mapper fills in
     * [ALCOHOLIC_UNKNOWN]; cards leave that out instead of repeating
     * "Unknown" on every row.
     */
    val knownAlcoholic: String?
        get() = alcoholic?.takeIf { it.isNotBlank() && it != ALCOHOLIC_UNKNOWN }

    // Hidden so the generated serializer does not drag the
    // kotlinx-serialization type tree into the Obj-C header.
    @HiddenFromObjC
    companion object {
        const val PLACEHOLDER_INGREDIENT_NAME = "Tap to view ingredients"
        const val PLACEHOLDER_INSTRUCTIONS = "Tap to view recipe"
        const val ALCOHOLIC_UNKNOWN = "Unknown"

        /** What a cocktail costs when nothing set a price ($10.00). */
        val DEFAULT_PRICE = Money(1000)
    }
}

/**
 * Cocktail JSON (cache, cart, favorites and recently viewed payloads, the
 * legacy Settings blobs) as the generated serializer writes it, also
 * reading the pre-cents shape (`"price": 12.5` dollars).
 */
@OptIn(ExperimentalSerializationApi::class)
internal object CocktailSerializer : JsonTransformingSerializer<Cocktail>(Cocktail.generatedSerializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement =
        element.withLegacyDollarsAsCents(dollarsKey = "price", centsKey = "priceCents")
}