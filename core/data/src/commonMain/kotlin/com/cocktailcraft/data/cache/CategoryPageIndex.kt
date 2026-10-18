package com.cocktailcraft.data.cache

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Which cocktails each category page lists, in the API's order, and when the
 * page was last fetched. Ids only: the cocktails themselves live in
 * [CocktailCache] (the cached_cocktail table), which the pages are read from.
 *
 * In memory because the schema can't hold it: cached_cocktail has a category
 * column per cocktail but no page order and no per-page fetch time (a row's
 * cached_at is also bumped by detail lookups and searches, so it can't tell
 * whether the whole page was fetched). Until a category is fetched in this
 * process its page falls back to the rows whose category column matches. A
 * category_page table (category, cocktail_id, position, fetched_at) would
 * make this persistent, but needs a schema migration.
 */
internal class CategoryPageIndex {

    data class Page(val ids: List<String>, val fetchedAtMs: Long)

    private val pages = MutableStateFlow<Map<String, Page>>(emptyMap())

    fun page(category: String): Page? = pages.value[category]

    /** The page's ids, or null until [category] has been fetched. */
    fun observeIds(category: String): Flow<List<String>?> =
        pages.map { it[category]?.ids }.distinctUntilChanged()

    fun put(category: String, ids: List<String>, fetchedAtMs: Long) {
        pages.update { it + (category to Page(ids, fetchedAtMs)) }
    }
}
