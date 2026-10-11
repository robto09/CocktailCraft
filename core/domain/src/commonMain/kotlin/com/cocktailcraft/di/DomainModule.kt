package com.cocktailcraft.di

import com.cocktailcraft.domain.usecase.GetCocktailDetailUseCase
import com.cocktailcraft.domain.usecase.LoadCocktailsByCategoryUseCase
import com.cocktailcraft.domain.usecase.ManageCartUseCase
import com.cocktailcraft.domain.usecase.ManageFavoritesUseCase
import com.cocktailcraft.domain.usecase.ManageProfileUseCase
import com.cocktailcraft.domain.usecase.ManageReviewsUseCase
import com.cocktailcraft.domain.usecase.PlaceOrderUseCase
import com.cocktailcraft.domain.usecase.SearchCocktailsUseCase
import com.cocktailcraft.domain.usecase.SortCocktailsUseCase
import org.koin.dsl.module

/**
 * Koin module for the domain layer's use cases. The shared ViewModels that
 * consume them are wired in presentationModule; the repositories they need
 * come from the data layer's modules.
 */
val domainModule = module {
    // AppConfig binding lives in each platformModule() (AR-4): the platform
    // knows whether this is a debug build; commonMain doesn't.

    // Use Cases.
    // Convention (AR-8): a use case must EARN its indirection — validation,
    // orchestration, or aggregation across repositories (e.g. Sort/Analyze
    // hold real logic; ManageProfile serves two ViewModels). A pure 1:1
    // pass-through with a single consumer is collapsed onto the repository
    // interface instead (ManageOrdersUseCase was removed this way — don't
    // reintroduce shells).
    factory { SearchCocktailsUseCase(searchRepository = get()) }
    factory { LoadCocktailsByCategoryUseCase(searchRepository = get()) }
    factory { SortCocktailsUseCase() }
    factory { GetCocktailDetailUseCase(detailRepository = get(), favoritesRepository = get(), searchRepository = get()) }
    factory { ManageFavoritesUseCase(favoritesRepository = get(), detailRepository = get()) }
    factory { ManageCartUseCase(cartRepository = get(), detailRepository = get()) }
    factory { ManageProfileUseCase(authRepository = get()) }
    factory { ManageReviewsUseCase(repository = get()) }
    factory { com.cocktailcraft.domain.usecase.AnalyzeCocktailUseCase() }
    factory { PlaceOrderUseCase(orderRepository = get()) }
}
