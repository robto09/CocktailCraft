package com.cocktailcraft.data.cache

import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCategories
import com.cocktailcraft.domain.model.CocktailIngredient
import com.cocktailcraft.testutil.CountingSqlDriver
import com.cocktailcraft.testutil.FakeNetworkMonitor
import com.cocktailcraft.testutil.createInMemorySqlDriver
import com.cocktailcraft.testutil.createTestDatabase
import com.cocktailcraft.testutil.testCocktail
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock

class CocktailCacheTest {

    private val config = AppConfigImpl()
    private val json = Json { ignoreUnknownKeys = true }

    private fun smallConfig(cap: Int): AppConfig = object : AppConfig by config {
        override val maxOfflineCocktails: Int = cap
    }

    /** Wraps a [Settings] so tests can count writes to the old key-value store. */
    private class CountingSettings(private val delegate: Settings) : Settings by delegate {
        var putStringCount = 0
            private set

        override fun putString(key: String, value: String) {
            putStringCount++
            delegate.putString(key, value)
        }
    }

    private fun cache(
        db: CocktailCraftDatabase,
        settings: Settings = MapSettings(),
        online: Boolean = true,
        appConfig: AppConfig = config
    ) = CocktailCache(db, settings, json, appConfig, offlineModePolicy = OfflineModePolicy(FakeNetworkMonitor(online)))

    private fun nowMs() = Clock.System.now().toEpochMilliseconds()

    private fun expiredAt() = nowMs() - config.cacheExpirationMs - 1_000

    /** Writes a row directly, bypassing the cache, e.g. to plant an old timestamp. */
    private fun seedRow(db: CocktailCraftDatabase, cocktail: Cocktail, cachedAt: Long, lastAccessed: Long = cachedAt) {
        db.cachedCocktailQueries.upsert(
            id = cocktail.id,
            category = cocktail.category,
            payload = json.encodeToString(Cocktail.serializer(), cocktail),
            cached_at = cachedAt,
            last_accessed = lastAccessed
        )
    }

    private fun rowCount(db: CocktailCraftDatabase) = db.cachedCocktailQueries.count().executeAsOne()

    private fun cachedAt(db: CocktailCraftDatabase, id: String) =
        db.cachedCocktailQueries.selectById(id).executeAsOneOrNull()?.cached_at

    private fun seedLegacyEntries(settings: Settings, entries: List<CachedCocktailEntry>) {
        settings.putString(
            CocktailCache.ALL_COCKTAILS_KEY,
            json.encodeToString(ListSerializer(CachedCocktailEntry.serializer()), entries)
        )
    }

    private fun seedLegacyCocktails(settings: Settings, key: String, cocktails: List<Cocktail>) {
        settings.putString(key, json.encodeToString(ListSerializer(Cocktail.serializer()), cocktails))
    }

    // --- Per-row persistence ---

    @Test
    fun batchCachingMakesAllCocktailsRetrievable() = runTest {
        val cache = cache(createTestDatabase())
        val cocktails = (1..20).map { testCocktail("$it") }

        cache.cacheCocktails(cocktails)

        assertEquals(20, cache.getCachedCocktailCount())
        cocktails.forEach { assertEquals(it, cache.getCachedCocktail(it.id)) }
    }

    @Test
    fun listPlaceholderNeverReplacesACachedFullRecord() = runTest {
        val cache = cache(createTestDatabase())
        val full = testCocktail("11007", name = "Margarita")
        // What a filter.php listing maps to: no ingredients, alcoholic unknown
        val placeholder = full.copy(
            alcoholic = "Unknown",
            ingredients = listOf(CocktailIngredient(Cocktail.PLACEHOLDER_INGREDIENT_NAME, ""))
        )

        cache.cacheCocktail(full)
        cache.cacheCocktails(listOf(placeholder))

        assertEquals(full, cache.getCachedCocktail("11007"))
    }

    @Test
    fun placeholderIsCachedWhenNothingBetterIsStored() = runTest {
        val cache = cache(createTestDatabase())
        val placeholder = testCocktail("1").copy(
            ingredients = listOf(CocktailIngredient(Cocktail.PLACEHOLDER_INGREDIENT_NAME, ""))
        )

        cache.cacheCocktail(placeholder)

        assertEquals(placeholder, cache.getCachedCocktail("1"))
    }

    private fun placeholderOf(cocktail: Cocktail) = cocktail.copy(
        alcoholic = "Unknown",
        ingredients = listOf(CocktailIngredient(Cocktail.PLACEHOLDER_INGREDIENT_NAME, ""))
    )

    // --- Batched reads/writes: a fixed number of statements, not one per row ---

    @Test
    fun cachingMorePlaceholdersThanOneInQueryBindsKeepsEveryFullRecord() = runTest {
        // The stored-record lookup is chunked below SQLite's bound-parameter limit
        val cache = cache(createTestDatabase(), appConfig = smallConfig(cap = 1200))
        val full = (1..1100).map { testCocktail("$it") }
        cache.cacheCocktails(full)

        cache.cacheCocktails(full.map(::placeholderOf))

        assertTrue(cache.getAllCachedCocktails().all { it.hasFullDetails })
    }

    @Test
    fun cachingPlaceholdersLooksUpStoredRecordsInOneBatch() = runTest {
        val driver = CountingSqlDriver(createInMemorySqlDriver())
        val cache = cache(createTestDatabase(driver))
        val full = (1..100).map { testCocktail("$it") }
        cache.cacheCocktails(full)

        driver.reset()
        cache.cacheCocktails(listOf(placeholderOf(full.first())))
        val singleQueries = driver.queries

        driver.reset()
        cache.cacheCocktails(full.map(::placeholderOf))

        assertEquals(singleQueries, driver.queries, "one batched SELECT, not one per placeholder")
        // ...and the stored full records still win over the placeholders
        assertEquals(full, cache.getAllCachedCocktails().sortedBy { it.id.toInt() })
    }

    @Test
    fun cachedCocktailsPersistAcrossANewCacheInstance() = runTest {
        val db = createTestDatabase()
        cache(db).cacheCocktails((1..20).map { testCocktail("$it") })
        cache(db).cacheCocktail(testCocktail("11007", name = "Margarita"))

        // A fresh instance over the same database sees everything
        val reloaded = cache(db)

        assertEquals(21, reloaded.getCachedCocktailCount())
        (1..20).forEach { assertNotNull(reloaded.getCachedCocktail("$it")) }
        assertEquals("Margarita", reloaded.getCachedCocktail("11007")?.name)
        assertTrue(reloaded.isCocktailCached("11007"))
    }

    @Test
    fun cachingWritesOneRowPerCocktailAndNeverTheSettingsBlob() = runTest {
        val db = createTestDatabase()
        val settings = CountingSettings(MapSettings())
        val cache = cache(db, settings)

        cache.cacheCocktails((1..20).map { testCocktail("$it") })
        val firstBatchCachedAt = cachedAt(db, "1")

        cache.cacheCocktail(testCocktail("21"))

        assertEquals(21L, rowCount(db))
        // Caching one more cocktail left the existing rows alone
        assertEquals(firstBatchCachedAt, cachedAt(db, "1"))
        assertEquals(0, settings.putStringCount, "the cache must no longer write to Settings")
        assertNull(settings.getStringOrNull(CocktailCache.ALL_COCKTAILS_KEY))
    }

    @Test
    fun recachingACocktailReplacesItsRowInsteadOfDuplicatingIt() = runTest {
        val db = createTestDatabase()
        val cache = cache(db)

        cache.cacheCocktail(testCocktail("1", name = "Old name"))
        cache.cacheCocktail(testCocktail("1", name = "New name"))

        assertEquals(1L, rowCount(db))
        assertEquals("New name", cache.getCachedCocktail("1")?.name)
    }

    @Test
    fun resolvingTheCacheDoesNotOpenTheDatabase() = runTest {
        var opened = false
        val database = lazy {
            opened = true
            createTestDatabase()
        }
        val cache = CocktailCache(database, MapSettings(), json, config)

        assertFalse(opened, "constructing the cache must not touch the database")

        cache.getCachedCocktailCount()
        assertTrue(opened)
    }

    // --- LRU eviction ---

    @Test
    fun maxOfflineCocktailsIsFiveHundred() {
        assertEquals(500, config.maxOfflineCocktails)
    }

    @Test
    fun batchCachingEvictsLeastRecentlyUsedBeyondTheDefaultCap() = runTest {
        val db = createTestDatabase()
        val cache = cache(db)

        // maxOfflineCocktails = 500; caching 505 must evict the 5 oldest
        cache.cacheCocktails((1..505).map { testCocktail("$it") })

        assertEquals(500, cache.getCachedCocktailCount())
        (1..5).forEach { assertNull(cache.getCachedCocktail("$it"), "id $it should be evicted") }
        listOf(6, 250, 505).forEach { assertNotNull(cache.getCachedCocktail("$it"), "id $it should survive") }

        // The cap holds for a new instance over the same database too
        assertEquals(500, cache(db).getCachedCocktailCount())
    }

    @Test
    fun twoBrowsedCategoriesBothFitInTheCache() = runTest {
        val cache = cache(createTestDatabase())

        cache.cacheCocktails((1..100).map { testCocktail("ordinary$it", category = "Ordinary Drink") })
        cache.cacheCocktails((1..100).map { testCocktail("cocktail$it", category = "Cocktail") })

        assertEquals(100, cache.observeCategoryPage("Ordinary Drink").first().size)
        assertEquals(100, cache.observeCategoryPage("Cocktail").first().size)
    }

    @Test
    fun batchEvictionOrderMatchesSequentialInsertion() = runTest {
        val cache = cache(createTestDatabase(), appConfig = smallConfig(5))
        cache.cacheCocktails((1..5).map { testCocktail("$it") })

        // Touch id 1 so it becomes most recently used, then overflow by one
        cache.getCachedCocktail("1")
        cache.cacheCocktails(listOf(testCocktail("6")))

        assertNotNull(cache.getCachedCocktail("1"), "recently accessed entry must survive")
        assertNull(cache.getCachedCocktail("2"), "true LRU entry must be evicted")
    }

    @Test
    fun accessRecencyIsPersistedAcrossInstances() = runTest {
        val db = createTestDatabase()
        val first = cache(db, appConfig = smallConfig(3))
        first.cacheCocktails(listOf(testCocktail("1"), testCocktail("2"), testCocktail("3")))
        first.getCachedCocktail("1")

        val second = cache(db, appConfig = smallConfig(3))
        second.cacheCocktail(testCocktail("4"))

        assertNotNull(second.getCachedCocktail("1"), "a read before restart still counts for LRU")
        assertNull(second.getCachedCocktail("2"))
    }

    @Test
    fun cacheSizeIsCappedByMaxOfflineCocktailsConfig() = runTest {
        val cache = cache(createTestDatabase(), appConfig = smallConfig(2))

        cache.cacheCocktail(testCocktail("1"))
        cache.cacheCocktail(testCocktail("2"))
        cache.cacheCocktail(testCocktail("3"))

        // LRU eviction: oldest entry dropped once over the configured cap
        assertEquals(2, cache.getCachedCocktailCount())
        assertNull(cache.getCachedCocktail("1"))
        assertNotNull(cache.getCachedCocktail("3"))
    }

    // --- TTL expiry (online only) ---

    @Test
    fun expiredEntryIsNotReturnedWhenOnline() = runTest {
        val db = createTestDatabase()
        seedRow(db, testCocktail("old"), cachedAt = expiredAt())
        seedRow(db, testCocktail("fresh"), cachedAt = nowMs())

        val cache = cache(db, online = true)

        assertNull(cache.getCachedCocktail("old"))
        assertFalse(cache.isCocktailCached("old"))
        assertEquals(listOf("fresh"), cache.getAllCachedCocktails().map { it.id })
    }

    @Test
    fun expiredEntryIsDeletedFromStorageWhenOnline() = runTest {
        val db = createTestDatabase()
        seedRow(db, testCocktail("old"), cachedAt = expiredAt())
        seedRow(db, testCocktail("fresh"), cachedAt = nowMs())

        cache(db, online = true).getAllCachedCocktails()

        // Even an offline instance (which never expires) no longer sees it
        val offline = cache(db, online = false)
        assertNull(offline.getCachedCocktail("old"))
        assertEquals(listOf("fresh"), offline.getAllCachedCocktails().map { it.id })
        assertEquals(1L, rowCount(db))
    }

    @Test
    fun expiredEntryIsStillServedWhileDeviceIsOffline() = runTest {
        val db = createTestDatabase()
        seedRow(db, testCocktail("old", category = "Shot"), cachedAt = 1L)

        // Offline: the cache backs offline browsing, so stale entries survive
        val cache = cache(db, online = false)

        assertEquals("old", cache.getCachedCocktail("old")?.id)
        assertEquals(listOf("old"), cache.getAllCachedCocktails().map { it.id })
        assertEquals(listOf("old"), cache.observeCategoryPage("Shot").first().map { it.id })
        assertEquals(1, cache.getCachedCocktailCount())
    }

    @Test
    fun recachingRefreshesTheExpiryClock() = runTest {
        val db = createTestDatabase()
        seedRow(db, testCocktail("1"), cachedAt = expiredAt())
        val cache = cache(db, online = false)

        cache.cacheCocktail(testCocktail("1"))

        assertNotNull(cache(db, online = true).getCachedCocktail("1"))
    }

    // --- Category query ---

    @Test
    fun categoryQueryReturnsOnlyThatCategory() = runTest {
        val cache = cache(createTestDatabase())
        cache.cacheCocktails(listOf(
            testCocktail("1", category = "Cocktail"),
            testCocktail("2", category = "Shot"),
            testCocktail("3", category = "Cocktail"),
            testCocktail("4", category = null)
        ))

        assertEquals(setOf("1", "3"), cache.observeCategoryPage("Cocktail").first().map { it.id }.toSet())
        assertEquals(listOf("2"), cache.observeCategoryPage("Shot").first().map { it.id })
        assertTrue(cache.observeCategoryPage("Beer").first().isEmpty())
    }

    @Test
    fun categoryQueryCanIncludeLegacyEntriesWithoutCategory() = runTest {
        val cache = cache(createTestDatabase())
        cache.cacheCocktails(listOf(
            testCocktail("stamped", category = CocktailCategories.DEFAULT),
            testCocktail("null", category = null),
            testCocktail("blank", category = "  "),
            testCocktail("other", category = "Shot")
        ))

        // Legacy rows only stand in while nothing carries the category
        assertEquals(
            listOf("stamped"),
            cache.observeCategoryPage(CocktailCategories.DEFAULT, includeUncategorized = true).first().map { it.id }
        )
        assertEquals(
            listOf("stamped"),
            cache.observeCategoryPage(CocktailCategories.DEFAULT).first().map { it.id }
        )
        assertEquals(
            setOf("null", "blank"),
            cache.observeCategoryPage("Ordinary Drink", includeUncategorized = true).first().map { it.id }.toSet()
        )
    }

    // --- Category pages (category_page) ---

    @Test
    fun storedCategoryPageKeepsTheFetchedOrderAndFetchTime() = runTest {
        val db = createTestDatabase()
        val page = listOf(testCocktail("z", name = "Zombie"), testCocktail("a", name = "Aviation"))
        cache(db).cacheCategoryPage("Cocktail", page, fetchedAtMs = 42L)

        val restarted = cache(db)

        assertEquals(listOf("z", "a"), restarted.observeCategoryPage("Cocktail").first().map { it.id })
        assertEquals(42L, restarted.categoryPageFetchedAt("Cocktail"))
        assertNull(restarted.categoryPageFetchedAt("Shot"), "never fetched")
    }

    @Test
    fun categoryPageWithAnEvictedCocktailIsNoLongerFresh() = runTest {
        val cache = cache(createTestDatabase(), appConfig = smallConfig(cap = 2))
        cache.cacheCategoryPage("Cocktail", listOf(testCocktail("1"), testCocktail("2")), fetchedAtMs = 42L)

        cache.cacheCocktail(testCocktail("other", category = "Shot")) // evicts id 1

        assertEquals(listOf("2"), cache.observeCategoryPage("Cocktail").first().map { it.id })
        assertNull(cache.categoryPageFetchedAt("Cocktail"), "the page no longer holds every listed cocktail")
    }

    @Test
    fun cachingWithoutACategoryKeepsTheStoredOne() = runTest {
        val cache = cache(createTestDatabase())
        cache.cacheCocktail(placeholderOf(testCocktail("1", category = "Shot")))

        cache.cacheCocktail(placeholderOf(testCocktail("1", category = null)))

        assertEquals("Shot", cache.getCachedCocktail("1")?.category)
        assertEquals(listOf("1"), cache.observeCategoryPage("Shot").first().map { it.id })
    }

    // --- Recently viewed ---

    @Test
    fun recentlyViewedIsCappedAndMostRecentFirst() = runTest {
        val cache = cache(createTestDatabase())

        (1..25).forEach { cache.addToRecentlyViewed(testCocktail("$it")) }

        val recent = cache.getRecentlyViewedCocktails().map { it.id }
        assertEquals(CocktailCache.MAX_RECENTLY_VIEWED, recent.size)
        assertEquals((25 downTo 6).map { "$it" }, recent)
    }

    @Test
    fun viewingAgainMovesACocktailToTheFront() = runTest {
        val cache = cache(createTestDatabase())

        cache.addToRecentlyViewed(testCocktail("1"))
        cache.addToRecentlyViewed(testCocktail("2"))
        cache.addToRecentlyViewed(testCocktail("1"))

        assertEquals(listOf("1", "2"), cache.getRecentlyViewedCocktails().map { it.id })
    }

    @Test
    fun recentlyViewedPersistsAcrossInstancesAndSurvivesCacheEviction() = runTest {
        val db = createTestDatabase()
        val cache = cache(db, appConfig = smallConfig(2))

        cache.addToRecentlyViewed(testCocktail("1"))
        cache.addToRecentlyViewed(testCocktail("2"))
        cache.cacheCocktails(listOf(testCocktail("3"), testCocktail("4")))

        val reloaded = cache(db, appConfig = smallConfig(2))
        // 1 and 2 were evicted from the offline cache but are still recent
        assertNull(reloaded.getCachedCocktail("1"))
        assertEquals(listOf("2", "1"), reloaded.getRecentlyViewedCocktails().map { it.id })
    }

    @Test
    fun viewingACocktailAlsoCachesIt() = runTest {
        val cache = cache(createTestDatabase())

        cache.addToRecentlyViewed(testCocktail("1"))

        assertNotNull(cache.getCachedCocktail("1"))
    }

    // --- One-time migration from the Settings blobs ---

    @Test
    fun migratesTimestampedEntriesPreservingCachedAtAndRemovesTheKeys() = runTest {
        val db = createTestDatabase()
        val settings = MapSettings()
        val oneHourAgo = nowMs() - 3_600_000
        val twoHoursAgo = nowMs() - 7_200_000
        seedLegacyEntries(settings, listOf(
            CachedCocktailEntry(testCocktail("1"), oneHourAgo),
            CachedCocktailEntry(testCocktail("2"), twoHoursAgo)
        ))

        val cache = cache(db, settings)

        assertEquals(setOf("1", "2"), cache.getAllCachedCocktails().map { it.id }.toSet())
        assertEquals(oneHourAgo, cachedAt(db, "1"))
        assertEquals(twoHoursAgo, cachedAt(db, "2"))
        assertFalse(settings.hasKey(CocktailCache.ALL_COCKTAILS_KEY))
        assertFalse(settings.hasKey(CocktailCache.RECENTLY_VIEWED_KEY))
    }

    @Test
    fun migratesPreTtlPlainCocktailListStampedNow() = runTest {
        val db = createTestDatabase()
        val settings = MapSettings()
        // Pre-TTL format: plain List<Cocktail>, no cachedAtMs anywhere
        seedLegacyCocktails(settings, CocktailCache.ALL_COCKTAILS_KEY, listOf(testCocktail("1"), testCocktail("2")))
        val before = nowMs()

        val cache = cache(db, settings, online = true)

        // Stamped "now", so they load and are not expired
        assertEquals(setOf("1", "2"), cache.getAllCachedCocktails().map { it.id }.toSet())
        assertTrue(cachedAt(db, "1")!! >= before)
        assertFalse(settings.hasKey(CocktailCache.ALL_COCKTAILS_KEY))
    }

    @Test
    fun migratesRecentlyViewedKeepingItsOrder() = runTest {
        val db = createTestDatabase()
        val settings = MapSettings()
        // The legacy list was stored oldest first
        seedLegacyCocktails(settings, CocktailCache.RECENTLY_VIEWED_KEY,
            listOf(testCocktail("a"), testCocktail("b"), testCocktail("c")))

        val cache = cache(db, settings)

        assertEquals(listOf("c", "b", "a"), cache.getRecentlyViewedCocktails().map { it.id })
        assertFalse(settings.hasKey(CocktailCache.RECENTLY_VIEWED_KEY))

        // New views land in front of the imported ones
        cache.addToRecentlyViewed(testCocktail("d"))
        assertEquals(listOf("d", "c", "b", "a"), cache.getRecentlyViewedCocktails().map { it.id })
    }

    @Test
    fun migratedLegacyCategoriesAreQueryable() = runTest {
        val settings = MapSettings()
        seedLegacyEntries(settings, listOf(
            CachedCocktailEntry(testCocktail("legacy", category = null), nowMs()),
            CachedCocktailEntry(testCocktail("shot", category = "Shot"), nowMs())
        ))

        val cache = cache(createTestDatabase(), settings)

        assertEquals(listOf("shot"), cache.observeCategoryPage("Shot").first().map { it.id })
        assertEquals(
            listOf("legacy"),
            cache.observeCategoryPage(CocktailCategories.DEFAULT, includeUncategorized = true).first().map { it.id }
        )
    }

    @Test
    fun rerunningAnInterruptedMigrationNeitherDuplicatesNorOverwrites() = runTest {
        val db = createTestDatabase()
        val settings = MapSettings()
        val legacyEntries = listOf(
            CachedCocktailEntry(testCocktail("1", name = "Legacy name"), nowMs()),
            CachedCocktailEntry(testCocktail("2"), nowMs())
        )
        val legacyRecent = listOf(testCocktail("1"))
        seedLegacyEntries(settings, legacyEntries)
        seedLegacyCocktails(settings, CocktailCache.RECENTLY_VIEWED_KEY, legacyRecent)

        val first = cache(db, settings)
        first.cacheCocktail(testCocktail("1", name = "Fresh name"))

        // Simulate the app dying after the import committed but before the
        // keys were removed: the same legacy data is still in Settings.
        seedLegacyEntries(settings, legacyEntries)
        seedLegacyCocktails(settings, CocktailCache.RECENTLY_VIEWED_KEY, legacyRecent)

        val second = cache(db, settings)

        assertEquals(2, second.getCachedCocktailCount())
        assertEquals("Fresh name", second.getCachedCocktail("1")?.name, "newer database rows must win")
        assertEquals(listOf("1"), second.getRecentlyViewedCocktails().map { it.id })
        assertFalse(settings.hasKey(CocktailCache.ALL_COCKTAILS_KEY))
        assertFalse(settings.hasKey(CocktailCache.RECENTLY_VIEWED_KEY))
    }

    @Test
    fun expiredLegacyEntriesAreDroppedOnlineButKeptOffline() = runTest {
        val legacy = listOf(
            CachedCocktailEntry(testCocktail("old"), expiredAt()),
            CachedCocktailEntry(testCocktail("fresh"), nowMs())
        )

        val onlineSettings = MapSettings().also { seedLegacyEntries(it, legacy) }
        val online = cache(createTestDatabase(), onlineSettings, online = true)
        assertEquals(listOf("fresh"), online.getAllCachedCocktails().map { it.id })

        val offlineSettings = MapSettings().also { seedLegacyEntries(it, legacy) }
        val offline = cache(createTestDatabase(), offlineSettings, online = false)
        assertEquals(setOf("old", "fresh"), offline.getAllCachedCocktails().map { it.id }.toSet())
    }

    @Test
    fun unreadableLegacyDataIsDiscardedWithoutCrashing() = runTest {
        val settings = MapSettings()
        settings.putString(CocktailCache.ALL_COCKTAILS_KEY, "{not json")
        settings.putString(CocktailCache.RECENTLY_VIEWED_KEY, "[42]")

        val cache = cache(createTestDatabase(), settings)

        assertTrue(cache.getAllCachedCocktails().isEmpty())
        assertTrue(cache.getRecentlyViewedCocktails().isEmpty())
        assertFalse(settings.hasKey(CocktailCache.ALL_COCKTAILS_KEY))
        assertFalse(settings.hasKey(CocktailCache.RECENTLY_VIEWED_KEY))

        // The cache keeps working after the failed import
        cache.cacheCocktail(testCocktail("1"))
        assertNotNull(cache.getCachedCocktail("1"))
    }
}
