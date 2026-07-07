package com.cocktailcraft.data.repository

import com.cocktailcraft.domain.repository.CocktailCatalogRepository
import com.cocktailcraft.domain.repository.CocktailDetailRepository
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.domain.repository.CocktailSearchRepository

/**
 * Thin composite over the five focused repository implementations, wired
 * purely through interface delegation.
 *
 * Kept only because the iOS `KoinHelper` exports the combined
 * [CocktailRepository] surface to Swift; Kotlin consumers should inject the
 * focused interfaces instead.
 */
internal class CocktailRepositoryImpl(
    searchRepository: CocktailSearchRepository,
    detailRepository: CocktailDetailRepository,
    catalogRepository: CocktailCatalogRepository,
    favoritesRepository: CocktailFavoritesRepository,
    offlineRepository: CocktailOfflineRepository
) : CocktailRepository,
    CocktailSearchRepository by searchRepository,
    CocktailDetailRepository by detailRepository,
    CocktailCatalogRepository by catalogRepository,
    CocktailFavoritesRepository by favoritesRepository,
    CocktailOfflineRepository by offlineRepository
