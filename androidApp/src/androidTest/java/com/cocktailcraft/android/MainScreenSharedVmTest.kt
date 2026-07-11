package com.cocktailcraft.android

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cocktailcraft.android.fakes.FakeAuthRepository
import com.cocktailcraft.android.fakes.FakeCartRepository
import com.cocktailcraft.android.fakes.FakeCocktailRepository
import com.cocktailcraft.android.fakes.FakeNetworkMonitor
import com.cocktailcraft.android.fakes.FakeOrderRepository
import com.cocktailcraft.android.fakes.TestCocktails
import com.cocktailcraft.android.ui.main.MainScreen
import com.cocktailcraft.di.domainModule
import com.cocktailcraft.domain.repository.AuthRepository
import com.cocktailcraft.domain.repository.CartRepository
import com.cocktailcraft.domain.repository.CocktailCatalogRepository
import com.cocktailcraft.domain.repository.CocktailDetailRepository
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import com.cocktailcraft.domain.repository.CocktailSearchRepository
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.util.NetworkMonitor
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

/**
 * Compose tests exercising the real screens with the real shared KMP
 * ViewModels and use cases — only the repositories and connectivity are
 * faked (in-memory), so these cover the full UI -> shared-VM -> use-case
 * round trip without network or persistence.
 */
@RunWith(AndroidJUnit4::class)
class MainScreenSharedVmTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var cocktailRepo: FakeCocktailRepository
    private lateinit var networkMonitor: FakeNetworkMonitor

    @Before
    fun setUp() {
        cocktailRepo = FakeCocktailRepository()
        networkMonitor = FakeNetworkMonitor(initiallyOnline = true)
        stopKoin()
        startKoin {
            modules(
                domainModule,
                module {
                    single<CocktailSearchRepository> { cocktailRepo }
                    single<CocktailCatalogRepository> { cocktailRepo }
                    single<CocktailDetailRepository> { cocktailRepo }
                    single<CocktailFavoritesRepository> { cocktailRepo }
                    single<CocktailOfflineRepository> { cocktailRepo }
                    single<CartRepository> { FakeCartRepository() }
                    single<OrderRepository> { FakeOrderRepository() }
                    single<AuthRepository> { FakeAuthRepository() }
                    single<NetworkMonitor> { networkMonitor }
                }
            )
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    private fun launchApp() {
        composeTestRule.setContent { MainScreen() }
        waitForText(TestCocktails.mojito.name)
    }

    private fun waitForText(text: String, timeoutMillis: Long = 10_000) {
        composeTestRule.waitUntil(timeoutMillis) {
            composeTestRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun homeShowsCocktailsFromRepository() {
        launchApp()

        composeTestRule.onNodeWithText("Test Mojito").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Negroni").assertIsDisplayed()
    }

    @Test
    fun addToCartFromHome_showsItemInCartTab() {
        launchApp()

        // The first "Add to Cart" button belongs to the first list item;
        // HomeScreen's onAddToCart then auto-navigates to the Cart screen.
        composeTestRule.onAllNodesWithContentDescription("Add to Cart").onFirst().performClick()

        waitForText("Place Order")
        composeTestRule.onNodeWithText("Test Mojito").assertIsDisplayed()
        composeTestRule.onNodeWithText("Place Order").assertIsDisplayed()
    }

    @Test
    fun tappingCocktail_navigatesToDetail() {
        launchApp()

        composeTestRule.onNodeWithText("Test Mojito").performClick()

        // Detail screen shows stock information the list rows don't
        waitForText("Add to Cart")
        composeTestRule.onNodeWithText("Add to Cart").assertIsDisplayed()
    }

    @Test
    fun favoriteFromHome_showsInFavoritesTab() {
        launchApp()

        composeTestRule.onAllNodesWithContentDescription("Add to favorites").onFirst().performClick()
        composeTestRule.onNodeWithText("Favorites").performClick()

        waitForText("Test Mojito")
        composeTestRule.onNodeWithText("Test Mojito").assertIsDisplayed()
    }

    @Test
    fun ordersTab_showsEmptyState() {
        launchApp()

        composeTestRule.onNodeWithText("Orders").performClick()

        waitForText("No orders yet")
        composeTestRule.onNodeWithText("No orders yet").assertIsDisplayed()
    }

    @Test
    fun offlineIndicator_appearsWhenNetworkDrops() {
        launchApp()

        composeTestRule.runOnUiThread { networkMonitor.setOnline(false) }

        // When connectivity drops, SharedOfflineModeViewModel auto-enables
        // offline mode, so the indicator shows the offline-mode wording.
        waitForText("Offline Mode Enabled")
    }
}
