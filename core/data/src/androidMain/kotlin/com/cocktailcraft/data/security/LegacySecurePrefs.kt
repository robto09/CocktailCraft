package com.cocktailcraft.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import co.touchlab.kermit.Logger
import java.io.File
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.ProviderException

/**
 * Where the auth store lived before it moved to [EncryptedFileSettings]:
 * an EncryptedSharedPreferences file (androidx.security:security-crypto,
 * now deprecated), whose master key sits in the Android Keystore.
 *
 * security-crypto stays a dependency only to read this file once — its
 * format (Tink keysets stored inside the prefs, SIV-encrypted key names,
 * type-tagged GCM values) is the library's private detail, and
 * re-implementing it by hand to drop the dependency early would put every
 * upgrading user's accounts on an untested parser.
 *
 * When to remove: once no supported upgrade path starts from a build older
 * than the one that ships this migration — i.e. after a release or two,
 * since every launch of this version imports and deletes the file. Delete
 * this file, [importLegacySecurePrefs] and the security-crypto dependency
 * then; keep the backup-rule exclusion and a plain
 * `deleteSharedPreferences(LEGACY_SECURE_PREFS_FILE)` so a straggler's file
 * is still cleaned up (its accounts would be lost, as on a reinstall).
 */
internal class LegacySecurePrefs(private val context: Context) {

    // Checked on disk: EncryptedSharedPreferences.create() on a missing file
    // would create one, keysets and all.
    fun exists(): Boolean =
        File(context.applicationInfo.dataDir, "shared_prefs/$LEGACY_SECURE_PREFS_FILE.xml").exists()

    /**
     * Every entry, decrypted. Throws (GeneralSecurityException, IOException,
     * or SecurityException/ProviderException from the Keystore or a failed
     * decryption) when the file or its master key is unusable.
     */
    @Suppress("DEPRECATION")
    fun readAll(): Map<String, *> {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            LEGACY_SECURE_PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ).all
    }

    fun delete() {
        context.deleteSharedPreferences(LEGACY_SECURE_PREFS_FILE)
    }

    companion object {
        const val LEGACY_SECURE_PREFS_FILE = "cocktailcraft_secure_prefs"
    }
}

/**
 * Moves the legacy EncryptedSharedPreferences auth data into [store], then
 * deletes the legacy file. Crash-safe and idempotent:
 *
 * - the copy and the "imported" marker land in one atomic write of [store],
 *   so an interruption before it leaves the legacy file untouched and the
 *   next launch simply retries;
 * - an interruption after it (before the delete) only leaves the delete to
 *   redo — the marker stops a re-import, which could otherwise restore data
 *   the user has since changed (an old password hash, a signed-out session).
 *
 * An unreadable legacy file (Keystore key invalidated, corrupt keysets) is
 * dropped, as the old store's own corruption handling did: those accounts
 * were unrecoverable either way, and retrying every launch would not help.
 */
internal fun importLegacySecurePrefs(
    store: EncryptedFileSettings,
    exists: () -> Boolean,
    readAll: () -> Map<String, *>,
    delete: () -> Unit,
) {
    if (!store.isLegacyImported) {
        val entries = if (exists()) readLegacyOrNothing(readAll) else emptyMap<String, Any>()
        store.importLegacy(entries)
    }
    if (exists()) delete()
}

internal fun importLegacySecurePrefs(store: EncryptedFileSettings, legacy: LegacySecurePrefs) =
    importLegacySecurePrefs(store, legacy::exists, legacy::readAll, legacy::delete)

private fun readLegacyOrNothing(readAll: () -> Map<String, *>): Map<String, *> =
    try {
        readAll()
    } catch (e: GeneralSecurityException) {
        legacyUnreadable(e)
    } catch (e: IOException) {
        legacyUnreadable(e)
    } catch (e: SecurityException) {
        // EncryptedSharedPreferences reports undecryptable entries this way
        legacyUnreadable(e)
    } catch (e: ProviderException) {
        legacyUnreadable(e)
    }

private fun legacyUnreadable(cause: Exception): Map<String, *> {
    Logger.withTag("AuthStore").e(cause) { "Legacy auth store unreadable; dropping it" }
    return emptyMap<String, Any>()
}
