package com.cocktailcraft.domain.model

/**
 * Canonical drink categories from TheCocktailDB, curated for browsing UI.
 * Single source of truth for both platforms' category chips — do not
 * duplicate this list in platform code.
 */
object CocktailCategories {
    /** The API's default category, used as the browse/pagination fallback. */
    const val DEFAULT = "Cocktail"

    val CURATED: List<String> = listOf(
        DEFAULT,
        "Shot",
        "Ordinary Drink",
        "Coffee / Tea",
        "Homemade Liqueur",
        "Punch / Party Drink",
        "Beer",
        "Soft Drink"
    )
}
