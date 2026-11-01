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
data class CocktailDetailRoute(val cocktailId: String)

/** One order's detail screen, opened from the order list. */
@Serializable
data class OrderDetailRoute(val orderId: String)
