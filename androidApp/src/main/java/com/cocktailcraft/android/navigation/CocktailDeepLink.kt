package com.cocktailcraft.android.navigation

// TheCocktailDB ids are short decimal numbers (e.g. "11007"). The cocktail
// detail deep link can be fired by other apps, so anything else in the id
// slot is dropped before it reaches the detail screen. ASCII digits only
// ([0-9], not \d) to match iOS's WidgetDeepLink exactly.
private val COCKTAIL_ID_PATTERN = Regex("^[0-9]{1,10}$")

/** True when [cocktailId] is a well-formed TheCocktailDB cocktail id. */
fun isValidCocktailId(cocktailId: String): Boolean = COCKTAIL_ID_PATTERN.matches(cocktailId)
