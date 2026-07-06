package com.cocktailcraft.testutil

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailIngredient

/** A cocktail with full details (real ingredients/instructions), as returned by lookup endpoints. */
fun testCocktail(
    id: String,
    name: String = "Cocktail $id",
    category: String? = "Cocktail",
    price: Double = 10.0,
    rating: Float = 4.5f,
    popularity: Int = 0,
    dateAdded: Long = 0L
) = Cocktail(
    id = id,
    name = name,
    category = category,
    alcoholic = "Alcoholic",
    glass = "Cocktail glass",
    instructions = "Stir well and strain.",
    imageUrl = "https://example.com/$id.jpg",
    ingredients = listOf(CocktailIngredient("Gin", "50 ml")),
    price = price,
    rating = rating,
    popularity = popularity,
    dateAdded = dateAdded
)

/**
 * A cocktail as returned by list/filter endpoints: placeholder ingredients and
 * instructions, so [Cocktail.hasFullDetails] is false and use cases must hydrate it.
 */
fun partialCocktail(id: String, name: String = "Partial $id") = Cocktail(
    id = id,
    name = name,
    category = "Cocktail",
    imageUrl = "https://example.com/$id.jpg",
    instructions = Cocktail.PLACEHOLDER_INSTRUCTIONS,
    ingredients = listOf(CocktailIngredient(Cocktail.PLACEHOLDER_INGREDIENT_NAME, ""))
)
