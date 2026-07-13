package com.cocktailcraft

import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.di.appModule
import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.di.secureSettingsQualifier
import com.cocktailcraft.testutil.FakeNetworkMonitor
import com.cocktailcraft.testutil.MainDispatcherTest
import com.cocktailcraft.util.NetworkMonitor
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Mirror of KoinDependencyGraphTest for the iOS service locator (AR-6):
 * resolves every KoinHelper getter so a shared binding that was renamed,
 * removed, or never surfaced here fails THIS test instead of crashing Swift
 * at first use. When a getter is added to KoinHelper, add its assertion here.
 *
 * Extends MainDispatcherTest for the same reason as the graph test: resolving
 * ViewModels runs their init blocks on viewModelScope, and the swapped-in
 * StandardTestDispatcher queues that work without running it.
 */
class KoinHelperTest : MainDispatcherTest() {

    /** Test stand-ins for the bindings iOS's platformModule() supplies natively. */
    private val testPlatformModule = module {
        single<AppConfig> { AppConfigImpl() }
        single<Settings> { MapSettings() }
        single<Settings>(secureSettingsQualifier) { MapSettings() }
        single<NetworkMonitor> { FakeNetworkMonitor() }
    }

    @AfterTest
    fun tearDownKoin() {
        stopKoin()
    }

    @Test
    fun everyKoinHelperGetterResolves() {
        startKoin { modules(appModule + testPlatformModule) }
        val helper = KoinHelper()

        // Repositories
        assertNotNull(helper.getAuthRepository())
        assertNotNull(helper.getCartRepository())
        assertNotNull(helper.getOrderRepository())

        // Services / platform bridges
        assertNotNull(helper.getNetworkMonitor())
        assertNotNull(helper.getBackgroundSyncService())

        // Shared ViewModels — screen-scoped
        assertNotNull(helper.getSharedCocktailDetailViewModel())
        assertNotNull(helper.getSharedReviewViewModel())

        // Shared ViewModels — global-state singles
        assertNotNull(helper.getSharedFavoritesViewModel())
        assertNotNull(helper.getSharedHomeViewModel())
        assertNotNull(helper.getSharedCartViewModel())
        assertNotNull(helper.getSharedOfflineModeViewModel())
        assertNotNull(helper.getSharedOrderViewModel())
        assertNotNull(helper.getSharedProfileViewModel())
        assertNotNull(helper.getSharedThemeViewModel())
    }
}
