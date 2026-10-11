package com.cocktailcraft.data.repository

import co.touchlab.kermit.Logger
import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.local.LocalDatabase
import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.runCatchingResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import com.cocktailcraft.data.cache.db.favoriteCocktail.SelectAll as FavoriteRow

/**
 * Favorites in the favorite_cocktail table (see FavoriteCocktail.sq),
 * extracted from the former CocktailRepositoryImpl god class.
 *
 * Each favorite stores the full cocktail, so favorites are available offline
 * whatever the offline cache evicts (SH-4), and [observeFavorites] re-emits
 * after every add, remove or in-place upgrade from any screen.
 *
 * Favorites imported from the old id-only Settings list have no stored
 * cocktail yet; they are resolved (offline cache first, then the API while
 * online) when favorites are first observed or loaded, and stay hidden until
 * then — as unresolvable ids always were.
 */
internal class CocktailFavoritesRepositoryImpl(
    private val localDatabase: LocalDatabase,
    private val json: Json,
    private val cocktailCache: CocktailCache,
    private val remote: CocktailRemoteDataSource,
    private val offlineRepository: CocktailOfflineRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CocktailFavoritesRepository {

    // Home and Favorites both observe on launch; serialising resolution means
    // the second pass finds nothing left to fetch.
    private val resolveMutex = Mutex()

    private fun encode(cocktail: Cocktail): String = json.encodeToString(Cocktail.serializer(), cocktail)

    private fun decodeOrNull(row: FavoriteRow): Cocktail? {
        val payload = row.payload ?: return null
        return try {
            json.decodeFromString(Cocktail.serializer(), payload)
        } catch (e: Exception) {
            Logger.w(e) { "Unreadable stored favorite ${row.id}, will re-resolve it" }
            null
        }
    }

    override fun observeFavorites(): Flow<List<Cocktail>> = channelFlow {
        // Resolve alongside, not before, the first emission: stored favorites
        // show immediately, and each resolved one arrives as a later emission
        // once its payload is written.
        launch {
            try {
                resolveMissingFavorites()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Logger.e(e) { "Failed to resolve favorites: ${e.message}" }
            }
        }
        localDatabase.observe(ioDispatcher, { favoriteCocktailQueries.selectAll() }) { rows ->
            rows.mapNotNull(::decodeOrNull)
        }.collect { send(it) }
    }

    /**
     * Fills in favorites without a (readable) stored cocktail. A favorite
     * that can't be resolved now — offline and not cached, or the lookup
     * failed — is left as is and retried next time.
     */
    private suspend fun resolveMissingFavorites() = resolveMutex.withLock {
        val db = localDatabase.get()
        val missing = withContext(ioDispatcher) {
            db.favoriteCocktailQueries.selectAll().executeAsList()
                .filter { decodeOrNull(it) == null }
                .map { it.id }
        }
        if (missing.isEmpty()) return@withLock

        val offline = offlineRepository.isOffline()
        for (id in missing) {
            val cocktail = try {
                cocktailCache.getCachedCocktail(id)
                    ?: if (offline) null else remote.getById(id)?.also { cocktailCache.cacheCocktail(it) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Logger.w(e) { "Could not resolve favorite $id: ${e.message}" }
                null
            }
            if (cocktail != null) {
                // A no-op if the favorite was removed in the meantime.
                withContext(ioDispatcher) {
                    db.favoriteCocktailQueries.updatePayload(payload = encode(cocktail), id = id)
                }
            }
        }
    }

    override suspend fun getFavoriteCocktails(): Result<List<Cocktail>> = withContext(ioDispatcher) {
        runCatchingResult("Failed to get favorite cocktails") {
            resolveMissingFavorites()
            val rows = localDatabase.get().favoriteCocktailQueries.selectAll().executeAsList()
            Result.Success(rows.mapNotNull(::decodeOrNull))
        }
    }

    /**
     * Adds [cocktail], or upgrades the stored copy in place if it is already
     * a favorite (the persist-then-hydrate path in ManageFavoritesUseCase).
     * Observers see it as soon as the row is written.
     */
    override suspend fun addToFavorites(cocktail: Cocktail): Result<Unit> = withContext(ioDispatcher) {
        runCatchingResult("Failed to add to favorites") {
            val db = localDatabase.get()
            val payload = encode(cocktail)
            db.transaction {
                db.favoriteCocktailQueries.insertIfAbsent(id = cocktail.id, payload = payload)
                db.favoriteCocktailQueries.updatePayload(payload = payload, id = cocktail.id)
            }
            // Also cache it, so the detail screen opens offline too.
            cocktailCache.cacheCocktail(cocktail)
            Result.Success(Unit)
        }
    }

    override suspend fun removeFromFavorites(cocktail: Cocktail): Result<Unit> = withContext(ioDispatcher) {
        runCatchingResult("Failed to remove from favorites") {
            localDatabase.get().favoriteCocktailQueries.deleteById(cocktail.id)
            Result.Success(Unit)
        }
    }

    override suspend fun isCocktailFavorite(id: String): Result<Boolean> = withContext(ioDispatcher) {
        runCatchingResult("Failed to check if cocktail is favorite") {
            Result.Success(localDatabase.get().favoriteCocktailQueries.exists(id).executeAsOne() > 0L)
        }
    }
}
