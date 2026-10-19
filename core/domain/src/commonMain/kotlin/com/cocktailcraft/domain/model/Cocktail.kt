package com.cocktailcraft.domain.model

import kotlin.native.HiddenFromObjC

/**
 * A cocktail as the app shows it. A plain model: how it is stored (the
 * database's JSON payload, older payload shapes) is the data layer's
 * concern.
 */
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
    val price: Money = DEFAULT_PRICE,
    val inStock: Boolean = true,
    val stockCount: Int = 50,
    val rating: Float = 4.5f,
    val popularity: Int = 0,
    // Epoch millis, or 0 when unknown. TheCocktailDB supplies no such date;
    // the default used to be construction time, which made two mappings of
    // the same API record compare unequal. Kept (not removed) so platform
    // callers that pass it stay compatible.
    val dateAdded: Long = 0L,
    /**
     * False for a partial record: what a list endpoint (filter.php) returns,
     * just the id, name and thumbnail, with no [ingredients] or
     * [instructions]. Persisting a partial record where the full one is
     * needed is what put "Tap to view ingredients" into carts and favorites,
     * so use cases look the full one up first.
     */
    val hasFullDetails: Boolean = true
) {
    /**
     * The alcoholic label ("Alcoholic", "Non alcoholic", ...) when the API
     * supplied one, or null. List endpoints omit it and the mapper fills in
     * [ALCOHOLIC_UNKNOWN]; cards leave that out instead of repeating
     * "Unknown" on every row.
     */
    val knownAlcoholic: String?
        get() = alcoholic?.takeIf { it.isNotBlank() && it != ALCOHOLIC_UNKNOWN }

    // Kotlin-side constants; not part of the Swift API
    @HiddenFromObjC
    companion object {
        const val ALCOHOLIC_UNKNOWN = "Unknown"

        /** What a cocktail costs when nothing set a price ($10.00). */
        val DEFAULT_PRICE = Money(1000)
    }
}
