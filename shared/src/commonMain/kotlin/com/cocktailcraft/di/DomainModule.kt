package com.cocktailcraft.di

import com.cocktailcraft.domain.usecase.GetCocktailDetailUseCase
import com.cocktailcraft.domain.usecase.LoadCocktailsByCategoryUseCase
import com.cocktailcraft.domain.usecase.ManageCartUseCase
import com.cocktailcraft.domain.usecase.ManageFavoritesUseCase
import com.cocktailcraft.domain.usecase.ManageOfflineModeUseCase
import com.cocktailcraft.domain.usecase.ManageProfileUseCase
import com.cocktailcraft.domain.usecase.ManageReviewsUseCase
import com.cocktailcraft.domain.usecase.PlaceOrderUseCase
import com.cocktailcraft.domain.usecase.SearchCocktailsUseCase
import com.cocktailcraft.domain.usecase.SortCocktailsUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for domain-related dependencies including use cases, domain configurations, and shared ViewModels.
 */
val domainModule = module {
    // AppConfig binding lives in each platformModule() (AR-4): the platform
    // knows whether this is a debug build; commonMain doesn't.

    // Use Cases.
    // Convention (AR-8): a use case must EARN its indirection — validation,
    // orchestration, or aggregation across repositories (e.g. Sort/Analyze
    // hold real logic; ManageOfflineMode aggregates two repositories;
    // ManageProfile serves two ViewModels). A pure 1:1 pass-through with a
    // single consumer is collapsed onto the repository interface instead
    // (ManageOrdersUseCase was removed this way — don't reintroduce shells).
    factory { SearchCocktailsUseCase(searchRepository = get()) }
    factory { LoadCocktailsByCategoryUseCase(searchRepository = get()) }
    factory { SortCocktailsUseCase() }
    factory { GetCocktailDetailUseCase(detailRepository = get(), favoritesRepository = get(), searchRepository = get()) }
    factory { ManageFavoritesUseCase(favoritesRepository = get(), detailRepository = get()) }
    factory { ManageCartUseCase(cartRepository = get(), detailRepository = get()) }
    factory { ManageOfflineModeUseCase(offlineRepository = get(), catalogRepository = get()) }
    factory { ManageProfileUseCase(authRepository = get()) }
    factory { ManageReviewsUseCase(repository = get()) }
    factory { com.cocktailcraft.domain.usecase.AnalyzeCocktailUseCase() }
    factory { PlaceOrderUseCase(orderRepository = get()) }

    // Screen-scoped ViewModels — new instance per screen; the viewModel DSL
    // lets Android's koinViewModel() scope them to the nav back-stack entry
    viewModel {
        com.cocktailcraft.viewmodel.SharedCocktailDetailViewModel(
            getCocktailDetailUseCase = get(),
            manageFavoritesUseCase = get(),
            manageCartUseCase = get(),
            analyzeCocktailUseCase = get()
        )
    }
    viewModel { com.cocktailcraft.viewmodel.SharedReviewViewModel(manageReviewsUseCase = get()) }

    // Global-state ViewModels — single (shared across screens, persist across
    // navigation). They extend androidx.lifecycle.ViewModel, but a Koin
    // `single` is never routed through a ViewModelStore, so onCleared() NEVER
    // runs on them: any init{}-launched collector lives for the whole process
    // by design (SH-9). Do not put teardown in onCleared() for these, and do
    // not stop process-wide services from them (SharedOfflineModeViewModel
    // once stopped the shared NetworkMonitor that way — fixed in SH-2).
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
            manageOfflineModeUseCase = get(),
            getCocktailDetailUseCase = get(),
            catalogRepository = get(),
            networkMonitor = get()
        )
    }
    single { com.cocktailcraft.viewmodel.SharedCartViewModel(manageCartUseCase = get()) }
    single { com.cocktailcraft.viewmodel.SharedFavoritesViewModel(manageFavoritesUseCase = get()) }
    single { com.cocktailcraft.viewmodel.SharedOrderViewModel(orderRepository = get(), placeOrderUseCase = get()) }
    single { com.cocktailcraft.viewmodel.SharedProfileViewModel(manageProfileUseCase = get()) }
    single { com.cocktailcraft.viewmodel.SharedOfflineModeViewModel(manageOfflineModeUseCase = get(), networkMonitor = get()) }
    single { com.cocktailcraft.viewmodel.SharedThemeViewModel(manageProfileUseCase = get()) }

    // Cross-platform sync orchestration (schedulers are platform-side)
    single { com.cocktailcraft.domain.service.BackgroundSyncService(catalogRepository = get(), networkMonitor = get()) }
}
