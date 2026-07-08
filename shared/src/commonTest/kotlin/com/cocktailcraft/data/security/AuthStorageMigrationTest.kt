package com.cocktailcraft.data.security

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthStorageMigrationTest {

    @Test
    fun movesEveryAuthKeyAndLeavesTheRest() {
        val plain = MapSettings()
        val secure = MapSettings()
        plain.putString("users", """[{"id":"1","email":"a@b.com"}]""")
        plain.putString("current_user_id", "1")
        plain.putString("guest_preferences", "{}")
        plain.putString("password_a@b.com", "v2:600000:aa:bb")
        plain.putInt("signin_attempts_a@b.com", 3)
        plain.putLong("signin_lockout_until_a@b.com", 12345L)
        plain.putString("cocktail_cart_items", "[]")
        plain.putBoolean("offline_mode_enabled", true)

        migrateAuthStorage(from = plain, to = secure)

        // All auth keys moved with their values and types intact.
        assertEquals("""[{"id":"1","email":"a@b.com"}]""", secure.getStringOrNull("users"))
        assertEquals("1", secure.getStringOrNull("current_user_id"))
        assertEquals("{}", secure.getStringOrNull("guest_preferences"))
        assertEquals("v2:600000:aa:bb", secure.getStringOrNull("password_a@b.com"))
        assertEquals(3, secure.getIntOrNull("signin_attempts_a@b.com"))
        assertEquals(12345L, secure.getLongOrNull("signin_lockout_until_a@b.com"))

        // ...and no longer exist in the plain store.
        assertNull(plain.getStringOrNull("users"))
        assertNull(plain.getStringOrNull("password_a@b.com"))
        assertFalse(plain.hasKey("signin_attempts_a@b.com"))

        // Non-auth data stays where it was.
        assertEquals("[]", plain.getStringOrNull("cocktail_cart_items"))
        assertTrue(plain.getBoolean("offline_mode_enabled", false))
        assertFalse(secure.hasKey("cocktail_cart_items"))
    }

    @Test
    fun secondRunIsANoOp() {
        val plain = MapSettings()
        val secure = MapSettings()
        plain.putString("users", "[]")

        migrateAuthStorage(from = plain, to = secure)
        migrateAuthStorage(from = plain, to = secure)

        assertEquals("[]", secure.getStringOrNull("users"))
        assertFalse(plain.hasKey("users"))
    }

    @Test
    fun neverOverwritesNewerSecureData() {
        val plain = MapSettings()
        val secure = MapSettings()
        plain.putString("current_user_id", "stale-plain-copy")
        secure.putString("current_user_id", "current-secure-value")

        migrateAuthStorage(from = plain, to = secure)

        assertEquals("current-secure-value", secure.getStringOrNull("current_user_id"))
        assertFalse(plain.hasKey("current_user_id"), "the stale copy is still cleaned up")
    }
}
