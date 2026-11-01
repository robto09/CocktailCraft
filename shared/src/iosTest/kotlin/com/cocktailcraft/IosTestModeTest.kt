package com.cocktailcraft

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.di.appModule
import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.domain.repository.AuthRepository
import com.cocktailcraft.domain.repository.CartRepository
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.repository.ReviewRepository
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.testutil.FakeNetworkMonitor
import com.cocktailcraft.testutil.MainDispatcherTest
import com.cocktailcraft.testutil.testCocktail
import com.cocktailcraft.util.NetworkMonitor
import kotlinx.coroutines.isActive
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The iOS test mode's bindings (iosTestModeModule) and the KoinHelper hooks
 * the iOS app's widget bridge, wrappers and Debug menu call.
 */
class IosTestModeTest : MainDispatcherTest() {

    // Bindings iosTestModeModule leaves to the platform module
    private val platformStandIns = module {
        single<AppConfig> { AppConfigImpl() }
        single<NetworkMonitor> { FakeNetworkMonitor() }
    }

    private val helper = KoinHelper()

    @BeforeTest
    fun startTestModeKoin() {
        startKoin { modules(appModule + platformStandIns + iosTestModeModule()) }
    }

    @AfterTest
    fun tearDown() {
        helper.debugSetSimulatedNetworkFailure(false)
        stopKoin()
    }

    @Test
    fun widgetPoolComesFromTheFixturesNotTheLiveApi() = runTest {
        val pool = helper.getRandomCocktailWidgetPool(limit = 5)

        assertEquals(listOf("11007"), pool.map { it.id })
    }

    @Test
    fun simulatedNetworkFailureFailsRequestsUntilTurnedOff() = runTest {
        helper.debugSetSimulatedNetworkFailure(true)
        assertTrue(helper.debugIsSimulatingNetworkFailure())
        assertTrue(helper.getRandomCocktailWidgetPool(limit = 5).isEmpty())

        helper.debugSetSimulatedNetworkFailure(false)
        assertEquals(1, helper.getRandomCocktailWidgetPool(limit = 5).size)
    }

    @Test
    fun debugResetUserDataEmptiesCartAndFavorites() = runTest {
        val cocktail = testCocktail("1")
        val cart = KoinPlatform.getKoin().get<CartRepository>()
        cart.addToCart(CocktailCartItem(cocktail, quantity = 2)).getOrThrow()
        val favorites = KoinPlatform.getKoin().get<CocktailFavoritesRepository>()
        favorites.addToFavorites(cocktail).getOrThrow()

        helper.debugResetUserData()

        assertTrue(cart.getCartItems().getOrThrow().isEmpty())
        assertTrue(favorites.getFavoriteCocktails().getOrThrow().isEmpty())
    }

    @Test
    fun debugResetUserDataEmptiesEveryOwnersData() = runTest {
        val koin = KoinPlatform.getKoin()
        val auth = koin.get<AuthRepository>()
        val cart = koin.get<CartRepository>()
        val reviews = koin.get<ReviewRepository>()
        auth.signUp("ana@example.com", "secret123").getOrThrow()
        cart.addToCart(CocktailCartItem(testCocktail("1"), quantity = 1)).getOrThrow()
        reviews.addReview(Review(cocktailId = "1", userName = "Ana", rating = 4f, comment = "Nice")).getOrThrow()
        auth.signOut().getOrThrow()
        reviews.addReview(Review(cocktailId = "1", userName = "Guest", rating = 3f, comment = "Fine")).getOrThrow()

        helper.debugResetUserData()

        assertTrue(reviews.getReviews().getOrThrow().isEmpty())
        auth.signIn("ana@example.com", "secret123").getOrThrow()
        assertTrue(cart.getCartItems().getOrThrow().isEmpty())
    }

    @Test
    fun clearScreenViewModelCancelsItsScope() {
        val viewModel = helper.getSharedReviewViewModel()
        assertTrue(viewModel.viewModelScope.isActive)

        helper.clearScreenViewModel(viewModel)

        assertFalse(viewModel.viewModelScope.isActive)
    }
}
