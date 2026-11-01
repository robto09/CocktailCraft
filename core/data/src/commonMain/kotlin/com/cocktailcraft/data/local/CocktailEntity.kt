package com.cocktailcraft.data.local

import co.touchlab.kermit.Logger
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailIngredient
import com.cocktailcraft.domain.model.Money
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * A cocktail as the database stores it: the JSON payload of a
 * cached_cocktail row. The same shape the domain Cocktail was serialized in
 * before the data layer had its own type, so every payload already stored
 * reads back unchanged. Whether a record is partial is not part of the
 * payload; see [CocktailCodec].
 */
@Serializable
internal data class CocktailEntity(
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
    val ingredients: List<IngredientEntity> = emptyList(),
    val imageSource: String? = null,
    val imageAttribution: String? = null,
    val creativeCommonsConfirmed: Boolean? = null,
    val dateModified: String? = null,
    // Payloads written before money was in cents hold "price" in dollars
    // instead; see CocktailCodec and LegacyCocktailSerializer
    val priceCents: Long = DEFAULT_PRICE_CENTS,
    val inStock: Boolean = true,
    val stockCount: Int = 50,
    val rating: Float = 4.5f,
    val popularity: Int = 0,
    val dateAdded: Long = 0L
) {
    /**
     * Whether this payload holds full details, judged the way payloads
     * written before the explicit flag existed encoded it: a list endpoint's
     * record had no ingredients, or the [LEGACY_PLACEHOLDER_INGREDIENT].
     */
    val looksFull: Boolean
        get() = ingredients.isNotEmpty() && ingredients.first().name != LEGACY_PLACEHOLDER_INGREDIENT

    companion object {
        private val DEFAULT_PRICE_CENTS = Cocktail.DEFAULT_PRICE.cents

        /**
         * What older versions stored as the ingredient list (and the
         * instructions) of a list endpoint's partial record. Only read back,
         * and stripped when decoding: the domain model flags partial
         * records explicitly.
         */
        const val LEGACY_PLACEHOLDER_INGREDIENT = "Tap to view ingredients"
        const val LEGACY_PLACEHOLDER_INSTRUCTIONS = "Tap to view recipe"
    }
}

@Serializable
internal data class IngredientEntity(val name: String, val measure: String)

internal fun Cocktail.toEntity(): CocktailEntity = CocktailEntity(
    id = id,
    name = name,
    alternateName = alternateName,
    tags = tags,
    category = category,
    iba = iba,
    alcoholic = alcoholic,
    glass = glass,
    instructions = instructions,
    imageUrl = imageUrl,
    ingredients = ingredients.map { IngredientEntity(it.name, it.measure) },
    imageSource = imageSource,
    imageAttribution = imageAttribution,
    creativeCommonsConfirmed = creativeCommonsConfirmed,
    dateModified = dateModified,
    priceCents = price.cents,
    inStock = inStock,
    stockCount = stockCount,
    rating = rating,
    popularity = popularity,
    dateAdded = dateAdded
)

/**
 * The domain cocktail for this payload. A partial record ([hasFullDetails]
 * false) drops the placeholder ingredient and instructions older versions
 * stored in it.
 */
internal fun CocktailEntity.toDomain(hasFullDetails: Boolean = looksFull): Cocktail = Cocktail(
    id = id,
    name = name,
    alternateName = alternateName,
    tags = tags,
    category = category,
    iba = iba,
    alcoholic = alcoholic,
    glass = glass,
    instructions = if (hasFullDetails) {
        instructions
    } else {
        instructions?.takeUnless { it == CocktailEntity.LEGACY_PLACEHOLDER_INSTRUCTIONS }
    },
    imageUrl = imageUrl,
    ingredients = ingredients
        .filter { hasFullDetails || it.name != CocktailEntity.LEGACY_PLACEHOLDER_INGREDIENT }
        .map { CocktailIngredient(it.name, it.measure) },
    imageSource = imageSource,
    imageAttribution = imageAttribution,
    creativeCommonsConfirmed = creativeCommonsConfirmed,
    dateModified = dateModified,
    price = Money(priceCents),
    inStock = inStock,
    stockCount = stockCount,
    rating = rating,
    popularity = popularity,
    dateAdded = dateAdded,
    hasFullDetails = hasFullDetails
)

/**
 * Encodes cocktails as [CocktailEntity] payloads and decodes them back.
 *
 * Decoding takes one pass with the generated serializer. Only a payload
 * still holding the pre-cents `"price"` key (written before money moved to
 * cents) goes through [LegacyCocktailSerializer], which parses it into a
 * JsonElement tree to rewrite the dollars first.
 */
internal class CocktailCodec(private val json: Json) {

    fun encode(cocktail: Cocktail): String = json.encodeToString(CocktailEntity.serializer(), cocktail.toEntity())

    /**
     * The cocktail in [payload]. [hasFullDetails] is the stored flag; null
     * (data written before it was stored) judges by the payload itself
     * ([CocktailEntity.looksFull]).
     */
    fun decode(payload: String, hasFullDetails: Boolean? = null): Cocktail {
        val entity = if (LEGACY_PRICE_KEY in payload) {
            json.decodeFromString(LegacyCocktailSerializer, payload)
        } else {
            json.decodeFromString(CocktailEntity.serializer(), payload)
        }
        return entity.toDomain(hasFullDetails ?: entity.looksFull)
    }

    /** [decode], or null (logged) when [payload] can't be read. */
    fun decodeOrNull(payload: String, hasFullDetails: Boolean? = null): Cocktail? =
        try {
            decode(payload, hasFullDetails)
        } catch (e: IllegalArgumentException) {
            // SerializationException included
            Logger.w(e) { "Unreadable stored cocktail: ${e.message}" }
            null
        }

    private companion object {
        // Compact JSON as this app writes it; "priceCents" doesn't match
        const val LEGACY_PRICE_KEY = "\"price\":"
    }
}
