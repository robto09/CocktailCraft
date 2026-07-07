package com.cocktailcraft.data.cache

import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.testutil.testCocktail
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CocktailCacheTest {

    /** Wraps a [Settings] so tests can count how many disk writes a code path performs. */
    private class CountingSettings(private val delegate: Settings) : Settings by delegate {
        var putStringCount = 0
            private set

        override fun putString(key: String, value: String) {
            putStringCount++
            delegate.putString(key, value)
        }
    }

    private fun cache(settings: Settings) =
        CocktailCache(settings, Json { ignoreUnknownKeys = true }, AppConfigImpl())

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

        // MAX_CACHED_COCKTAILS = 100; caching 105 must evict the 5 oldest
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
}
