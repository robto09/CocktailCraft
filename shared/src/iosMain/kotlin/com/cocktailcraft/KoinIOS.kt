package com.cocktailcraft

import androidx.lifecycle.ViewModelStore
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailDetailRepository
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrDefault
import com.cocktailcraft.util.NetworkMonitor
import com.cocktailcraft.viewmodel.SharedCartViewModel
import com.cocktailcraft.viewmodel.SharedCocktailDetailViewModel
import com.cocktailcraft.viewmodel.SharedFavoritesViewModel
import com.cocktailcraft.viewmodel.SharedHomeViewModel
import com.cocktailcraft.viewmodel.SharedOrderViewModel
import com.cocktailcraft.viewmodel.SharedProfileViewModel
import com.cocktailcraft.viewmodel.SharedReviewViewModel
import com.cocktailcraft.viewmodel.SharedThemeViewModel
import com.cocktailcraft.viewmodel.SharedViewModel
import kotlinx.coroutines.flow.Flow
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

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

    // Services / platform bridges

    fun getNetworkMonitor(): NetworkMonitor = get()

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

    // Debug tools (debugClearFavorites, debugSetSimulatedNetworkFailure, ...)
    // are extensions in src/iosTestMode (DebugTools.kt), compiled into Debug
    // frameworks only.
}
