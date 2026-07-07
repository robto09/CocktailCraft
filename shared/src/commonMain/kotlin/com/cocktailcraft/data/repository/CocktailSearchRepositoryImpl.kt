package com.cocktailcraft.data.repository

import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.domain.repository.CocktailSearchRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrThrow

/**
 * Search and filter operations over TheCocktailDB, extracted from the former
 * CocktailRepositoryImpl monolith. Category browsing shares the cached
 * fetch path in [CocktailCategoryFetcher] with the catalog repository.
 */
internal class CocktailSearchRepositoryImpl(
    private val remote: CocktailRemoteDataSource,
    private val cocktailCache: CocktailCache,
    private val offlineRepository: CocktailOfflineRepositoryImpl,
    private val categoryFetcher: CocktailCategoryFetcher
) : CocktailSearchRepository {

    private suspend fun isOffline(): Boolean = offlineRepository.isOffline()

    override suspend fun searchCocktailsByName(name: String): Result<List<Cocktail>> {
        return try {
            Result.Success(remote.searchByName(name))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to search cocktails by name")
        }
    }

    override suspend fun filterByIngredient(ingredient: String): Result<List<Cocktail>> {
        return try {
            Result.Success(remote.filterByIngredient(ingredient))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to filter by ingredient")
        }
    }

    override suspend fun filterByAlcoholic(alcoholic: Boolean): Result<List<Cocktail>> {
        return try {
            Result.Success(remote.filterByAlcoholic(alcoholic))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to filter by alcoholic")
        }
    }

    override suspend fun filterByCategory(category: String): Result<List<Cocktail>> {
        return try {
            Result.Success(categoryFetcher.fetchCocktailsByCategory(category))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to filter by category")
        }
    }

    /**
     * Advanced search over the 4 supported filters.
     *
     * TheCocktailDB cannot combine filters server-side, but every filter.php call
     * returns a complete id set for its dimension, so combining = intersecting id
     * sets client-side. One rate-limited call per active filter; the first
     * successful result (priority order below, preferring full name-search records)
     * is the base list, narrowed to ids present in every other active set. Any
     * active call failing surfaces as [Result.Error] — a filter is never silently
     * dropped.
     */
    override suspend fun advancedSearch(filters: SearchFilters): Result<List<Cocktail>> {
        return try {
            if (isOffline()) {
                return Result.Success(applyFiltersInMemory(cocktailCache.getAllCachedCocktails(), filters))
            }

            // Nothing active → same default list as category browsing.
            if (filters.query.isBlank() && !filters.hasActiveFilters()) {
                return filterByCategory("Cocktail")
            }

            // One call per active filter, in priority order. getOrThrow bubbles a
            // failed call out to the catch below as Result.Error.
            val activeSets = mutableListOf<List<Cocktail>>()
            if (filters.query.isNotBlank()) {
                activeSets.add(searchCocktailsByName(filters.query).getOrThrow())
            }
            if (filters.category != null) {
                activeSets.add(filterByCategory(filters.category).getOrThrow())
            }
            if (filters.ingredient != null) {
                activeSets.add(filterByIngredient(filters.ingredient).getOrThrow())
            }
            if (filters.alcoholic != null) {
                activeSets.add(filterByAlcoholic(filters.alcoholic).getOrThrow())
            }

            // Base list = first active set; keep only ids present in every other set.
            val base = activeSets.first()
            val result = activeSets.drop(1).fold(base) { acc, set ->
                val ids = set.mapTo(HashSet()) { it.id }
                acc.filter { it.id in ids }
            }

            // Cache the results for offline access
            result.forEach { cocktailCache.cacheCocktail(it) }

            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to perform advanced search")
        }
    }

    /** Offline path: filter the in-memory cache on the 4 supported fields. */
    private fun applyFiltersInMemory(cocktails: List<Cocktail>, filters: SearchFilters): List<Cocktail> {
        var result = cocktails
        val query = filters.query
        val category = filters.category
        val ingredient = filters.ingredient
        val alcoholic = filters.alcoholic

        if (query.isNotBlank()) {
            result = result.filter { it.name.contains(query, ignoreCase = true) }
        }
        if (category != null) {
            result = result.filter { it.category == category }
        }
        if (ingredient != null) {
            result = result.filter { cocktail ->
                cocktail.ingredients.any { it.name.contains(ingredient, ignoreCase = true) }
            }
        }
        if (alcoholic != null) {
            // Full records store TheCocktailDB's display strings ("Alcoholic" /
            // "Non alcoholic"); the underscore form only exists as a URL token.
            result = result.filter {
                val value = it.alcoholic?.replace('_', ' ')
                if (alcoholic) value.equals("Alcoholic", ignoreCase = true)
                else value.equals("Non alcoholic", ignoreCase = true)
            }
        }
        return result
    }
}
