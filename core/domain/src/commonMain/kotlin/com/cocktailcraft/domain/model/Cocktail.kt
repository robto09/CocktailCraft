package com.cocktailcraft.domain.model

import kotlin.native.HiddenFromObjC
import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Serializable
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
    val price: Double = 10.0,
    val inStock: Boolean = true,
    val stockCount: Int = 50,
    val rating: Float = 4.5f,
    val popularity: Int = 0,
    val dateAdded: Long = Clock.System.now().toEpochMilliseconds()
) {
    /**
     * False when this instance came from a list endpoint, which fabricates
     * placeholder ingredients/instructions. Persisting such instances is
     * what put "Tap to view ingredients" into carts and favorites.
     */
    val hasFullDetails: Boolean
        get() = ingredients.isNotEmpty() &&
            ingredients.first().name != PLACEHOLDER_INGREDIENT_NAME

    // Hidden so the generated serializer does not drag the
    // kotlinx-serialization type tree into the Obj-C header.
    @HiddenFromObjC
    companion object {
        const val PLACEHOLDER_INGREDIENT_NAME = "Tap to view ingredients"
        const val PLACEHOLDER_INSTRUCTIONS = "Tap to view recipe"
    }
}