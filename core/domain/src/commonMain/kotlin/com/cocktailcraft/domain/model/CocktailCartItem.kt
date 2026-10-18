package com.cocktailcraft.domain.model

import kotlin.native.HiddenFromObjC
import kotlinx.serialization.Serializable

@Serializable
data class CocktailCartItem(
    val cocktail: Cocktail,
    val quantity: Int
) {
    /** The cocktail's price times [quantity]. */
    val lineTotal: Money
        get() = cocktail.price * quantity

    // Hidden so the generated serializer does not drag the
    // kotlinx-serialization type tree into the Obj-C header.
    @HiddenFromObjC
    companion object
}