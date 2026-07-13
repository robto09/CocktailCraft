package com.cocktailcraft.android.util

import com.cocktailcraft.domain.model.Cocktail

/**
 * O(1) favorite-membership lookups instead of a linear scan per row (AN-3).
 * Hoist once per screen with `remember(favorites) { favorites.toFavoriteIdSet() }`.
 */
fun List<Cocktail>.toFavoriteIdSet(): Set<String> = mapTo(HashSet(size)) { it.id }
