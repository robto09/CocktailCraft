package com.cocktailcraft.domain.model

import kotlinx.serialization.Serializable

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
    val dateAdded: Long = System.currentTimeMillis()
)
