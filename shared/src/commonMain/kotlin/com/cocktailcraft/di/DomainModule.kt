package com.cocktailcraft.di

import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.domain.usecase.FilterCocktailsUseCase
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
import org.koin.dsl.module

/**
 * Koin module for domain-related dependencies including use cases, domain configurations, and shared ViewModels.
 */
val domainModule = module {
    // Config
    single<AppConfig> { AppConfigImpl() }

    // Use Cases
    factory { SearchCocktailsUseCase(searchRepository = get()) }
    factory { LoadCocktailsByCategoryUseCase(searchRepository = get(), catalogRepository = get()) }
    factory { SortCocktailsUseCase() }
    factory { FilterCocktailsUseCase(catalogRepository = get()) }
    factory { GetCocktailDetailUseCase(detailRepository = get(), favoritesRepository = get(), searchRepository = get()) }
    factory { ManageFavoritesUseCase(favoritesRepository = get(), detailRepository = get()) }
    factory { ManageCartUseCase(cartRepository = get(), detailRepository = get()) }
    factory { ManageOfflineModeUseCase(offlineRepository = get(), catalogRepository = get()) }
    factory { ManageProfileUseCase(authRepository = get()) }
    factory { ManageReviewsUseCase() }
    factory { ManageOrdersUseCase(orderRepository = get()) }
    factory { PlaceOrderUseCase(orderRepository = get()) }

    // Screen-scoped ViewModels — factory (new instance per screen)
    factory { com.cocktailcraft.viewmodel.SharedCocktailListViewModel() }
    factory { com.cocktailcraft.viewmodel.SharedCocktailDetailViewModel() }
    factory { com.cocktailcraft.viewmodel.SharedReviewViewModel() }

    // Global-state ViewModels — single (shared across screens, persist across navigation)
    single { com.cocktailcraft.viewmodel.SharedHomeViewModel() }
    single { com.cocktailcraft.viewmodel.SharedCartViewModel() }
    single { com.cocktailcraft.viewmodel.SharedFavoritesViewModel() }
    single { com.cocktailcraft.viewmodel.SharedOrderViewModel() }
    single { com.cocktailcraft.viewmodel.SharedProfileViewModel() }
    single { com.cocktailcraft.viewmodel.SharedOfflineModeViewModel() }
    single { com.cocktailcraft.viewmodel.SharedThemeViewModel() }
}
