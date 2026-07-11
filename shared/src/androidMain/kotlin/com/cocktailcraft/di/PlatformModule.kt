package com.cocktailcraft.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.cocktailcraft.data.security.migrateAuthStorage
import com.cocktailcraft.util.AndroidNetworkMonitor
import com.cocktailcraft.util.NetworkMonitor
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.dsl.module
import java.io.IOException
import java.security.GeneralSecurityException

actual fun platformModule() = module {
    single<Settings> {
        val context = get<Context>()
        val sharedPrefs = context.getSharedPreferences("cocktailcraft_prefs", Context.MODE_PRIVATE)
        SharedPreferencesSettings(sharedPrefs)
    }

    // Keystore-encrypted store for credentials and profile PII. The backup
    // rules exclude its file (cocktailcraft_secure_prefs.xml): the master key
    // never leaves the device, so a restored copy would be unreadable.
    single<Settings>(secureSettingsQualifier) {
        SharedPreferencesSettings(createSecurePrefs(get<Context>())).also { secure ->
            // Move auth data written by older versions out of the plain store.
            migrateAuthStorage(from = get(), to = secure)
        }
    }

    // Network monitoring
    single<NetworkMonitor> { AndroidNetworkMonitor(get<Context>()) }
}

private const val SECURE_PREFS_FILE = "cocktailcraft_secure_prefs"

// Keystore-backed prefs have a documented corruption class (master key
// invalidated by an OTA/vendor bug, keyset/prefs desync after a partial
// restore) where create() throws on every launch. Failing soft — wiping the
// store and starting fresh — loses the local accounts in that rare case but
// beats an unrecoverable crash loop the user can only escape via Clear Data.
private fun createSecurePrefs(context: Context): SharedPreferences =
    try {
        buildSecurePrefs(context)
    } catch (e: GeneralSecurityException) {
        recreateSecurePrefs(context)
    } catch (e: IOException) {
        recreateSecurePrefs(context)
    }

private fun recreateSecurePrefs(context: Context): SharedPreferences {
    context.deleteSharedPreferences(SECURE_PREFS_FILE)
    return buildSecurePrefs(context)
}

private fun buildSecurePrefs(context: Context): SharedPreferences {
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    return EncryptedSharedPreferences.create(
        context,
        SECURE_PREFS_FILE,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}
