package com.cocktailcraft.di

import com.cocktailcraft.domain.usecase.GetCocktailDetailUseCase
import com.cocktailcraft.domain.usecase.LoadCocktailsByCategoryUseCase
import com.cocktailcraft.domain.usecase.ManageCartUseCase
import com.cocktailcraft.domain.usecase.ManageFavoritesUseCase
import com.cocktailcraft.domain.usecase.ManageReviewsUseCase
import com.cocktailcraft.domain.usecase.PlaceOrderUseCase
import com.cocktailcraft.domain.usecase.SearchCocktailsUseCase
import com.cocktailcraft.domain.usecase.SortCocktailsUseCase
import org.koin.dsl.module
import kotlin.native.HiddenFromObjC

/**
 * Koin module for the domain layer's use cases. Lives here, next to
 * presentationModule (which wires the ViewModels that consume them), so
 * :core:domain itself stays free of DI; the repositories come from the
 * data layer's modules.
 */
@HiddenFromObjC // Kotlin-side wiring; Swift only calls doInitKoin()/KoinHelper
val domainModule = module {
    // AppConfig binding lives in each platformModule(): the platform
    // knows whether this is a debug build; commonMain doesn't.

    // Use Cases.
    // Convention: a use case must EARN its indirection — validation,
    // orchestration, or aggregation across repositories (e.g. Sort, Search,
    // PlaceOrder hold real logic). A pure pass-through is collapsed onto
    // the repository interface instead, and ViewModels call that
    // repository directly (ManageOrdersUseCase and ManageProfileUseCase
    // were removed this way — don't reintroduce shells). So a ViewModel
    // depends on a use case where there is logic and on the repository
    // where there is none.
    factory { SearchCocktailsUseCase(searchRepository = get()) }
    factory { LoadCocktailsByCategoryUseCase(catalogRepository = get()) }
    factory { SortCocktailsUseCase() }
    factory {
        GetCocktailDetailUseCase(detailRepository = get(), favoritesRepository = get(), catalogRepository = get())
    }
    factory { ManageFavoritesUseCase(favoritesRepository = get(), detailRepository = get()) }
    factory { ManageCartUseCase(cartRepository = get(), detailRepository = get()) }
    factory { ManageReviewsUseCase(repository = get()) }
    factory { PlaceOrderUseCase(orderRepository = get()) }
}
