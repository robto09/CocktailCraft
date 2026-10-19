package com.cocktailcraft

import com.cocktailcraft.data.local.deleteEveryOwnersUserData
import com.cocktailcraft.domain.repository.CartRepository
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.util.getOrDefault
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import kotlinx.io.IOException
import org.koin.core.component.get
import kotlin.concurrent.Volatile

// Debug tools — called only from the Debug-build QA menu (DebugSwift's custom
// actions in CocktailCraftApp.swift, all under #if DEBUG). They live in the
// iOS test mode source set, so Release frameworks don't contain them (see
// includeIosTestMode in shared/build.gradle.kts). Extensions on KoinHelper
// export to Swift as KoinHelper methods, so the call sites read the same.
// The shared ViewModels observe the database, so every screen updates on its
// own. The suspend hooks declare @Throws: Swift awaits them, and an
// undeclared exception reaching Swift terminates the app.

/** Removes the current user's (or, signed out, the guest's) favorites. */
@Throws(Exception::class)
suspend fun KoinHelper.debugClearFavorites() {
    val favorites = get<CocktailFavoritesRepository>()
    favorites.getFavoriteCocktails().getOrDefault(emptyList()).forEach {
        favorites.removeFromFavorites(it)
    }
}

/** Empties the current user's (or, signed out, the guest's) cart. */
@Throws(Exception::class)
suspend fun KoinHelper.debugClearCart() {
    get<CartRepository>().clearCart()
}

/**
 * Deletes every owner's local data — the cart, favorites, orders and
 * reviews of every user on the device and of the guest — leaving settings
 * (Settings > Reset to defaults), accounts and sign-in alone.
 */
@Throws(Exception::class)
suspend fun KoinHelper.debugResetUserData() {
    getKoin().deleteEveryOwnersUserData()
}

/**
 * While on, every request through the shared HttpClient fails as a
 * transport error — the path a dropped connection takes (retries, then
 * the offline-cache fallback and the network error UI) — without
 * touching the device's connectivity, so NetworkMonitor still reports
 * online.
 */
fun KoinHelper.debugSetSimulatedNetworkFailure(enabled: Boolean) {
    SimulatedNetworkFailure.installOn(get())
    SimulatedNetworkFailure.enabled = enabled
}

fun KoinHelper.debugIsSimulatingNetworkFailure(): Boolean = SimulatedNetworkFailure.enabled

/** Debug hook behind KoinHelper.debugSetSimulatedNetworkFailure. */
private object SimulatedNetworkFailure {
    @Volatile
    var enabled = false

    // The client the interceptor was added to: the app's Koin single, so in
    // practice set once (a restarted graph, as in tests, brings a new client)
    @Volatile
    private var installedOn: HttpClient? = null

    /** Adds the failing interceptor to [client] once; it is a no-op while disabled. */
    fun installOn(client: HttpClient) {
        if (installedOn === client) return
        installedOn = client
        client.plugin(HttpSend).intercept { request ->
            if (enabled) throw IOException("Simulated network failure (Debug menu)")
            execute(request)
        }
    }
}
