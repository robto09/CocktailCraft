package com.cocktailcraft.domain.repository

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlin.native.HiddenFromObjC

/**
 * Repository interface for cocktail catalog/browsing operations.
 * Part of the Interface Segregation split of CocktailRepository.
 *
 * Category pages follow the same pattern as the cart and favorites: the
 * local database is the source of truth. [observeCategory] emits what is
 * stored (right away, then after every change) and [refreshCategory] fetches
 * from the network into the database, so observers see the result through
 * the flow.
 */
@HiddenFromObjC // Kotlin-side port, not Swift API: Swift reaches data through the ViewModels
interface CocktailCatalogRepository {
    suspend fun getCategories(): Result<List<String>>
    suspend fun getIngredients(): Result<List<String>>

    /** The stored cocktails of [category], emitted immediately and again whenever they change. */
    fun observeCategory(category: String): Flow<List<Cocktail>>

    /**
     * Fetch [category] from the network into the database when the stored
     * page is stale, or always with [force]. A failure (including being
     * offline) is a Result.Error and leaves the stored page untouched.
     * Concurrent calls for one category share one request.
     */
    suspend fun refreshCategory(category: String, force: Boolean = false): Result<Unit>
}
