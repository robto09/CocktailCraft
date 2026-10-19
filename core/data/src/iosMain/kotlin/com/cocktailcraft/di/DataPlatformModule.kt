package com.cocktailcraft.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.sqliter.DatabaseFileContext
import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.data.local.protectDatabaseFiles
import com.cocktailcraft.data.security.LazySettings
import com.cocktailcraft.data.security.migrateAuthStorage
import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.util.IOSNetworkMonitor
import com.cocktailcraft.util.NetworkMonitor
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import org.koin.dsl.module

@OptIn(ExperimentalSettingsImplementation::class, kotlin.experimental.ExperimentalNativeApi::class)
actual fun dataPlatformModule() = module {
    // Debug-aware config: the K/N binary genuinely differs Debug vs
    // Release per Xcode configuration (embedAndSignAppleFrameworkForXcode builds
    // the framework variant matching $CONFIGURATION).
    single<AppConfig> { AppConfigImpl(verboseNetworkLogging = Platform.isDebugBinary) }

    single<Settings> {
        NSUserDefaultsSettings(platform.Foundation.NSUserDefaults.standardUserDefaults)
    }

    // Keychain-backed store for credentials and profile PII — encrypted at
    // rest by the OS and absent from unencrypted backups, unlike the plist
    // behind NSUserDefaults.
    // Opened lazily (LazySettings), as on Android: the Keychain reads and
    // the migration run on first access — on AuthRepositoryImpl's io
    // dispatcher — not when the theme ViewModel's graph resolves at launch.
    single<Settings>(secureSettingsQualifier) {
        LazySettings {
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
    }

    // Network monitoring
    single<NetworkMonitor> { IOSNetworkMonitor() }

    // SQLite driver for the app database (offline cocktail cache, cart,
    // orders, favorites, reviews); it runs the .sqm migrations when an older
    // schema version is opened. sqlite3 itself is linked into the app via
    // OTHER_LDFLAGS in iosApp/project.yml (the framework is static).
    // Its files are excluded from backup and locked with the device
    // (protectDatabaseFiles) before the driver can open them.
    single<SqlDriver> {
        protectDatabaseFiles(DatabaseFileContext.databasePath(COCKTAIL_DATABASE_NAME, null))
        NativeSqliteDriver(CocktailCraftDatabase.Schema, COCKTAIL_DATABASE_NAME)
    }
}

// Lives in NSUserDefaults (wiped on uninstall) — its absence detects a fresh
// install so stale Keychain auth data can be cleared. Not an auth key itself.
private const val KEYCHAIN_INSTALL_MARKER = "keychain_install_marker"