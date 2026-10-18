package com.cocktailcraft

import androidx.lifecycle.ViewModelStore
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.AuthRepository
import com.cocktailcraft.domain.repository.CocktailDetailRepository
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import com.cocktailcraft.domain.repository.CartRepository
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.repository.ReviewRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrDefault
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.viewmodel.SharedFavoritesViewModel
import com.cocktailcraft.viewmodel.SharedHomeViewModel
import com.cocktailcraft.viewmodel.SharedCartViewModel
import com.cocktailcraft.viewmodel.SharedCocktailDetailViewModel
import com.cocktailcraft.viewmodel.SharedOrderViewModel
import com.cocktailcraft.viewmodel.SharedProfileViewModel
import com.cocktailcraft.viewmodel.SharedThemeViewModel
import com.cocktailcraft.viewmodel.SharedReviewViewModel
import com.cocktailcraft.viewmodel.SharedViewModel
import com.cocktailcraft.util.NetworkMonitor
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import kotlinx.coroutines.flow.Flow
import kotlinx.io.IOException
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.concurrent.Volatile

/**
 * iOS app init. [isDebug] is the app's own `#if DEBUG` — release builds keep
 * only warning/error logs (see configureLogging).
 */
fun doInitKoin(isDebug: Boolean): KoinApplication = initKoin(isDebug) {}

/**
 * Helper class to provide dependencies to iOS without using reified generics.
 * Explicit return types define the exported Objective-C surface.
 *
 * OBLIGATION (AR-6): every new shared single/factory that iOS needs must get
 * an explicit getter here — a missing getter silently hides the binding from
 * Swift. Add it under the matching section below AND add a resolution
 * assertion to KoinHelperTest (shared/src/iosTest) so a missing or renamed
 * binding fails a test instead of crashing Swift at first use.
 */
class KoinHelper : KoinComponent {

    // Repositories

    fun getAuthRepository(): AuthRepository = get()

    fun getCartRepository(): CartRepository = get()

    fun getOrderRepository(): OrderRepository = get()

    // Services / platform bridges

    fun getNetworkMonitor(): NetworkMonitor = get()

    /**
     * One-shot favorites snapshot for the iOS home-screen widget bridge.
     * Throws on a failed fetch (via getOrThrow) instead of returning an
     * empty list: the bridge must be able to tell "no favorites" apart
     * from "fetch failed" so a transient failure never wipes the widget's
     * last-known-good snapshot.
     */
    suspend fun getFavoriteCocktailsSnapshot(): List<Cocktail> =
        get<CocktailFavoritesRepository>().getFavoriteCocktails().getOrThrow()

    /**
     * Favorites as they change (any add/remove, from any screen), so the
     * widget bridge can keep the favorites widget's snapshot current while
     * the app runs instead of only at launch/backgrounding.
     */
    fun observeFavoriteCocktails(): Flow<List<Cocktail>> =
        get<CocktailFavoritesRepository>().observeFavorites()

    /**
     * Cocktails for the Random Cocktail widget's App Group snapshot: a fresh
     * random pick when online (best-effort — a failed fetch is skipped),
     * followed by the recently viewed cocktails, which keep the widget
     * populated offline, like Android's WidgetDataProvider fallback.
     * At most [limit], without duplicates.
     */
    suspend fun getRandomCocktailWidgetPool(limit: Int): List<Cocktail> {
        val offline = get<CocktailOfflineRepository>()
        val random = if (offline.isOffline()) {
            null
        } else {
            (get<CocktailDetailRepository>().getRandomCocktail() as? Result.Success)?.data
        }
        val recent = offline.getRecentlyViewedCocktails().getOrDefault(emptyList())
        return (listOfNotNull(random) + recent).distinctBy { it.id }.take(limit)
    }

    // Shared ViewModels — screen-scoped (viewModel {} DSL): fresh instance per
    // resolution; iOS wrappers own the lifecycle and release it through
    // clearScreenViewModel() when they deinit

    fun getSharedCocktailDetailViewModel(): SharedCocktailDetailViewModel = get()

    fun getSharedReviewViewModel(): SharedReviewViewModel = get()

    /**
     * Clears a screen-scoped ViewModel the way Android's ViewModelStore does
     * when its screen goes away: cancels viewModelScope (and any other
     * closeables), then runs onCleared(). Calling onCleared() directly only
     * runs the hook and leaves viewModelScope — and every collector the
     * ViewModel launched in it — running. Call on the main thread, once.
     */
    fun clearScreenViewModel(viewModel: SharedViewModel) {
        ViewModelStore().apply { put("cleared", viewModel) }.clear()
    }

    // Shared ViewModels — global-state (single): process-wide instances shared
    // with Android's screens; wrappers must NOT clear these

    fun getSharedFavoritesViewModel(): SharedFavoritesViewModel = get()

    fun getSharedHomeViewModel(): SharedHomeViewModel = get()

    fun getSharedCartViewModel(): SharedCartViewModel = get()

    fun getSharedOrderViewModel(): SharedOrderViewModel = get()

    fun getSharedProfileViewModel(): SharedProfileViewModel = get()

    fun getSharedThemeViewModel(): SharedThemeViewModel = get()

    // Debug tools — called only from the Debug-build QA menu (DebugSwift's
    // custom actions in CocktailCraftApp.swift). The shared ViewModels
    // observe the database, so every screen updates on its own.

    /** Removes every favorite. */
    suspend fun debugClearFavorites() {
        val favorites = get<CocktailFavoritesRepository>()
        favorites.getFavoriteCocktails().getOrDefault(emptyList()).forEach {
            favorites.removeFromFavorites(it)
        }
    }

    /** Empties the cart. */
    suspend fun debugClearCart() {
        get<CartRepository>().clearCart()
    }

    /**
     * Deletes the user's local data — cart, favorites, orders and reviews —
     * leaving settings (Settings > Reset to defaults) and sign-in alone.
     */
    suspend fun debugResetUserData() {
        debugClearCart()
        debugClearFavorites()
        val orders = get<OrderRepository>()
        orders.getOrders().getOrDefault(emptyList()).forEach { orders.deleteOrder(it.id) }
        val reviews = get<ReviewRepository>()
        reviews.getReviews().getOrDefault(emptyList()).forEach { reviews.deleteReview(it.id) }
    }

    /**
     * While on, every request through the shared HttpClient fails as a
     * transport error — the path a dropped connection takes (retries, then
     * the offline-cache fallback and the network error UI) — without
     * touching the device's connectivity, so NetworkMonitor still reports
     * online.
     */
    fun debugSetSimulatedNetworkFailure(enabled: Boolean) {
        SimulatedNetworkFailure.installOn(get())
        SimulatedNetworkFailure.enabled = enabled
    }

    fun debugIsSimulatingNetworkFailure(): Boolean = SimulatedNetworkFailure.enabled
}

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
