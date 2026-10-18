package com.cocktailcraft.di

import app.cash.sqldelight.db.SqlDriver
import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import com.cocktailcraft.data.local.LegacySettingsImport
import com.cocktailcraft.data.local.LocalDatabase
import com.cocktailcraft.data.remote.ApiRateLimiter
import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.data.repository.AuthRepositoryImpl
import com.cocktailcraft.data.repository.CartRepositoryImpl
import com.cocktailcraft.data.repository.CocktailCatalogRepositoryImpl
import com.cocktailcraft.data.repository.CocktailCategoryFetcher
import com.cocktailcraft.data.repository.CocktailDetailRepositoryImpl
import com.cocktailcraft.data.repository.CocktailFavoritesRepositoryImpl
import com.cocktailcraft.data.repository.CocktailOfflineRepositoryImpl
import com.cocktailcraft.data.repository.CocktailSearchRepositoryImpl
import com.cocktailcraft.data.repository.OrderRepositoryImpl
import com.cocktailcraft.data.repository.ReviewRepositoryImpl
import com.cocktailcraft.domain.repository.AuthRepository
import com.cocktailcraft.domain.repository.CartRepository
import com.cocktailcraft.domain.repository.CocktailCatalogRepository
import com.cocktailcraft.domain.repository.CocktailDetailRepository
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import com.cocktailcraft.domain.repository.CocktailSearchRepository
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.repository.ReviewRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * File name of the app's SQLite database: the offline cocktail cache plus the
 * user's cart, orders, favorites and reviews. The name predates the user-data
 * tables; it stays so existing installs keep their database.
 */
internal const val COCKTAIL_DATABASE_NAME = "cocktailcraft_cache.db"

/**
 * Koin module for data-related dependencies including repositories and caching.
 */
val dataModule = module {
    // JSON
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            // A JSON null for a non-nullable field with a default coerces to
            // the default instead of failing the whole decode (SH-6).
            coerceInputValues = true
        }
    }

    // Background dispatcher for database and Settings I/O and JSON (de)serialization
    single<CoroutineDispatcher>(named("ioDispatcher")) { Dispatchers.IO }

    // Offline decision — single source of truth for "is the app offline" (AR-7)
    single { com.cocktailcraft.data.cache.OfflineModePolicy(networkMonitor = get()) }

    // App database over the platform's SqlDriver (platformModule). Creating
    // it doesn't open the database file.
    single { CocktailCraftDatabase(get<SqlDriver>()) }

    // Cache. The database is injected lazily so resolving this single never
    // touches disk; the cache opens it on first use, on the io dispatcher.
    // Settings is still needed to import (once) the cache older versions
    // kept there.
    single { CocktailCache(
        database = inject<CocktailCraftDatabase>(),
        settings = get(),
        json = get(),
        appConfig = get(),
        ioDispatcher = get(named("ioDispatcher")),
        offlineModePolicy = get()
    ) }

    // Database access for the cart, orders, favorites and reviews
    // repositories. Like the cache, it opens the database lazily on the io
    // dispatcher; the first open imports (once) the data older versions kept
    // as JSON in Settings, then deletes those keys.
    single {
        LocalDatabase(
            database = inject<CocktailCraftDatabase>(),
            legacyImport = LegacySettingsImport(settings = get(), json = get()),
            ioDispatcher = get(named("ioDispatcher"))
        )
    }

    // The API's min-interval throttle, shared by every remote call site
    single { ApiRateLimiter() }

    // Remote data source: API access, DTO mapping, rate limiting
    single { CocktailRemoteDataSource(api = get(), rateLimiter = get()) }

    // Focused repositories, each owning one concern
    single<CocktailOfflineRepository> {
        CocktailOfflineRepositoryImpl(
            offlineModePolicy = get(),
            cocktailCache = get()
        )
    }

    single<CocktailFavoritesRepository> {
        CocktailFavoritesRepositoryImpl(
            localDatabase = get(),
            json = get(),
            cocktailCache = get(),
            remote = get(),
            offlineRepository = get(),
            ioDispatcher = get(named("ioDispatcher"))
        )
    }

    // Database-backed category pages, shared by category browsing (catalog
    // repository) and the search repository's category filter. A single, so
    // both see one page index and one set of in-flight requests.
    single {
        CocktailCategoryFetcher(
            remote = get(),
            cocktailCache = get(),
            offlineRepository = get()
        )
    }

    single<CocktailSearchRepository> {
        CocktailSearchRepositoryImpl(
            remote = get(),
            cocktailCache = get(),
            offlineRepository = get(),
            categoryFetcher = get()
        )
    }

    single<CocktailDetailRepository> {
        CocktailDetailRepositoryImpl(
            remote = get(),
            cocktailCache = get(),
            appConfig = get(),
            offlineRepository = get()
        )
    }

    single<CocktailCatalogRepository> {
        CocktailCatalogRepositoryImpl(remote = get(), categoryFetcher = get())
    }

    single<CartRepository> {
        CartRepositoryImpl(
            localDatabase = get(),
            json = get(),
            ioDispatcher = get(named("ioDispatcher"))
        )
    }

    single<AuthRepository> {
        AuthRepositoryImpl(get(secureSettingsQualifier), get(), get(named("ioDispatcher")))
    }

    single<OrderRepository> {
        OrderRepositoryImpl(
            localDatabase = get(),
            ioDispatcher = get(named("ioDispatcher"))
        )
    }

    single<ReviewRepository> {
        ReviewRepositoryImpl(
            localDatabase = get(),
            ioDispatcher = get(named("ioDispatcher"))
        )
    }
}
