package com.cocktailcraft.data.cache

import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.testutil.FakeNetworkMonitor
import com.cocktailcraft.testutil.testCocktail
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
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

    // Same key as CocktailCache.ALL_COCKTAILS_KEY — tests write persisted JSON directly.
    private val allCocktailsKey = "all_cached_cocktails"

    /** Wraps a [Settings] so tests can count how many disk writes a code path performs. */
    private class CountingSettings(private val delegate: Settings) : Settings by delegate {
        var putStringCount = 0
            private set

        override fun putString(key: String, value: String) {
            putStringCount++
            delegate.putString(key, value)
        }
    }

    private fun cache(settings: Settings, online: Boolean = true, appConfig: AppConfig = config) =
        CocktailCache(settings, json, appConfig, networkMonitor = FakeNetworkMonitor(online))

    private fun nowMs() = Clock.System.now().toEpochMilliseconds()

    private fun persistEntries(settings: Settings, entries: List<CachedCocktailEntry>) {
        settings.putString(allCocktailsKey, json.encodeToString(entries))
    }

    // --- Batch persistence (one write per batch, LRU parity with sequential inserts) ---

    @Test
    fun batchCachingMakesAllCocktailsRetrievable() = runTest {
        val cache = cache(MapSettings())
        val cocktails = (1..20).map { testCocktail("$it") }

        cache.cacheCocktails(cocktails)

        assertEquals(20, cache.getCachedCocktailCount())
        cocktails.forEach { assertEquals(it, cache.getCachedCocktail(it.id)) }
    }

    @Test
    fun batchCachingPersistsAcrossReload() = runTest {
        val settings = MapSettings()
        cache(settings).cacheCocktails((1..20).map { testCocktail("$it") })

        // A fresh instance over the same settings must load the persisted batch
        val reloaded = cache(settings)

        assertEquals(20, reloaded.getCachedCocktailCount())
        (1..20).forEach { assertNotNull(reloaded.getCachedCocktail("$it")) }
    }

    @Test
    fun batchCachingWritesToSettingsOnceInsteadOfPerItem() = runTest {
        val settings = CountingSettings(MapSettings())
        val cache = cache(settings)

        cache.cacheCocktails((1..20).map { testCocktail("$it") })

        assertEquals(1, settings.putStringCount, "batch must persist once, not once per item")
    }

    @Test
    fun singleItemCachingStillPersists() = runTest {
        val settings = MapSettings()
        cache(settings).cacheCocktail(testCocktail("11007"))

        val reloaded = cache(settings)

        assertEquals("Cocktail 11007", reloaded.getCachedCocktail("11007")?.name)
    }

    @Test
    fun batchCachingEvictsLeastRecentlyUsedBeyondCap() = runTest {
        val settings = MapSettings()
        val cache = cache(settings)

        // maxOfflineCocktails = 100; caching 105 must evict the 5 oldest
        cache.cacheCocktails((1..105).map { testCocktail("$it") })

        assertEquals(100, cache.getCachedCocktailCount())
        (1..5).forEach { assertNull(cache.getCachedCocktail("$it"), "id $it should be evicted") }
        (6..105).forEach { assertNotNull(cache.getCachedCocktail("$it"), "id $it should survive") }

        // Persisted snapshot honours the cap too
        assertEquals(100, cache(settings).getCachedCocktailCount())
    }

    @Test
    fun batchEvictionOrderMatchesSequentialInsertion() = runTest {
        val cache = cache(MapSettings())
        cache.cacheCocktails((1..100).map { testCocktail("$it") })

        // Touch id 1 so it becomes most recently used, then overflow by one
        cache.getCachedCocktail("1")
        cache.cacheCocktails(listOf(testCocktail("101")))

        assertNotNull(cache.getCachedCocktail("1"), "recently accessed entry must survive")
        assertNull(cache.getCachedCocktail("2"), "true LRU entry must be evicted")
    }

    // --- Time-based expiry (online only) and legacy-format migration ---

    @Test
    fun freshEntrySurvivesPersistenceRoundTrip() = runTest {
        val settings = MapSettings()
        cache(settings).cacheCocktail(testCocktail("11007"))

        // A brand-new instance over the same settings reloads it from storage
        val reloaded = cache(settings)
        assertEquals("11007", reloaded.getCachedCocktail("11007")?.id)
        assertTrue(reloaded.isCocktailCached("11007"))
    }

    @Test
    fun expiredEntryIsNotReturnedWhenOnline() = runTest {
        val settings = MapSettings()
        val now = nowMs()
        persistEntries(
            settings,
            listOf(
                CachedCocktailEntry(testCocktail("old"), now - config.cacheExpirationMs - 1_000),
                CachedCocktailEntry(testCocktail("fresh"), now)
            )
        )

        val cache = cache(settings, online = true)

        assertNull(cache.getCachedCocktail("old"))
        assertFalse(cache.isCocktailCached("old"))
        assertEquals(listOf("fresh"), cache.getAllCachedCocktails().map { it.id })
    }

    @Test
    fun expiredEntryIsEvictedFromStorageAfterReload() = runTest {
        val settings = MapSettings()
        val now = nowMs()
        persistEntries(
            settings,
            listOf(
                CachedCocktailEntry(testCocktail("old"), now - config.cacheExpirationMs - 1_000),
                CachedCocktailEntry(testCocktail("fresh"), now)
            )
        )

        // Online load evicts the expired entry and rewrites storage
        cache(settings, online = true).getAllCachedCocktails()

        // Even an offline instance (which never expires) no longer sees it
        val offline = cache(settings, online = false)
        assertNull(offline.getCachedCocktail("old"))
        assertEquals(listOf("fresh"), offline.getAllCachedCocktails().map { it.id })
    }

    @Test
    fun legacyPersistedJsonWithoutTimestampsStillLoads() = runTest {
        val settings = MapSettings()
        // Pre-TTL format: plain List<Cocktail>, no cachedAtMs anywhere
        val legacy = listOf(testCocktail("1"), testCocktail("2"))
        settings.putString(allCocktailsKey, json.encodeToString(legacy))

        val cache = cache(settings, online = true)

        // Legacy entries are stamped "now", so they load and are not expired
        assertEquals(setOf("1", "2"), cache.getAllCachedCocktails().map { it.id }.toSet())
        assertNotNull(cache.getCachedCocktail("1"))

        // Storage was migrated to the timestamped format
        val migrated = json.decodeFromString<List<CachedCocktailEntry>>(
            settings.getStringOrNull(allCocktailsKey)!!
        )
        assertEquals(2, migrated.size)
        assertTrue(migrated.all { it.cachedAtMs > 0L })
    }

    @Test
    fun expiredEntryIsStillServedWhileDeviceIsOffline() = runTest {
        val settings = MapSettings()
        persistEntries(settings, listOf(CachedCocktailEntry(testCocktail("old"), cachedAtMs = 1L)))

        // Offline: the cache backs offline browsing, so stale entries survive
        val cache = cache(settings, online = false)

        assertEquals("old", cache.getCachedCocktail("old")?.id)
        assertEquals(listOf("old"), cache.getAllCachedCocktails().map { it.id })
    }

    @Test
    fun expiredEntryIsStillServedWhenOfflineModeIsForced() = runTest {
        val settings = MapSettings()
        settings.putBoolean(config.offlineModeEnabledKey, true)
        persistEntries(settings, listOf(CachedCocktailEntry(testCocktail("old"), cachedAtMs = 1L)))

        // Network is up, but the user forced offline mode — no expiry
        val cache = cache(settings, online = true)

        assertEquals("old", cache.getCachedCocktail("old")?.id)
    }

    @Test
    fun cacheSizeIsCappedByMaxOfflineCocktailsConfig(): Unit = runTest {
        val smallConfig = object : AppConfig by config {
            override val maxOfflineCocktails: Int = 2
        }
        val settings = MapSettings()
        val cache = cache(settings, appConfig = smallConfig)

        cache.cacheCocktail(testCocktail("1"))
        cache.cacheCocktail(testCocktail("2"))
        cache.cacheCocktail(testCocktail("3"))

        // LRU eviction: oldest entry dropped once over the configured cap
        assertEquals(2, cache.getCachedCocktailCount())
        assertNull(cache.getCachedCocktail("1"))
        assertNotNull(cache.getCachedCocktail("3"))
    }
}
