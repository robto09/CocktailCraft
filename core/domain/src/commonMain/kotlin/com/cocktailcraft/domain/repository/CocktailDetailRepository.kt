package com.cocktailcraft.domain.repository

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for cocktail detail operations.
 * Part of the Interface Segregation split of CocktailRepository.
 *
 * The local database is the source of truth: [observeCocktail] emits the
 * stored cocktail and [refreshCocktail] fetches it from the network into the
 * database.
 */
interface CocktailDetailRepository {
    /**
     * One-shot lookup: the stored cocktail when it has full details,
     * otherwise fetched (and stored). Offline or failing, the stored copy
     * (or null) is returned instead.
     */
    suspend fun getCocktailById(id: String): Result<Cocktail?>

    /** The stored cocktail [id] (null when not stored), emitted immediately and again whenever it changes. */
    fun observeCocktail(id: String): Flow<Cocktail?>

    /**
     * Fetch cocktail [id] from the network into the database when the stored
     * copy is missing, partial or stale, or always with [force]. A failure
     * (including being offline) is a Result.Error and leaves the stored copy
     * untouched. Concurrent calls for one id share one request.
     */
    suspend fun refreshCocktail(id: String, force: Boolean = false): Result<Unit>

    suspend fun getRandomCocktail(): Result<Cocktail?>
    fun getCocktailImageUrl(cocktail: Cocktail): String
}
