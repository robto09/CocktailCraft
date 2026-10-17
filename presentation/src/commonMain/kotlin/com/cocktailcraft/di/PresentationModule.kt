package com.cocktailcraft.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import kotlin.native.HiddenFromObjC

/**
 * Koin module for the shared ViewModels. Their use cases come from
 * domainModule, their repositories and NetworkMonitor from the data layer.
 */
@HiddenFromObjC // Kotlin-side wiring; Swift only calls doInitKoin()/KoinHelper (AR-12)
val presentationModule = module {
    // Screen-scoped ViewModels — new instance per screen; the viewModel DSL
    // lets Android's koinViewModel() scope them to the nav back-stack entry
    viewModel {
        com.cocktailcraft.viewmodel.SharedCocktailDetailViewModel(
            getCocktailDetailUseCase = get(),
            manageFavoritesUseCase = get(),
            manageCartUseCase = get()
        )
    }
    viewModel { com.cocktailcraft.viewmodel.SharedReviewViewModel(manageReviewsUseCase = get()) }

    // Global-state ViewModels — single (shared across screens, persist across
    // navigation). They extend androidx.lifecycle.ViewModel, but a Koin
    // `single` is never routed through a ViewModelStore, so onCleared() NEVER
    // runs on them: any init{}-launched collector lives for the whole process
    // by design (SH-9). Do not put teardown in onCleared() for these, and do
    // not stop process-wide services from them (the since-removed offline-mode
    // ViewModel once stopped the shared NetworkMonitor that way — fixed in SH-2).
    //
    // Process death (AN-4): these singletons carry no SavedStateHandle — the
    // same instances are handed to iOS via KoinHelper, where no such concept
    // exists — so a process kill resets them to default state. That is an
    // accepted, documented limitation; high-value UI state that must survive
    // (e.g. the in-progress Home search) is shadowed platform-side with
    // rememberSaveable and replayed (see HomeScreen.kt).
    single {
        com.cocktailcraft.viewmodel.SharedHomeViewModel(
            searchCocktailsUseCase = get(),
            loadCocktailsByCategoryUseCase = get(),
            sortCocktailsUseCase = get(),
            manageFavoritesUseCase = get(),
            offlineRepository = get(),
            getCocktailDetailUseCase = get(),
            catalogRepository = get(),
            networkMonitor = get()
        )
    }
    single { com.cocktailcraft.viewmodel.SharedCartViewModel(manageCartUseCase = get()) }
    single { com.cocktailcraft.viewmodel.SharedFavoritesViewModel(manageFavoritesUseCase = get()) }
    single { com.cocktailcraft.viewmodel.SharedOrderViewModel(orderRepository = get(), placeOrderUseCase = get()) }
    single { com.cocktailcraft.viewmodel.SharedProfileViewModel(manageProfileUseCase = get()) }
    single { com.cocktailcraft.viewmodel.SharedThemeViewModel(manageProfileUseCase = get()) }
}
