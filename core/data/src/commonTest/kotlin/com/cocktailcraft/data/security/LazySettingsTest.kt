package com.cocktailcraft.data.security

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LazySettingsTest {

    @Test
    fun opensTheStoreOnFirstUseNotOnConstruction() {
        var opened = 0
        val settings = LazySettings {
            opened++
            MapSettings()
        }
        assertEquals(0, opened)

        settings.putString("k", "v")
        assertEquals("v", settings.getStringOrNull("k"))
        settings.putInt("n", 1)
        assertEquals(1, settings.getInt("n", 0))
        assertEquals(1, opened)
    }

    @Test
    fun delegatesEveryTypeAndBulkOperation() {
        val backing = MapSettings()
        val settings = LazySettings { backing }

        settings.putLong("l", 2L)
        settings.putFloat("f", 3f)
        settings.putDouble("d", 4.0)
        settings.putBoolean("b", true)
        assertEquals(2L, backing.getLongOrNull("l"))
        assertEquals(3f, settings.getFloatOrNull("f"))
        assertEquals(4.0, settings.getDouble("d", 0.0))
        assertTrue(settings.getBoolean("b", false))
        assertEquals(setOf("l", "f", "d", "b"), settings.keys)
        assertEquals(4, settings.size)

        settings.remove("l")
        assertTrue(!settings.hasKey("l"))
        settings.clear()
        assertEquals(0, backing.size)
    }

    @Test
    fun aFailedOpenIsRetriedOnTheNextAccess() {
        var attempts = 0
        val settings = LazySettings {
            attempts++
            if (attempts == 1) throw IllegalStateException("keystore unavailable")
            MapSettings()
        }

        assertFailsWith<IllegalStateException> { settings.getStringOrNull("k") }
        settings.putString("k", "v")
        assertEquals("v", settings.getStringOrNull("k"))
        assertEquals(2, attempts)
    }
}
