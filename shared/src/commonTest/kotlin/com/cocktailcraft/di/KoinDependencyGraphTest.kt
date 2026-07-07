package com.cocktailcraft.di

import com.cocktailcraft.domain.repository.AuthRepository
import com.cocktailcraft.domain.repository.CartRepository
import com.cocktailcraft.domain.repository.CocktailCatalogRepository
import com.cocktailcraft.domain.repository.CocktailDetailRepository
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.domain.repository.CocktailSearchRepository
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.repository.ReviewRepository
import com.cocktailcraft.domain.service.BackgroundSyncService
import com.cocktailcraft.testutil.FakeNetworkMonitor
import com.cocktailcraft.testutil.MainDispatcherTest
import com.cocktailcraft.util.NetworkMonitor
import com.cocktailcraft.viewmodel.SharedCartViewModel
import com.cocktailcraft.viewmodel.SharedCocktailDetailViewModel
import com.cocktailcraft.viewmodel.SharedFavoritesViewModel
import com.cocktailcraft.viewmodel.SharedHomeViewModel
import com.cocktailcraft.viewmodel.SharedOfflineModeViewModel
import com.cocktailcraft.viewmodel.SharedOrderViewModel
import com.cocktailcraft.viewmodel.SharedProfileViewModel
import com.cocktailcraft.viewmodel.SharedReviewViewModel
import com.cocktailcraft.viewmodel.SharedThemeViewModel
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertSame

/**
 * Builds the full production Koin graph ([appModule] = network + data + domain
 * modules) with a test platform module standing in for the bindings each
 * platform's platformModule() supplies natively (Settings, NetworkMonitor),
 * then resolves every registered ViewModel and repository. Any future DI
 * wiring break fails here instead of at app startup.
 *
 * Extends [MainDispatcherTest] because resolving ViewModels constructs them:
 * their init blocks launch on viewModelScope (Dispatchers.Main.immediate), and
 * the swapped-in StandardTestDispatcher queues that work without running it,
 * so resolution stays free of real network or persistence I/O.
 */
class KoinDependencyGraphTest : MainDispatcherTest() {

    /** Test stand-ins for the platform-supplied bindings. */
    private val testPlatformModule = module {
        single<Settings> { MapSettings() }
        single<NetworkMonitor> { FakeNetworkMonitor() }
    }

    private fun startTestKoin(): Koin = startKoin {
        modules(appModule + testPlatformModule)
    }.koin

    @AfterTest
    fun tearDownKoin() {
        stopKoin()
    }

    @Test
    fun everyRepositoryBindingResolves() {
        val koin = startTestKoin()

        assertNotNull(koin.get<CocktailRepository>())
        assertNotNull(koin.get<CocktailSearchRepository>())
        assertNotNull(koin.get<CocktailDetailRepository>())
        assertNotNull(koin.get<CocktailCatalogRepository>())
        assertNotNull(koin.get<CocktailFavoritesRepository>())
        assertNotNull(koin.get<CocktailOfflineRepository>())
        assertNotNull(koin.get<CartRepository>())
        assertNotNull(koin.get<AuthRepository>())
        assertNotNull(koin.get<OrderRepository>())
        assertNotNull(koin.get<ReviewRepository>())
    }

    @Test
    fun everyViewModelDefinitionResolves() {
        val koin = startTestKoin()

        // Screen-scoped ViewModels (viewModel DSL)
        assertNotNull(koin.get<SharedCocktailDetailViewModel>())
        assertNotNull(koin.get<SharedReviewViewModel>())

        // Global-state ViewModels (singles)
        assertNotNull(koin.get<SharedHomeViewModel>())
        assertNotNull(koin.get<SharedCartViewModel>())
        assertNotNull(koin.get<SharedFavoritesViewModel>())
        assertNotNull(koin.get<SharedOrderViewModel>())
        assertNotNull(koin.get<SharedProfileViewModel>())
        assertNotNull(koin.get<SharedOfflineModeViewModel>())
        assertNotNull(koin.get<SharedThemeViewModel>())
    }

    @Test
    fun repositoryBindingsAreSingletonsAndCompositeResolves() {
        val koin = startTestKoin()

        // Post-split: each narrow interface is its own focused singleton, and
        // the composite (kept for the iOS KoinHelper surface) still resolves.
        assertNotNull(koin.get<CocktailRepository>())
        assertSame(koin.get<CocktailSearchRepository>(), koin.get<CocktailSearchRepository>())
        assertSame(koin.get<CocktailDetailRepository>(), koin.get<CocktailDetailRepository>())
        assertSame(koin.get<CocktailCatalogRepository>(), koin.get<CocktailCatalogRepository>())
    }

    @Test
    fun backgroundSyncServiceResolves() {
        val koin = startTestKoin()

        assertNotNull(koin.get<BackgroundSyncService>())
    }
}
