package com.cocktailcraft.domain.model

import kotlin.native.HiddenFromObjC
import kotlinx.serialization.Serializable

@Serializable
data class CocktailIngredient(
    val name: String,
    val measure: String
) {
    /**
     * "1 oz Vodka": measure first, as recipes are written, or just the name
     * when there is no measure ("Salt"). One format for both detail screens
     * and the share text.
     */
    val displayText: String
        get() = listOf(measure, name).filter { it.isNotBlank() }.joinToString(" ")

    // Hidden so the generated serializer does not drag the
    // kotlinx-serialization type tree into the Obj-C header.
    @HiddenFromObjC
    companion object
}