package com.cocktailcraft.domain.repository

/**
 * Composite repository interface that combines all cocktail repository capabilities.
 * Extends the 5 focused interfaces for backward compatibility.
 *
 * Prefer injecting the narrower interfaces (CocktailSearchRepository, CocktailDetailRepository,
 * CocktailCatalogRepository, CocktailFavoritesRepository, CocktailOfflineRepository) when a
 * consumer only needs a subset of operations.
 */
interface CocktailRepository :
    CocktailSearchRepository,
    CocktailDetailRepository,
    CocktailCatalogRepository,
    CocktailFavoritesRepository,
    CocktailOfflineRepository