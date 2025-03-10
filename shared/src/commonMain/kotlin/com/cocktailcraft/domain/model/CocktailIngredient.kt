package com.cocktailcraft.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CocktailIngredient(
    val name: String,
    val measure: String
) 