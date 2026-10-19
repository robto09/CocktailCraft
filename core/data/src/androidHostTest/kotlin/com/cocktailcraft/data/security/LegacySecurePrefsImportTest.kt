package com.cocktailcraft.data.security

import com.russhwolf.settings.MapSettings
import java.io.File
import java.io.IOException
import java.security.GeneralSecurityException
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The one-time move from the EncryptedSharedPreferences file into the Tink
 * store. The legacy file itself needs the Android Keystore (absent on the
 * host JVM and in Robolectric), so it is stood in for by [FakeLegacy]; the
 * new store is the real one.
 */
class LegacySecurePrefsImportTest {

    private val dir = createTempDirectory("auth-store").toFile()
    private val file = File(dir, "store")
    private val aead = newTestAead()

    private fun openStore() = EncryptedFileSettings(file, aead)

    private class FakeLegacy(entries: Map<String, Any>?) {
        var entries = entries
        var reads = 0
        var failRead: Exception? = null
        var failDelete = false

        fun exists() = entries != null
        fun readAll(): Map<String, *> {
            reads++
            failRead?.let { throw it }
            return entries!!
        }
        fun delete() {
            check(!failDelete) { "killed before the delete" }
            entries = null
        }
    }

    private fun import(store: EncryptedFileSettings, legacy: FakeLegacy) =
        importLegacySecurePrefs(store, legacy::exists, legacy::readAll, legacy::delete)

    private val legacyAccount = mapOf(
        "users" to """[{"id":"u1","email":"a@b.com","name":"A","preferences":{}}]""",
        "current_user_id" to "u1",
        "password_a@b.com" to "v2:600000:00:11",
        "signin_attempts_a@b.com" to 2,
        "signin_lockout_until_a@b.com" to 1_000L,
    )

    @AfterTest
    fun cleanUp() {
        dir.deleteRecursively()
    }

    @Test
    fun movesEveryEntryAndDeletesTheLegacyFile() {
        val legacy = FakeLegacy(legacyAccount)

        import(openStore(), legacy)

        val store = openStore()
        assertEquals(legacyAccount["users"], store.getStringOrNull("users"))
        assertEquals("u1", store.getStringOrNull("current_user_id"))
        assertEquals("v2:600000:00:11", store.getStringOrNull("password_a@b.com"))
        assertEquals(2, store.getIntOrNull("signin_attempts_a@b.com"))
        assertEquals(1_000L, store.getLongOrNull("signin_lockout_until_a@b.com"))
        assertFalse(legacy.exists())
    }

    @Test
    fun aFreshInstallJustMarksTheStore() {
        val legacy = FakeLegacy(null)

        import(openStore(), legacy)

        assertTrue(openStore().isLegacyImported)
        assertEquals(0, openStore().size)
        assertEquals(0, legacy.reads)
    }

    @Test
    fun runningAgainIsANoOp() {
        val legacy = FakeLegacy(legacyAccount)
        import(openStore(), legacy)

        val store = openStore()
        store.putString("current_user_id", "u2")
        import(store, legacy)

        assertEquals(1, legacy.reads)
        assertEquals("u2", openStore().getStringOrNull("current_user_id"))
    }

    @Test
    fun interruptedBeforeTheImportWriteRetriesWithNothingLost() {
        val legacy = FakeLegacy(legacyAccount)
        // The process dies while reading the legacy file (or before the store write)
        legacy.failRead = IllegalStateException("process killed")
        assertFailsWith<IllegalStateException> { import(openStore(), legacy) }
        assertTrue(legacy.exists(), "legacy data is untouched")
        assertFalse(openStore().isLegacyImported)

        legacy.failRead = null
        import(openStore(), legacy)

        assertEquals("v2:600000:00:11", openStore().getStringOrNull("password_a@b.com"))
        assertFalse(legacy.exists())
    }

    @Test
    fun interruptedBeforeTheDeleteOnlyFinishesTheDelete() {
        val legacy = FakeLegacy(legacyAccount)
        legacy.failDelete = true
        assertFailsWith<IllegalStateException> { import(openStore(), legacy) }
        assertTrue(legacy.exists())

        // The user changes their password before the next launch's retry...
        openStore().putString("password_a@b.com", "v2:600000:22:33")
        legacy.failDelete = false
        import(openStore(), legacy)

        // ...and the stale legacy hash must not come back
        assertEquals("v2:600000:22:33", openStore().getStringOrNull("password_a@b.com"))
        assertEquals(1, legacy.reads)
        assertFalse(legacy.exists())
    }

    @Test
    fun anUnreadableLegacyFileIsDroppedNotRetriedForever() {
        listOf(GeneralSecurityException("bad keyset"), IOException("bad xml"), SecurityException("bad entry"))
            .forEach { failure ->
                file.delete()
                val legacy = FakeLegacy(legacyAccount)
                legacy.failRead = failure

                import(openStore(), legacy)

                assertTrue(openStore().isLegacyImported, "${failure::class.simpleName}")
                assertNull(openStore().getStringOrNull("users"))
                assertFalse(legacy.exists())
            }
    }

    @Test
    fun theOlderPlainStoreMigrationStillRunsAfterward() {
        // An install old enough to have auth data in the plain store too
        val legacy = FakeLegacy(legacyAccount)
        val plain = MapSettings("password_old@b.com" to "v1:1:00:11", "users" to "[]")
        val store = openStore()

        import(store, legacy)
        migrateAuthStorage(from = plain, to = store)

        assertEquals(legacyAccount["users"], openStore().getStringOrNull("users"), "newer secure data wins")
        assertEquals("v1:1:00:11", openStore().getStringOrNull("password_old@b.com"))
        assertEquals(0, plain.size)
    }
}
