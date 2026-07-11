package com.cocktailcraft.di

import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.domain.usecase.GetCocktailDetailUseCase
import com.cocktailcraft.domain.usecase.LoadCocktailsByCategoryUseCase
import com.cocktailcraft.domain.usecase.ManageCartUseCase
import com.cocktailcraft.domain.usecase.ManageFavoritesUseCase
import com.cocktailcraft.domain.usecase.ManageOfflineModeUseCase
import com.cocktailcraft.domain.usecase.ManageOrdersUseCase
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
    // Config
    single<AppConfig> { AppConfigImpl() }

    // Use Cases
    factory { SearchCocktailsUseCase(searchRepository = get()) }
    factory { LoadCocktailsByCategoryUseCase(searchRepository = get()) }
    factory { SortCocktailsUseCase() }
    factory { GetCocktailDetailUseCase(detailRepository = get(), favoritesRepository = get(), searchRepository = get()) }
    factory { ManageFavoritesUseCase(favoritesRepository = get(), detailRepository = get()) }
    factory { ManageCartUseCase(cartRepository = get(), detailRepository = get()) }
    factory { ManageOfflineModeUseCase(offlineRepository = get(), catalogRepository = get()) }
    factory { ManageProfileUseCase(authRepository = get()) }
    factory { ManageReviewsUseCase(repository = get()) }
    factory { ManageOrdersUseCase(orderRepository = get()) }
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

    // Global-state ViewModels — single (shared across screens, persist across navigation)
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
    single { com.cocktailcraft.viewmodel.SharedOrderViewModel(manageOrdersUseCase = get(), placeOrderUseCase = get()) }
    single { com.cocktailcraft.viewmodel.SharedProfileViewModel(manageProfileUseCase = get()) }
    single { com.cocktailcraft.viewmodel.SharedOfflineModeViewModel(manageOfflineModeUseCase = get(), networkMonitor = get()) }
    single { com.cocktailcraft.viewmodel.SharedThemeViewModel(manageProfileUseCase = get()) }

    // Cross-platform sync orchestration (schedulers are platform-side)
    single { com.cocktailcraft.domain.service.BackgroundSyncService(catalogRepository = get(), networkMonitor = get()) }
}
