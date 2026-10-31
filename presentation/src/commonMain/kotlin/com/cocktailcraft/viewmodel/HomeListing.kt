package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailRatingSummary
import com.cocktailcraft.domain.repository.CocktailCatalogRepository
import com.cocktailcraft.domain.usecase.SortCocktailsUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.runCatchingResult
import com.cocktailcraft.viewmodel.state.HomeSortOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/*
 * The parts of the Home listing that stand on their own: ordering, paging
 * over the stored category page, and the advanced-search option lists.
 * SharedHomeViewModel keeps the listing's state machine (which load is on
 * screen, search vs category, offline fallback) and uses these.
 */

/** The use case's sort for [this] option, or null for DEFAULT (source order). */
internal val HomeSortOption.sortType: SortCocktailsUseCase.SortType?
    get() = when (this) {
        HomeSortOption.DEFAULT -> null
        HomeSortOption.NAME_ASC -> SortCocktailsUseCase.SortType.NAME_ASC
        HomeSortOption.PRICE_ASC -> SortCocktailsUseCase.SortType.PRICE_ASC
        HomeSortOption.PRICE_DESC -> SortCocktailsUseCase.SortType.PRICE_DESC
        HomeSortOption.RATING -> SortCocktailsUseCase.SortType.RATING
        HomeSortOption.POPULARITY -> SortCocktailsUseCase.SortType.POPULARITY
    }

/**
 * [source] in [option]'s order. Rating ranks by the review average where
 * there is one, and the demo [Cocktail.rating] otherwise.
 */
internal fun SortCocktailsUseCase.sortListing(
    source: List<Cocktail>,
    option: HomeSortOption,
    ratings: Map<String, CocktailRatingSummary>
): List<Cocktail> {
    val sortType = option.sortType ?: return source
    return this(source, sortType) { ratings[it.id]?.rating ?: it.rating }
}

/**
 * The selected category's whole stored page, shown [pageSize] cocktails at a
 * time. Showing another page only shows more of it: no request.
 */
internal class CategoryPages(private val pageSize: Int) {
    /** The whole stored page, as last loaded or observed. */
    var all: List<Cocktail> = emptyList()

    /** The first [pagesShown] pages. */
    fun visible(pagesShown: Int): List<Cocktail> = all.take(pagesShown * pageSize)

    /** Whether there is more than [pagesShown] pages. */
    fun hasMore(pagesShown: Int): Boolean = pagesShown * pageSize < all.size
}

/**
 * [latest] in [shown]'s order when both list the same cocktails, so a
 * refresh (or an upgraded record) keeps every card in place; otherwise
 * [latest] as it is.
 */
internal fun keepingOrder(shown: List<Cocktail>, latest: List<Cocktail>): List<Cocktail> {
    if (shown.mapTo(HashSet()) { it.id } != latest.mapTo(HashSet()) { it.id }) return latest
    val byId = latest.associateBy { it.id }
    return shown.map { byId.getValue(it.id) }
}

/**
 * Loads the advanced-search option lists (categories, ingredients) once.
 * Platforms ask whenever the filter UI opens; a request in flight is not
 * repeated, and only a failed load is tried again.
 */
internal class FilterOptionsLoader(private val catalogRepository: CocktailCatalogRepository) {
    private var loaded = false
    private var job: Job? = null

    /**
     * Load on [scope] unless already loaded or loading, then hand both
     * results (either may have failed) to [publish].
     */
    fun load(
        scope: CoroutineScope,
        publish: (categories: Result<List<String>>, ingredients: Result<List<String>>) -> Unit
    ) {
        if (loaded || job?.isActive == true) return
        job = scope.launch {
            val categories = runCatchingResult("Failed to load categories") { catalogRepository.getCategories() }
            val ingredients = runCatchingResult("Failed to load ingredients") { catalogRepository.getIngredients() }
            loaded = categories is Result.Success && ingredients is Result.Success
            publish(categories, ingredients)
        }
    }
}
