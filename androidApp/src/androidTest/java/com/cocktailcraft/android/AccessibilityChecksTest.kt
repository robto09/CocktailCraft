package com.cocktailcraft.android

import androidx.compose.ui.test.junit4.accessibility.enableAccessibilityChecks
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.tryPerformAccessibilityChecks
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cocktailcraft.android.fakes.FakeAuthRepository
import com.cocktailcraft.android.fakes.FakeCartRepository
import com.cocktailcraft.android.fakes.FakeCocktailRepository
import com.cocktailcraft.android.fakes.FakeNetworkMonitor
import com.cocktailcraft.android.fakes.FakeOrderRepository
import com.cocktailcraft.android.fakes.FakeReviewRepository
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
import com.cocktailcraft.domain.repository.ReviewRepository
import com.cocktailcraft.util.NetworkMonitor
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils.matchesCheck
import com.google.android.apps.common.testing.accessibility.framework.checks.ImageContrastCheck
import com.google.android.apps.common.testing.accessibility.framework.checks.TextContrastCheck
import com.google.android.apps.common.testing.accessibility.framework.integrations.espresso.AccessibilityValidator
import org.hamcrest.CoreMatchers.anyOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

/**
 * Accessibility Test Framework checks on a device/emulator via Compose's
 * enableAccessibilityChecks: once enabled, every performClick (and the
 * explicit tryPerformAccessibilityChecks) validates the whole screen and
 * fails on any ATF error. Walks every tab of the real MainScreen with the
 * same in-memory fakes as [MainScreenSharedVmTest].
 *
 * The Robolectric AccessibilityChecksTest (unit tests) covers the same
 * screens on every `testDebugUnitTest` run; this is the on-device
 * counterpart. Contrast checks are suppressed for the same reason as there:
 * the brand coral and default secondary gray are below 4.5:1 by design.
 */
@RunWith(AndroidJUnit4::class)
class AccessibilityChecksTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        val cocktailRepo = FakeCocktailRepository()
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
                    single { FakeCartRepository() }
                    single<CartRepository> { get<FakeCartRepository>() }
                    // Checkout empties the cart in the same transaction (placeOrderAndClearCart)
                    single<OrderRepository> { FakeOrderRepository(cart = get()) }
                    single<AuthRepository> { FakeAuthRepository() }
                    single<ReviewRepository> { FakeReviewRepository() }
                    single<NetworkMonitor> { FakeNetworkMonitor(initiallyOnline = true) }
                }
            )
        }

        composeTestRule.enableAccessibilityChecks(
            AccessibilityValidator()
                .setRunChecksFromRootView(true)
                .setSuppressingResultMatcher(
                    anyOf(
                        matchesCheck(TextContrastCheck::class.java),
                        matchesCheck(ImageContrastCheck::class.java)
                    )
                )
        )
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun everyTabPassesAccessibilityChecks() {
        composeTestRule.setContent { MainScreen() }
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithText(TestCocktails.mojito.name).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onRoot().tryPerformAccessibilityChecks()

        // Each tab click validates the screen it leaves; the final explicit
        // check covers the last one.
        for (tag in listOf("nav_cart", "nav_favorites", "nav_orders", "nav_profile", "nav_home")) {
            composeTestRule.onNodeWithTag(tag).performClick()
            composeTestRule.waitForIdle()
        }
        composeTestRule.onRoot().tryPerformAccessibilityChecks()
    }
}
