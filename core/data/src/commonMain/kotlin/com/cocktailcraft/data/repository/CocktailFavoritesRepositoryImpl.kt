package com.cocktailcraft.data.repository

import co.touchlab.kermit.Logger
import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.local.CocktailCodec
import com.cocktailcraft.data.local.CocktailDecodeMemo
import com.cocktailcraft.data.local.LocalDatabase
import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.runCatchingResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.shareIn
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
 * A favorite refers to the cocktail's cached_cocktail row ([CocktailCache]),
 * which it pins, so favorites are available offline whatever else the cache
 * evicts and show the latest details. [observeFavorites] re-emits
 * after every add or remove from any screen, and whenever a favorite's
 * cocktail is refreshed.
 *
 * Each owner (user, or the guest) has favorites of their own; everything
 * here works on the current owner's (see UserDataOwnership.kt).
 *
 * Favorites imported from the old id-only Settings list may have no stored
 * cocktail yet; they are resolved (offline cache first, then the API while
 * online) when favorites are first observed or loaded, and stay hidden until
 * then — as unresolvable ids always were.
 */
internal class CocktailFavoritesRepositoryImpl(
    private val localDatabase: LocalDatabase,
    json: Json,
    private val cocktailCache: CocktailCache,
    private val remote: CocktailRemoteDataSource,
    private val offlineRepository: CocktailOfflineRepository,
    // App-wide scope the shared favorites stream runs in
    appScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CocktailFavoritesRepository {

    // Home and Favorites both observe on launch; serialising resolution means
    // the second pass finds nothing left to fetch.
    private val resolveMutex = Mutex()

    private val codec = CocktailCodec(json)

    private fun decodeOrNull(row: FavoriteRow): Cocktail? {
        val payload = row.payload ?: return null
        return codec.decodeOrNull(payload, row.full_details != 0L)
    }

    /**
     * The current owner's favorites, shared by every observer (Home,
     * Favorites, the detail screen, the widget bridge): one database query
     * and one decode per change however many collect, and one resolution
     * pass per owner. It runs in the app scope while anyone collects and
     * for 5 seconds after (a screen rotation or a quick back-and-forth
     * keeps it), then stops; the last list is replayed to a new collector
     * only while it runs, so nobody starts from a stale list.
     */
    private val sharedFavorites: SharedFlow<List<Cocktail>> = channelFlow {
        // Resolve alongside, not before, the first emission: stored favorites
        // show immediately, and each resolved one arrives as a later emission
        // once its cocktail is stored. Again for each new owner.
        launch {
            localDatabase.ownerIds.collect {
                try {
                    resolveMissingFavorites()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Logger.e(e) { "Failed to resolve favorites: ${e.message}" }
                }
            }
        }
        localDatabase.observeStateful(ioDispatcher, { ownerId -> favoriteCocktailQueries.selectAll(ownerId) }) {
            val memo = CocktailDecodeMemo(codec)
            return@observeStateful { rows ->
                memo.emission {
                    rows.mapNotNull { row -> row.payload?.let { decode(row.cocktail_id, it, row.full_details != 0L) } }
                }
            }
        }.collect { send(it) }
    }.shareIn(
        scope = appScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000, replayExpirationMillis = 0),
        replay = 1
    )

    override fun observeFavorites(): Flow<List<Cocktail>> = sharedFavorites

    /**
     * Stores the cocktail of favorites without a (readable) stored one. A
     * favorite that can't be resolved now — offline and not cached, or the
     * lookup failed — is left as is and retried next time.
     */
    private suspend fun resolveMissingFavorites() = resolveMutex.withLock {
        val db = localDatabase.get()
        val ownerId = localDatabase.ownerId()
        val missing = withContext(ioDispatcher) {
            db.favoriteCocktailQueries.selectAll(ownerId).executeAsList()
                .filter { decodeOrNull(it) == null }
                .map { it.cocktail_id }
        }
        if (missing.isEmpty()) return@withLock

        val offline = offlineRepository.isOffline()
        for (id in missing) {
            try {
                // A cached cocktail is already the favorite's row; otherwise
                // storing the fetched one resolves it.
                if (cocktailCache.getCachedCocktail(id) == null && !offline) {
                    remote.getById(id)?.let { cocktailCache.cacheCocktail(it) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Logger.w(e) { "Could not resolve favorite $id: ${e.message}" }
            }
        }
    }

    override suspend fun getFavoriteCocktails(): Result<List<Cocktail>> = withContext(ioDispatcher) {
        runCatchingResult("Failed to get favorite cocktails") {
            resolveMissingFavorites()
            val rows = localDatabase.get().favoriteCocktailQueries.selectAll(localDatabase.ownerId()).executeAsList()
            Result.Success(rows.mapNotNull(::decodeOrNull))
        }
    }

    /**
     * Adds [cocktail], storing it unless a better record is stored; adding
     * a favorite again with full details upgrades a partial stored record
     * (the persist-then-hydrate path in ManageFavoritesUseCase). Observers
     * see it as soon as the row is written.
     */
    override suspend fun addToFavorites(cocktail: Cocktail): Result<Unit> = withContext(ioDispatcher) {
        runCatchingResult("Failed to add to favorites") {
            localDatabase.get()
            val ownerId = localDatabase.ownerId()
            cocktailCache.storeAndReference(listOf(cocktail)) {
                favoriteCocktailQueries.insertIfAbsent(ownerId = ownerId, cocktailId = cocktail.id)
            }
            Result.Success(Unit)
        }
    }

    override suspend fun removeFromFavorites(cocktail: Cocktail): Result<Unit> = withContext(ioDispatcher) {
        runCatchingResult("Failed to remove from favorites") {
            localDatabase.get().favoriteCocktailQueries.deleteById(localDatabase.ownerId(), cocktail.id)
            Result.Success(Unit)
        }
    }

    override suspend fun isCocktailFavorite(id: String): Result<Boolean> = withContext(ioDispatcher) {
        runCatchingResult("Failed to check if cocktail is favorite") {
            val ownerId = localDatabase.ownerId()
            Result.Success(localDatabase.get().favoriteCocktailQueries.exists(ownerId, id).executeAsOne() > 0L)
        }
    }
}
