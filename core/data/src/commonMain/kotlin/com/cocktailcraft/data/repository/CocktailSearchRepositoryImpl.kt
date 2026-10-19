package com.cocktailcraft.data.repository

import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCategories
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import com.cocktailcraft.domain.repository.CocktailSearchRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.util.runCatchingResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Search and filter operations over TheCocktailDB, extracted from the former
 * CocktailRepositoryImpl monolith. The category filter reads the same
 * database-backed pages as category browsing, via [CocktailCategoryFetcher].
 */
internal class CocktailSearchRepositoryImpl(
    private val remote: CocktailRemoteDataSource,
    private val cocktailCache: CocktailCache,
    private val offlineRepository: CocktailOfflineRepository,
    private val categoryFetcher: CocktailCategoryFetcher
) : CocktailSearchRepository {

    private suspend fun isOffline(): Boolean = offlineRepository.isOffline()

    override suspend fun searchCocktailsByName(name: String): Result<List<Cocktail>> = runCatchingResult(
        "Failed to search cocktails by name"
    ) {
        Result.Success(remote.searchByName(name))
    }

    override suspend fun filterByIngredient(ingredient: String): Result<List<Cocktail>> = runCatchingResult(
        "Failed to filter by ingredient"
    ) {
        Result.Success(remote.filterByIngredient(ingredient))
    }

    override suspend fun filterByAlcoholic(alcoholic: Boolean): Result<List<Cocktail>> = runCatchingResult(
        "Failed to filter by alcoholic"
    ) {
        Result.Success(remote.filterByAlcoholic(alcoholic))
    }

    /**
     * One-shot read of a category page: refreshed first when stale, then
     * read from the database. A failed refresh falls back to the stored page;
     * only a failure with nothing stored is a Result.Error, so the UI can
     * show an error/retry instead of an empty list. An empty success means
     * the category is empty.
     */
    override suspend fun filterByCategory(
        category: String
    ): Result<List<Cocktail>> = runCatchingResult("Failed to filter by category") {
        val refreshed = categoryFetcher.refresh(category)
        val stored = categoryFetcher.snapshot(category)
        if (stored.isEmpty() && refreshed is Result.Error) refreshed else Result.Success(stored)
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
        return runCatchingResult("Failed to perform advanced search") {
            if (isOffline()) {
                return Result.Success(applyFiltersInMemory(cocktailCache.getAllCachedCocktails(), filters))
            }

            // Nothing active → same default list as category browsing.
            if (filters.query.isBlank() && !filters.hasActiveFilters()) {
                return filterByCategory(CocktailCategories.DEFAULT)
            }

            // One call per active filter, all in flight together (the rate
            // limiter still spaces the filter.php ones), listed in priority
            // order. getOrThrow bubbles a failed call out to the catch below as
            // Result.Error, cancelling the others.
            val activeSets = coroutineScope {
                // SearchFilters lives in :core:domain: no cross-module smart casts
                listOfNotNull(
                    filters.query.takeIf { it.isNotBlank() }
                        ?.let { async { searchCocktailsByName(it).getOrThrow() } },
                    filters.category?.let { async { filterByCategory(it).getOrThrow() } },
                    filters.ingredient?.let { async { filterByIngredient(it).getOrThrow() } },
                    filters.alcoholic?.let { async { filterByAlcoholic(it).getOrThrow() } }
                ).awaitAll()
            }

            // Base list = first active set; keep only ids present in every other set.
            val base = activeSets.first()
            val result = activeSets.drop(1).fold(base) { acc, set ->
                val ids = set.mapTo(HashSet()) { it.id }
                acc.filter { it.id in ids }
            }

            // Cache the full records (name search) for offline access. The
            // filter.php ones are partial: nothing an offline search could
            // match beyond the name, and hundreds of them per filter used to
            // push category pages and full details out of the cache.
            cocktailCache.cacheCocktails(result.filter { it.hasFullDetails })

            Result.Success(result)
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
                if (alcoholic) {
                    value.equals("Alcoholic", ignoreCase = true)
                } else {
                    value.equals("Non alcoholic", ignoreCase = true)
                }
            }
        }
        return result
    }
}
