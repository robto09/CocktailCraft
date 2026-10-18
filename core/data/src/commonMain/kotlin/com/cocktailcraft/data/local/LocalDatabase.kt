package com.cocktailcraft.data.local

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import co.touchlab.kermit.Logger
import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Access to the app database for the repositories that own the user's data
 * (cart, orders, favorites, reviews).
 *
 * The database is opened lazily on first [get], on [ioDispatcher] — a Koin
 * `single` must not touch disk on whichever thread first resolves it. That
 * first open also runs the one-time [LegacySettingsImport] of the data older
 * app versions kept as JSON in Settings, so no repository ever reads the
 * tables before the import has run.
 *
 * [com.cocktailcraft.data.cache.CocktailCache] shares the same database but
 * opens it, and imports its own legacy Settings keys, independently.
 */
internal class LocalDatabase(
    // Also read by CocktailCache's test fixtures over the same database
    internal val database: Lazy<CocktailCraftDatabase>,
    private val legacyImport: LegacySettingsImport,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    constructor(
        database: CocktailCraftDatabase,
        legacyImport: LegacySettingsImport,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    ) : this(lazyOf(database), legacyImport, ioDispatcher)

    private val openMutex = Mutex()
    private var opened = false

    /** The database, after the legacy import has run (once per process). */
    suspend fun get(): CocktailCraftDatabase {
        if (!opened) {
            openMutex.withLock {
                if (!opened) {
                    withContext(ioDispatcher) { importLegacyData() }
                    opened = true
                }
            }
        }
        return database.value
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
     * Emits the rows of [query], mapped by [transform], and again after every
     * write to the tables the query reads that changes the result. Querying
     * and mapping (JSON decoding included) run on [dispatcher], the calling
     * repository's IO dispatcher.
     *
     * A failing query is logged and ends the stream, so collectors keep their
     * last state instead of crashing the scope they collect in.
     */
    fun <T : Any, R> observe(
        dispatcher: CoroutineDispatcher,
        query: CocktailCraftDatabase.() -> Query<T>,
        transform: (List<T>) -> R
    ): Flow<R> =
        flow { emitAll(get().query().asFlow().mapToList(dispatcher)) }
            .map(transform)
            .flowOn(dispatcher)
            // Queries joining cached_cocktail re-run on every catalog write
            .distinctUntilChanged()
            .catch { e -> Logger.e(e) { "Local database query failed: ${e.message}" } }
}
