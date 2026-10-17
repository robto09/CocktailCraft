package com.cocktailcraft.domain.model

import kotlin.native.HiddenFromObjC
import kotlinx.serialization.Serializable

@Serializable
data class CocktailCartItem(
    val cocktail: Cocktail,
    val quantity: Int
) {
    // Hidden so the generated serializer does not drag the
    // kotlinx-serialization type tree into the Obj-C header.
    @HiddenFromObjC
    companion object
}