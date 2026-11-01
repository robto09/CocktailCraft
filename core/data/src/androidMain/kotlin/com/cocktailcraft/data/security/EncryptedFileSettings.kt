package com.cocktailcraft.data.security

import com.google.crypto.tink.Aead
import com.russhwolf.settings.Settings
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * A [Settings] kept as one AEAD-encrypted file: the whole key/value map
 * (keys included — they carry emails) is serialized, encrypted with [aead]
 * and written atomically on every change. The auth store is a handful of
 * small entries, so rewriting it all is cheap, and one file means one
 * atomic write per change: a crash leaves either the old or the new store,
 * never a mix.
 *
 * Opening reads and decrypts [file]; a file that fails authentication or
 * does not parse throws (GeneralSecurityException / IOException) so the
 * caller can apply its corruption policy. A failed write throws and leaves
 * both the file and the in-memory view unchanged.
 *
 * Values read back with a different type than they were written with read
 * as absent (like MapSettings), rather than throwing.
 */
// The function count is the Settings interface's
@Suppress("TooManyFunctions")
internal class EncryptedFileSettings(private val file: File, private val aead: Aead,) : Settings {

    private val lock = Any()
    private var state: StoreFile = load()

    override val keys: Set<String> get() = synchronized(lock) { state.entries.keys.toSet() }
    override val size: Int get() = synchronized(lock) { state.entries.size }

    override fun clear() = synchronized(lock) { commit(state.copy(entries = emptyMap())) }

    override fun remove(key: String) = synchronized(lock) {
        if (key in state.entries) commit(state.copy(entries = state.entries - key))
    }

    override fun hasKey(key: String): Boolean = synchronized(lock) { key in state.entries }

    override fun putInt(key: String, value: Int) = put(key, StoredValue(TYPE_INT, value.toString()))
    override fun getInt(key: String, defaultValue: Int): Int = getIntOrNull(key) ?: defaultValue
    override fun getIntOrNull(key: String): Int? = get(key, TYPE_INT)?.toIntOrNull()

    override fun putLong(key: String, value: Long) = put(key, StoredValue(TYPE_LONG, value.toString()))
    override fun getLong(key: String, defaultValue: Long): Long = getLongOrNull(key) ?: defaultValue
    override fun getLongOrNull(key: String): Long? = get(key, TYPE_LONG)?.toLongOrNull()

    override fun putString(key: String, value: String) = put(key, StoredValue(TYPE_STRING, value))
    override fun getString(key: String, defaultValue: String): String = getStringOrNull(key) ?: defaultValue
    override fun getStringOrNull(key: String): String? = get(key, TYPE_STRING)

    override fun putFloat(key: String, value: Float) = put(key, StoredValue(TYPE_FLOAT, value.toString()))
    override fun getFloat(key: String, defaultValue: Float): Float = getFloatOrNull(key) ?: defaultValue
    override fun getFloatOrNull(key: String): Float? = get(key, TYPE_FLOAT)?.toFloatOrNull()

    override fun putDouble(key: String, value: Double) = put(key, StoredValue(TYPE_DOUBLE, value.toString()))
    override fun getDouble(key: String, defaultValue: Double): Double = getDoubleOrNull(key) ?: defaultValue
    override fun getDoubleOrNull(key: String): Double? = get(key, TYPE_DOUBLE)?.toDoubleOrNull()

    override fun putBoolean(key: String, value: Boolean) = put(key, StoredValue(TYPE_BOOLEAN, value.toString()))
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = getBooleanOrNull(key) ?: defaultValue
    override fun getBooleanOrNull(key: String): Boolean? = get(key, TYPE_BOOLEAN)?.toBooleanStrictOrNull()

    private fun get(key: String, type: String): String? = synchronized(lock) {
        state.entries[key]?.takeIf { it.type == type }?.value
    }

    private fun put(key: String, value: StoredValue) = synchronized(lock) {
        if (state.entries[key] != value) commit(state.copy(entries = state.entries + (key to value)))
    }

    /** Writes [next] to disk, then (only if that succeeded) makes it the live state. */
    private fun commit(next: StoreFile) {
        val plaintext = json.encodeToString(StoreFile.serializer(), next).encodeToByteArray()
        writeAtomically(aead.encrypt(plaintext, ASSOCIATED_DATA))
        state = next
    }

    private fun load(): StoreFile {
        // A leftover temp file is a write that never got renamed into place:
        // the store it held was never committed, so it is discarded.
        tempFile().delete()
        if (!file.exists()) return StoreFile()
        val plaintext = aead.decrypt(file.readBytes(), ASSOCIATED_DATA)
        return try {
            json.decodeFromString(StoreFile.serializer(), plaintext.decodeToString())
        } catch (e: SerializationException) {
            throw IOException("Unreadable auth store", e)
        } catch (e: IllegalArgumentException) {
            throw IOException("Unreadable auth store", e)
        }
    }

    // Write-to-temp, fsync, rename: rename within a directory is atomic, so
    // readers (and the next launch) see the old file or the new one.
    private fun writeAtomically(bytes: ByteArray) {
        file.parentFile?.mkdirs()
        val temp = tempFile()
        FileOutputStream(temp).use { out ->
            out.write(bytes)
            out.fd.sync()
        }
        if (!temp.renameTo(file)) {
            temp.delete()
            throw IOException("Could not replace ${file.name}")
        }
    }

    private fun tempFile() = File(file.parentFile, "${file.name}.tmp")

    @Serializable
    private data class StoreFile(
        // Stores written while the auth store still imported an older file
        // also hold a "legacyImported" flag; ignoreUnknownKeys skips it.
        val entries: Map<String, StoredValue> = emptyMap(),
    )

    @Serializable
    private data class StoredValue(val type: String, val value: String)

    private companion object {
        // Binds the ciphertext to this store, so no other blob encrypted
        // with the same key can be swapped in for it.
        val ASSOCIATED_DATA = "cocktailcraft_auth_store/v1".encodeToByteArray()

        val json = Json { ignoreUnknownKeys = true }

        const val TYPE_STRING = "s"
        const val TYPE_INT = "i"
        const val TYPE_LONG = "l"
        const val TYPE_FLOAT = "f"
        const val TYPE_DOUBLE = "d"
        const val TYPE_BOOLEAN = "b"
    }
}
