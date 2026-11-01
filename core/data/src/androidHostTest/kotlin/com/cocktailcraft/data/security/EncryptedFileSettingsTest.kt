package com.cocktailcraft.data.security

import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
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

/** Real Tink AES-256-GCM on the host JVM; only the Keystore wrapping is absent. */
internal fun newTestAead(): Aead {
    AeadConfig.register()
    return KeysetHandle.generateNew(KeyTemplates.get("AES256_GCM"))
        .getPrimitive(RegistryConfiguration.get(), Aead::class.java)
}

class EncryptedFileSettingsTest {

    private val dir = createTempDirectory("auth-store").toFile()
    private val file = File(dir, "store")
    private val aead = newTestAead()

    @AfterTest
    fun cleanUp() {
        dir.deleteRecursively()
    }

    @Test
    fun valuesOfEveryTypeSurviveReopening() {
        EncryptedFileSettings(file, aead).apply {
            putString("s", "hello")
            putInt("i", 42)
            putLong("l", 1L shl 40)
            putFloat("f", 1.5f)
            putDouble("d", 2.25)
            putBoolean("b", true)
        }

        val reopened = EncryptedFileSettings(file, aead)
        assertEquals("hello", reopened.getStringOrNull("s"))
        assertEquals(42, reopened.getIntOrNull("i"))
        assertEquals(1L shl 40, reopened.getLongOrNull("l"))
        assertEquals(1.5f, reopened.getFloatOrNull("f"))
        assertEquals(2.25, reopened.getDoubleOrNull("d"))
        assertEquals(true, reopened.getBooleanOrNull("b"))
        assertEquals(setOf("s", "i", "l", "f", "d", "b"), reopened.keys)
    }

    @Test
    fun removeAndClearPersist() {
        EncryptedFileSettings(file, aead).apply {
            putString("a", "1")
            putString("b", "2")
            remove("a")
        }
        assertEquals(setOf("b"), EncryptedFileSettings(file, aead).keys)

        EncryptedFileSettings(file, aead).clear()
        assertEquals(0, EncryptedFileSettings(file, aead).size)
    }

    @Test
    fun aValueReadAsAnotherTypeIsAbsent() {
        val settings = EncryptedFileSettings(file, aead)
        settings.putInt("n", 1)

        assertNull(settings.getLongOrNull("n"))
        assertNull(settings.getStringOrNull("n"))
        assertEquals(7L, settings.getLong("n", 7L))
    }

    @Test
    fun neitherKeysNorValuesAreStoredInTheClear() {
        EncryptedFileSettings(file, aead).putString("password_alice@example.com", "v2:600000:abc:def")

        val raw = file.readBytes().decodeToString()
        assertFalse("alice@example.com" in raw)
        assertFalse("v2:600000" in raw)
    }

    @Test
    fun aDifferentKeyCannotOpenTheStore() {
        EncryptedFileSettings(file, aead).putString("k", "v")

        assertFailsWith<GeneralSecurityException> { EncryptedFileSettings(file, newTestAead()) }
    }

    @Test
    fun aTamperedFileFailsToOpen() {
        EncryptedFileSettings(file, aead).putString("k", "v")
        val bytes = file.readBytes()
        bytes[bytes.size / 2] = (bytes[bytes.size / 2].toInt() xor 1).toByte()
        file.writeBytes(bytes)

        assertFailsWith<GeneralSecurityException> { EncryptedFileSettings(file, aead) }
    }

    @Test
    fun aWriteInterruptedBeforeTheRenameLeavesThePreviousStore() {
        EncryptedFileSettings(file, aead).putString("k", "committed")
        // What a crash mid-write leaves behind: a partial temp file
        File(dir, "store.tmp").writeBytes(byteArrayOf(1, 2, 3))

        val reopened = EncryptedFileSettings(file, aead)
        assertEquals("committed", reopened.getStringOrNull("k"))
        assertFalse(File(dir, "store.tmp").exists(), "the stale temp file is discarded")
    }

    @Test
    fun aFailedWriteChangesNothing() {
        val settings = EncryptedFileSettings(file, aead)
        settings.putString("k", "old")
        // A directory where the temp file goes makes the next write fail
        File(dir, "store.tmp").mkdir()

        assertFailsWith<IOException> { settings.putString("k", "new") }
        assertEquals("old", settings.getStringOrNull("k"), "in-memory view unchanged")
        File(dir, "store.tmp").delete()
        assertEquals("old", EncryptedFileSettings(file, aead).getStringOrNull("k"), "file unchanged")
    }
}
