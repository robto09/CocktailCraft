package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailCatalogRepository
import com.cocktailcraft.domain.usecase.LoadCocktailsByCategoryUseCase
import com.cocktailcraft.domain.usecase.ManageFavoritesUseCase
import com.cocktailcraft.domain.usecase.ManageReviewsUseCase
import com.cocktailcraft.domain.usecase.SearchCocktailsUseCase
import com.cocktailcraft.domain.usecase.SortCocktailsUseCase
import com.cocktailcraft.testutil.FakeCatalogRepository
import com.cocktailcraft.testutil.FakeDetailRepository
import com.cocktailcraft.testutil.FakeNetworkMonitor
import com.cocktailcraft.testutil.FakeOfflineRepository
import com.cocktailcraft.testutil.FakeReviewRepository
import com.cocktailcraft.testutil.FakeSearchRepository
import com.cocktailcraft.testutil.FlakyFavoritesRepository

/** All fakes behind a real use-case stack, so tests exercise VM + domain together. */
internal class HomeViewModelHarness {
    val search = FakeSearchRepository()
    val catalog = FakeCatalogRepository()
    val detail = FakeDetailRepository()
    val favorites = FlakyFavoritesRepository()
    val offline = FakeOfflineRepository()
    val network = FakeNetworkMonitor()
    val reviews = FakeReviewRepository()

    /** What the network serves: search results and category pages alike. */
    var all: List<Cocktail>
        get() = search.all
        set(value) {
            search.all = value
            catalog.all = value
        }

    /** Fails (or, with null, un-fails) every search and category refresh. */
    var errorMessage: String?
        get() = search.errorMessage
        set(value) {
            search.errorMessage = value
            catalog.errorMessage = value
        }

    /** [catalogRepository] stands in for [catalog] where a test needs to wrap it. */
    fun viewModel(catalogRepository: CocktailCatalogRepository = catalog) = SharedHomeViewModel(
        searchCocktailsUseCase = SearchCocktailsUseCase(search),
        loadCocktailsByCategoryUseCase = LoadCocktailsByCategoryUseCase(catalogRepository),
        sortCocktailsUseCase = SortCocktailsUseCase(),
        manageFavoritesUseCase = ManageFavoritesUseCase(favorites, detail),
        offlineRepository = offline,
        catalogRepository = catalogRepository,
        networkMonitor = network,
        manageReviewsUseCase = ManageReviewsUseCase(reviews)
    )
}
