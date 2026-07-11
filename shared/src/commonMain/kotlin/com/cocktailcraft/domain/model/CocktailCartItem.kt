package com.cocktailcraft.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CocktailCartItem(
    val cocktail: Cocktail,
    val quantity: Int
) 