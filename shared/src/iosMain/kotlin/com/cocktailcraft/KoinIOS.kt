package com.cocktailcraft

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.AuthRepository
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.service.BackgroundSyncService
import com.cocktailcraft.domain.repository.CartRepository
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.viewmodel.SharedFavoritesViewModel
import com.cocktailcraft.viewmodel.SharedHomeViewModel
import com.cocktailcraft.viewmodel.SharedCartViewModel
import com.cocktailcraft.viewmodel.SharedCocktailDetailViewModel
import com.cocktailcraft.viewmodel.SharedOfflineModeViewModel
import com.cocktailcraft.viewmodel.SharedOrderViewModel
import com.cocktailcraft.viewmodel.SharedProfileViewModel
import com.cocktailcraft.viewmodel.SharedThemeViewModel
import com.cocktailcraft.viewmodel.SharedReviewViewModel
import com.cocktailcraft.util.NetworkMonitor
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

fun doInitKoin(): KoinApplication = initKoin {}

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

    fun getBackgroundSyncService(): BackgroundSyncService = get()

    /**
     * One-shot favorites snapshot for the iOS home-screen widget bridge.
     * Throws on a failed fetch (via getOrThrow) instead of returning an
     * empty list: the bridge must be able to tell "no favorites" apart
     * from "fetch failed" so a transient failure never wipes the widget's
     * last-known-good snapshot.
     */
    suspend fun getFavoriteCocktailsSnapshot(): List<Cocktail> =
        get<CocktailFavoritesRepository>().getFavoriteCocktails().getOrThrow()

    // Shared ViewModels — screen-scoped (viewModel {} DSL): fresh instance per
    // resolution; iOS wrappers own the lifecycle and call onCleared() in deinit

    fun getSharedCocktailDetailViewModel(): SharedCocktailDetailViewModel = get()

    fun getSharedReviewViewModel(): SharedReviewViewModel = get()

    // Shared ViewModels — global-state (single): process-wide instances shared
    // with Android's screens; wrappers must NOT call onCleared() on these

    fun getSharedFavoritesViewModel(): SharedFavoritesViewModel = get()

    fun getSharedHomeViewModel(): SharedHomeViewModel = get()

    fun getSharedCartViewModel(): SharedCartViewModel = get()

    fun getSharedOfflineModeViewModel(): SharedOfflineModeViewModel = get()

    fun getSharedOrderViewModel(): SharedOrderViewModel = get()

    fun getSharedProfileViewModel(): SharedProfileViewModel = get()

    fun getSharedThemeViewModel(): SharedThemeViewModel = get()
}
