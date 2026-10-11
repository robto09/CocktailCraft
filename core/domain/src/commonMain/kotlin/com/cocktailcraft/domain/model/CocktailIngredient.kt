package com.cocktailcraft.domain.model

import kotlin.native.HiddenFromObjC
import kotlinx.serialization.Serializable

@Serializable
data class CocktailIngredient(
    val name: String,
    val measure: String
) {
    // Hidden so the generated serializer does not drag the
    // kotlinx-serialization type tree into the Obj-C header.
    @HiddenFromObjC
    companion object
}