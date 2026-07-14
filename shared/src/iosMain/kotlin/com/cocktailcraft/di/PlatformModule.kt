package com.cocktailcraft.di

import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.data.security.migrateAuthStorage
import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.util.IOSNetworkMonitor
import com.cocktailcraft.util.NetworkMonitor
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.NSUserDefaultsSettings
import org.koin.dsl.module

@OptIn(ExperimentalSettingsImplementation::class, kotlin.experimental.ExperimentalNativeApi::class)
actual fun platformModule() = module {
    // Debug-aware config (AR-4): the K/N binary genuinely differs Debug vs
    // Release per Xcode configuration (the podspec forwards $CONFIGURATION).
    single<AppConfig> { AppConfigImpl(verboseNetworkLogging = Platform.isDebugBinary) }

    single<Settings> {
        NSUserDefaultsSettings(platform.Foundation.NSUserDefaults.standardUserDefaults)
    }

    // Keychain-backed store for credentials and profile PII — encrypted at
    // rest by the OS and absent from unencrypted backups, unlike the plist
    // behind NSUserDefaults.
    single<Settings>(secureSettingsQualifier) {
        val defaults = get<Settings>()
        val secure = KeychainSettings(service = "com.cocktailcraft.auth")
        // Keychain items outlive app uninstalls but NSUserDefaults does not:
        // a missing marker means this is a fresh install, so drop any stale
        // auth data instead of silently restoring the previous session —
        // matching Android, where uninstall wipes everything.
        if (!defaults.hasKey(KEYCHAIN_INSTALL_MARKER)) {
            secure.clear()
            defaults.putBoolean(KEYCHAIN_INSTALL_MARKER, true)
        }
        secure.also {
            // Move auth data written by older versions out of NSUserDefaults.
            migrateAuthStorage(from = defaults, to = it)
        }
    }

    // Network monitoring
    single<NetworkMonitor> { IOSNetworkMonitor() }
}

// Lives in NSUserDefaults (wiped on uninstall) — its absence detects a fresh
// install so stale Keychain auth data can be cleared. Not an auth key itself.
private const val KEYCHAIN_INSTALL_MARKER = "keychain_install_marker"