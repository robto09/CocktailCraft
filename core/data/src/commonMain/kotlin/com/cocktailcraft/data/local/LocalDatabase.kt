package com.cocktailcraft.data.local

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import co.touchlab.kermit.Logger
import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Access to the app database for the repositories that own the user's data
 * (cart, orders, favorites, reviews), scoped to its current [owner]: reads
 * and writes go to [ownerId]'s rows, and [observe] follows the owner as it
 * changes (see UserDataOwnership.kt).
 *
 * The database is opened lazily on first [get], on [ioDispatcher] — a Koin
 * `single` must not touch disk on whichever thread first resolves it. That
 * first open also runs the one-time [LegacySettingsImport] of the data older
 * app versions kept as JSON in Settings, and assigns any rows without an
 * owner yet (from that import or an upgrade) to the current owner, so no
 * repository ever reads the tables before both have run.
 *
 * [com.cocktailcraft.data.cache.CocktailCache] shares the same database but
 * opens it, and imports its own legacy Settings keys, independently.
 */
internal class LocalDatabase(
    // Also read by CocktailCache's test fixtures over the same database
    internal val database: Lazy<CocktailCraftDatabase>,
    private val legacyImport: LegacySettingsImport,
    private val owner: CurrentOwner,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    constructor(
        database: CocktailCraftDatabase,
        legacyImport: LegacySettingsImport,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
        owner: CurrentOwner = FixedOwner()
    ) : this(lazyOf(database), legacyImport, owner, ioDispatcher)

    private val openMutex = Mutex()
    private var opened = false

    /**
     * The database, after the legacy import and the assignment of rows
     * without an owner have run (once per process).
     */
    suspend fun get(): CocktailCraftDatabase {
        if (!opened) {
            openMutex.withLock {
                if (!opened) {
                    val ownerId = owner.ownerId()
                    withContext(ioDispatcher) {
                        importLegacyData()
                        database.value.claimUnassignedUserData(ownerId)
                    }
                    opened = true
                }
            }
        }
        return database.value
    }

    /** Whose rows reads and writes go to now: a user id, or OwnerIds.GUEST. */
    suspend fun ownerId(): String = owner.ownerId()

    /** [ownerId], and again whenever it changes. */
    val ownerIds: Flow<String>
        get() = owner.ownerIds

    /** Moves the guest's cart, favorites and recently viewed list to [accountId] (see mergeGuestUserData). */
    suspend fun mergeGuestInto(accountId: String) {
        val db = get()
        withContext(ioDispatcher) { db.mergeGuestUserData(accountId) }
    }

    /** Deletes every owner's cart, favorites, orders and reviews (see deleteEveryOwnersUserData). */
    suspend fun deleteEveryOwnersUserData() {
        val db = get()
        withContext(ioDispatcher) { db.deleteEveryOwnersUserData() }
    }

    private fun importLegacyData() {
        try {
            legacyImport.run(database.value)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Each store's Settings key is only removed after its rows are
            // committed, so anything that failed here is retried next launch.
            Logger.e(e) { "Importing legacy Settings data failed: ${e.message}" }
        }
    }

    /**
     * Emits the rows of [query] for the current owner, mapped by [transform],
     * again after every write to the tables the query reads that changes the
     * result, and with the new owner's rows whenever the owner changes.
     * Querying and mapping (JSON decoding included) run on [dispatcher], the
     * calling repository's IO dispatcher.
     *
     * A failing query is logged and ends the stream, so collectors keep their
     * last state instead of crashing the scope they collect in.
     */
    fun <T : Any, R> observe(
        dispatcher: CoroutineDispatcher,
        query: CocktailCraftDatabase.(ownerId: String) -> Query<T>,
        transform: (List<T>) -> R
    ): Flow<R> = observeStateful(dispatcher, query) { transform }

    /**
     * [observe] with a transform made by [newTransform] for each collector
     * (and owner), so it can keep state across emissions: a
     * CocktailDecodeMemo, which only decodes the stored cocktails that
     * changed since the last one.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun <T : Any, R> observeStateful(
        dispatcher: CoroutineDispatcher,
        query: CocktailCraftDatabase.(ownerId: String) -> Query<T>,
        newTransform: () -> (List<T>) -> R
    ): Flow<R> =
        owner.ownerIds
            .flatMapLatest { ownerId ->
                flow {
                    val transform = newTransform()
                    emitAll(get().query(ownerId).asFlow().mapToList(dispatcher).map(transform))
                }
            }
            .flowOn(dispatcher)
            // Queries joining cached_cocktail re-run on every catalog write
            .distinctUntilChanged()
            .catch { e -> Logger.e(e) { "Local database query failed: ${e.message}" } }
}
