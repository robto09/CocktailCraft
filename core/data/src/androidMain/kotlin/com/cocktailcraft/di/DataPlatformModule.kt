package com.cocktailcraft.di

import android.content.Context
import android.content.pm.ApplicationInfo
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.data.security.LazySettings
import com.cocktailcraft.data.security.migrateAuthStorage
import com.cocktailcraft.data.security.openAuthStore
import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.util.AndroidNetworkMonitor
import com.cocktailcraft.util.NetworkMonitor
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.OkHttpClient
import org.koin.dsl.module

actual fun dataPlatformModule() = module {
    // Debug-aware config (AR-4): androidApp's BuildConfig is invisible to this
    // module, but the debuggable flag carries the same signal.
    single<AppConfig> {
        val debuggable = (get<Context>().applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        AppConfigImpl(verboseNetworkLogging = debuggable)
    }

    single<Settings> {
        val context = get<Context>()
        val sharedPrefs = context.getSharedPreferences("cocktailcraft_prefs", Context.MODE_PRIVATE)
        SharedPreferencesSettings(sharedPrefs)
    }

    // Encrypted store for credentials and profile PII: Tink AES-256-GCM
    // with a Keystore-wrapped keyset (openAuthStore). Neither the store nor
    // the keyset is backed up (no_backup/ + backup rules): the Keystore key
    // never leaves the device, so a restored copy would be unreadable.
    // Opened lazily (LazySettings): resolving this sits on the first frame's
    // injection path, but the Keystore/Tink/disk work and the migrations
    // only run on first access, which AuthRepositoryImpl makes on its io
    // dispatcher.
    single<Settings>(secureSettingsQualifier) {
        val context = get<Context>()
        LazySettings {
            // openAuthStore also imports the EncryptedSharedPreferences file
            // the previous versions used.
            openAuthStore(context).also { secure ->
                // Move auth data written by even older versions out of the plain store.
                migrateAuthStorage(from = get(), to = secure)
            }
        }
    }

    // Network monitoring
    single<NetworkMonitor> { AndroidNetworkMonitor(get<Context>()) }

    // The app's one OkHttpClient. Ktor's engine (below) and androidApp's Coil
    // ImageLoader both derive from it via newBuilder(), sharing its
    // connection pool and TLS sessions — TheCocktailDB serves the API and the
    // images from the same host. Certificate pinning is unaffected: it lives
    // in network_security_config.xml, enforced by the platform trust manager
    // OkHttp uses, not in an OkHttp CertificatePinner.
    single<OkHttpClient> { OkHttpClient.Builder().build() }

    // Picked up by networkModule; timeouts come from its HttpTimeout config
    single<HttpClientEngine> { OkHttp.create { preconfigured = get<OkHttpClient>() } }

    // SQLite driver for the app database (offline cocktail cache, cart,
    // orders, favorites, reviews); it runs the .sqm migrations when an older
    // schema version is opened. The file is opened on first query, not here.
    // Excluded from backup like the prefs file that used to hold this data
    // (see backup_rules.xml).
    single<SqlDriver> {
        AndroidSqliteDriver(CocktailCraftDatabase.Schema, get<Context>(), COCKTAIL_DATABASE_NAME)
    }
}
