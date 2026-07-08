package com.cocktailcraft.android.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes (navigation-compose 2.8+). Arguments are
 * constructor parameters — no route-string templates or NavType parsing.
 */
@Serializable
data object HomeRoute

@Serializable
data object CartRoute

@Serializable
data object ProfileRoute

@Serializable
data object FavoritesRoute

@Serializable
data object OrderListRoute

@Serializable
data object OfflineModeRoute

@Serializable
data class CocktailDetailRoute(val cocktailId: String)

/**
 * Deep-link base for [CocktailDetailRoute]; the cocktail id is the final path
 * segment (`app://cocktailcraft/cocktail/{cocktailId}`). Must stay in step
 * with the VIEW intent filter in AndroidManifest.xml, which can't reference
 * this constant.
 */
const val COCKTAIL_DEEP_LINK_BASE_PATH = "app://cocktailcraft/cocktail"
