package com.cocktailcraft.android.ui.preview

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.CocktailIngredient
import com.cocktailcraft.domain.model.Review

/**
 * Representative fake state for design-time previews. Never referenced from
 * production code paths.
 */
internal object PreviewData {

    val cocktail = Cocktail(
        id = "11007",
        name = "Margarita",
        category = "Ordinary Drink",
        alcoholic = "Alcoholic",
        glass = "Cocktail glass",
        instructions = "Rub the rim of the glass with the lime slice to make the salt " +
            "stick to it. Shake the other ingredients with ice, then carefully pour " +
            "into the glass.",
        imageUrl = null,
        ingredients = listOf(
            CocktailIngredient("Tequila", "1 1/2 oz"),
            CocktailIngredient("Triple sec", "1/2 oz"),
            CocktailIngredient("Lime juice", "1 oz"),
            CocktailIngredient("Salt", "pinch")
        ),
        price = 12.5,
        stockCount = 8,
        dateAdded = 0L
    )

    val outOfStockCocktail = cocktail.copy(
        id = "11008",
        name = "Manhattan",
        stockCount = 0
    )

    val cocktails = listOf(cocktail, outOfStockCocktail)

    val cartItem = CocktailCartItem(cocktail = cocktail, quantity = 2)

    val reviews = listOf(
        Review(
            id = "preview-1",
            cocktailId = cocktail.id,
            userName = "Alex",
            rating = 4.5f,
            comment = "Perfectly balanced — the fresh lime makes all the difference.",
            date = "2026-06-30"
        ),
        Review(
            id = "preview-2",
            cocktailId = cocktail.id,
            userName = "",
            rating = 3f,
            comment = "",
            date = "2026-07-02"
        )
    )
}
