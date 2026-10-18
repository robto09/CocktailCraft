package com.cocktailcraft.data.security

import android.content.Context
import co.touchlab.kermit.Logger
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.crypto.tink.integration.android.AndroidKeystore
import java.io.File
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.ProviderException

/**
 * Opens the Android auth store: an [EncryptedFileSettings] in
 * no_backup/, encrypted with a Tink AES-256-GCM keyset that is itself
 * wrapped by an Android Keystore key (the keyset lives, encrypted, in the
 * cocktailcraft_auth_keyset prefs file). Neither the store nor the keyset
 * is backed up: the Keystore key never leaves the device, so a restored
 * copy could not be decrypted.
 *
 * Runs the one-time move out of the legacy EncryptedSharedPreferences file
 * ([importLegacySecurePrefs]) before returning.
 *
 * Keystore-backed storage has a documented corruption class (key
 * invalidated by an OTA/vendor bug, keyset/data desync after a partial
 * restore) where opening throws on every launch. Failing soft — wiping the
 * store, keyset and master key and starting fresh — loses the local
 * accounts in that rare case but beats an unrecoverable crash loop the user
 * can only escape via Clear Data.
 */
internal fun openAuthStore(context: Context): EncryptedFileSettings {
    val store = try {
        buildAuthStore(context)
    } catch (e: GeneralSecurityException) {
        recreateAuthStore(context, e)
    } catch (e: IOException) {
        recreateAuthStore(context, e)
    } catch (e: ProviderException) {
        recreateAuthStore(context, e)
    }
    importLegacySecurePrefs(store, LegacySecurePrefs(context))
    return store
}

private fun recreateAuthStore(context: Context, cause: Exception): EncryptedFileSettings {
    Logger.withTag(LogTag).e(cause) { "Auth store unreadable; starting a fresh one" }
    authStoreFile(context).delete()
    context.deleteSharedPreferences(KeysetPrefsFile)
    try {
        // A master key that exists but is unusable would fail the rebuild too
        AndroidKeystore.deleteKey(MasterKeyAlias)
    } catch (ignored: GeneralSecurityException) {
        // Already gone, or the Keystore is unavailable; the rebuild decides
    }
    return buildAuthStore(context)
}

private fun buildAuthStore(context: Context): EncryptedFileSettings {
    AeadConfig.register()
    val keysetManager = AndroidKeysetManager.Builder()
        .withSharedPref(context, KeysetName, KeysetPrefsFile)
        .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
        .withMasterKeyUri("android-keystore://$MasterKeyAlias")
        .build()
    if (!keysetManager.isUsingKeystore) {
        // Tink falls back to an unwrapped keyset when the device has no
        // usable Keystore, as EncryptedSharedPreferences did; the file
        // still never leaves the device (no_backup/, backup rules).
        Logger.withTag(LogTag).w { "Android Keystore unavailable; auth keyset is not hardware-wrapped" }
    }
    val aead = keysetManager.keysetHandle.getPrimitive(RegistryConfiguration.get(), Aead::class.java)
    return EncryptedFileSettings(authStoreFile(context), aead)
}

// In no_backup/: excluded from Auto Backup by the OS, and by name in the
// backup rules as well.
private fun authStoreFile(context: Context) = File(context.noBackupFilesDir, AuthStoreFileName)

private const val LogTag = "AuthStore"
private const val AuthStoreFileName = "cocktailcraft_auth_store"
private const val KeysetPrefsFile = "cocktailcraft_auth_keyset"
private const val KeysetName = "auth_store_keyset"
private const val MasterKeyAlias = "cocktailcraft_auth_master_key"
