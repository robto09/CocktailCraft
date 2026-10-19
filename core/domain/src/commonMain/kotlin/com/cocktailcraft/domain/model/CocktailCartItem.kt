package com.cocktailcraft.domain.model

data class CocktailCartItem(val cocktail: Cocktail, val quantity: Int) {
    /** The cocktail's price times [quantity]. */
    val lineTotal: Money
        get() = cocktail.price * quantity
}