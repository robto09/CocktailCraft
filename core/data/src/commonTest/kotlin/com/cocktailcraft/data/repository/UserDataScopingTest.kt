package com.cocktailcraft.data.repository

import app.cash.turbine.test
import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.cache.OfflineModePolicy
import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.data.local.UserSession
import com.cocktailcraft.data.remote.ApiRateLimiter
import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Money
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderItem
import com.cocktailcraft.domain.model.OrderStatus
import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.testutil.FakeCocktailApi
import com.cocktailcraft.testutil.FakeNetworkMonitor
import com.cocktailcraft.testutil.createTestDatabase
import com.cocktailcraft.testutil.createTestLocalDatabase
import com.cocktailcraft.testutil.testCocktail
import com.cocktailcraft.testutil.testJson
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * The cart, favorites, recently viewed list, orders and reviews belong to
 * whoever is signed in, or to the guest, and everyone reads every review
 * (see UserDataOwnership.kt): over the real repositories, account store and
 * session, as DataModule wires them.
 */
class UserDataScopingTest {

    /** The data layer as DataModule wires it, over one database. */
    private class App(appScope: CoroutineScope, db: CocktailCraftDatabase = createTestDatabase()) {
        private val accounts = AuthRepositoryImpl(MapSettings(), testJson)
        private val session = UserSession(accounts)
        private val local = createTestLocalDatabase(db, owner = session)
        val cache = CocktailCache(db, testJson, AppConfigImpl(), currentOwner = session)
        private val offline = CocktailOfflineRepositoryImpl(OfflineModePolicy(FakeNetworkMonitor(true)), cache)

        val auth = SessionAwareAuthRepository(accounts, session, local)
        val cart = CartRepositoryImpl(local, cache, testJson)
        val favorites = CocktailFavoritesRepositoryImpl(
            localDatabase = local,
            json = testJson,
            cocktailCache = cache,
            remote = CocktailRemoteDataSource(FakeCocktailApi(), ApiRateLimiter()),
            offlineRepository = offline,
            appScope = appScope
        )
        val orders = OrderRepositoryImpl(local)
        val reviews = ReviewRepositoryImpl(local)

        suspend fun cartIds() = cart.getCartItems().getOrThrow().map { it.cocktail.id to it.quantity }
        suspend fun favoriteIds() = favorites.getFavoriteCocktails().getOrThrow().map { it.id }
        suspend fun orderIds() = orders.getOrders().getOrThrow().map { it.id }
        suspend fun reviewIds() = reviews.getReviews().getOrThrow().map { it.id }
        suspend fun myReviewIds() = reviews.getReviews().getOrThrow().filter { it.isMine }.map { it.id }
        suspend fun recentIds() = cache.getRecentlyViewedCocktails().map { it.id }

        /** Fills the current owner's cart, favorites, recently viewed list, orders and reviews. */
        suspend fun addData(tag: String) {
            cart.addToCart(CocktailCartItem(testCocktail("cart-$tag"), 1)).getOrThrow()
            favorites.addToFavorites(testCocktail("fav-$tag")).getOrThrow()
            cache.addToRecentlyViewed(testCocktail("viewed-$tag"))
            orders.placeOrderAndClearCart(order("order-$tag")).getOrThrow()
            cart.addToCart(CocktailCartItem(testCocktail("cart-$tag"), 1)).getOrThrow()
            reviews.addReview(review("review-$tag")).getOrThrow()
        }
    }

    @Test
    fun aSignedOutGuestSeesNoneOfTheUsersDataButTheirReviews() = runTest {
        val app = App(backgroundScope)
        app.auth.signUp("ana@example.com", "secret123").getOrThrow()
        app.addData("ana")

        app.auth.signOut().getOrThrow()

        assertTrue(app.cartIds().isEmpty())
        assertTrue(app.favoriteIds().isEmpty())
        assertTrue(app.recentIds().isEmpty())
        assertTrue(app.orderIds().isEmpty())
        assertEquals(listOf("review-ana"), app.reviewIds(), "everyone reads every review")
        assertTrue(app.myReviewIds().isEmpty())
    }

    @Test
    fun everyoneReadsEveryReviewButOnlyItsAuthorChangesIt() = runTest {
        val app = App(backgroundScope)
        app.reviews.addReview(review("review-guest")).getOrThrow()
        app.auth.signUp("ana@example.com", "secret123").getOrThrow()
        app.reviews.addReview(review("review-ana")).getOrThrow()

        val asAna = app.reviews.getReviews().getOrThrow().associate { it.id to it.isMine }
        assertEquals(mapOf("review-guest" to false, "review-ana" to true), asAna)
        assertFalse(app.reviews.updateReview("review-guest", 1f, "Hijacked", "2026-10-20").getOrThrow())
        assertFalse(app.reviews.deleteReview("review-guest").getOrThrow())
        assertTrue(app.reviews.updateReview("review-ana", 5f, "Even better", "2026-10-20").getOrThrow())

        app.auth.signOut().getOrThrow()
        val asGuest = app.reviews.getReviews().getOrThrow()
        assertEquals(listOf("review-guest" to true, "review-ana" to false), asGuest.map { it.id to it.isMine })
        assertEquals(listOf("Nice", "Even better"), asGuest.map { it.comment })
        assertTrue(app.reviews.deleteReview("review-guest").getOrThrow())
        assertEquals(listOf("review-ana"), app.reviewIds())
    }

    @Test
    fun observedReviewsFollowWhoseTheyAre() = runTest {
        val app = App(backgroundScope)
        app.auth.signUp("ana@example.com", "secret123").getOrThrow()
        app.reviews.addReview(review("review-ana")).getOrThrow()

        app.reviews.observeReviews().test {
            assertEquals(listOf(true), awaitItem().map { it.isMine })

            app.auth.signOut().getOrThrow()
            assertEquals(listOf(false), awaitItem().map { it.isMine }, "still shown, no longer editable")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun nobodyElseCanChangeAUsersOrdersOrReviews() = runTest {
        val app = App(backgroundScope)
        app.auth.signUp("ana@example.com", "secret123").getOrThrow()
        app.addData("ana")
        app.auth.signOut().getOrThrow()

        assertFalse(app.reviews.updateReview("review-ana", 1f, "Hijacked", "2026-10-20").getOrThrow())
        assertFalse(app.reviews.deleteReview("review-ana").getOrThrow())
        assertFalse(app.orders.cancelOrder("order-ana").getOrThrow())
        app.orders.deleteOrder("order-ana").getOrThrow()

        app.auth.signIn("ana@example.com", "secret123").getOrThrow()
        assertEquals(listOf("order-ana"), app.orderIds())
        assertEquals(OrderStatus.PROCESSING, app.orders.getOrderById("order-ana").getOrThrow()?.status)
        assertEquals("Nice", app.reviews.getReviews().getOrThrow().single().comment)
    }

    @Test
    fun switchingAccountsShowsEachUserOnlyTheirOwnData() = runTest {
        val app = App(backgroundScope)
        app.auth.signUp("ana@example.com", "secret123").getOrThrow()
        app.addData("ana")
        app.auth.signOut().getOrThrow()
        app.auth.signUp("ben@example.com", "secret123").getOrThrow()
        app.addData("ben")

        assertEquals(listOf("cart-ben" to 1), app.cartIds())
        assertEquals(listOf("order-ben"), app.orderIds())

        app.auth.signOut().getOrThrow()
        app.auth.signIn("ana@example.com", "secret123").getOrThrow()

        assertEquals(listOf("cart-ana" to 1), app.cartIds())
        assertEquals(listOf("fav-ana"), app.favoriteIds())
        assertEquals(listOf("viewed-ana"), app.recentIds())
        assertEquals(listOf("order-ana"), app.orderIds())
        assertEquals(listOf("review-ana", "review-ben"), app.reviewIds())
        assertEquals(listOf("review-ana"), app.myReviewIds())
    }

    @Test
    fun signingInMergesTheGuestsCartFavoritesAndViewsIntoTheAccount() = runTest {
        val app = App(backgroundScope)
        app.auth.signUp("ana@example.com", "secret123").getOrThrow()
        app.cart.addToCart(CocktailCartItem(testCocktail("1", price = Money(900)), 1)).getOrThrow()
        app.favorites.addToFavorites(testCocktail("3")).getOrThrow()
        app.auth.signOut().getOrThrow()

        // The guest builds a cart, then signs in to check out
        app.cart.addToCart(CocktailCartItem(testCocktail("1", price = Money(950)), 2)).getOrThrow()
        app.cart.addToCart(CocktailCartItem(testCocktail("2"), 1)).getOrThrow()
        app.favorites.addToFavorites(testCocktail("3")).getOrThrow()
        app.favorites.addToFavorites(testCocktail("4")).getOrThrow()
        app.cache.addToRecentlyViewed(testCocktail("5"))
        app.orders.placeOrderAndClearCart(order("guest-order")).getOrThrow()
        app.cart.addToCart(CocktailCartItem(testCocktail("1", price = Money(950)), 2)).getOrThrow()
        app.cart.addToCart(CocktailCartItem(testCocktail("2"), 1)).getOrThrow()
        app.reviews.addReview(review("guest-review")).getOrThrow()

        app.auth.signIn("ana@example.com", "secret123").getOrThrow()

        val cart = app.cart.getCartItems().getOrThrow()
        assertEquals(listOf("1" to 3, "2" to 1), cart.map { it.cocktail.id to it.quantity })
        assertEquals(Money(900), cart.first().cocktail.price, "the account's line keeps its price")
        assertEquals(listOf("3", "4"), app.favoriteIds())
        assertEquals(listOf("5"), app.recentIds())
        assertTrue(app.orderIds().isEmpty(), "orders stay with whoever placed them")
        assertTrue(app.myReviewIds().isEmpty(), "reviews stay with their author")
        assertEquals(listOf("guest-review"), app.reviewIds(), "but everyone still reads them")

        // The guest keeps the orders and reviews; the rest moved
        app.auth.signOut().getOrThrow()
        assertTrue(app.cartIds().isEmpty())
        assertTrue(app.favoriteIds().isEmpty())
        assertTrue(app.recentIds().isEmpty())
        assertEquals(listOf("guest-order"), app.orderIds())
        assertEquals(listOf("guest-review"), app.myReviewIds())
    }

    @Test
    fun switchingFromOneAccountToAnotherMergesNothing() = runTest {
        val app = App(backgroundScope)
        app.auth.signUp("ben@example.com", "secret123").getOrThrow()
        app.auth.signOut().getOrThrow()
        app.auth.signUp("ana@example.com", "secret123").getOrThrow()
        app.addData("ana")

        // Ben signs in over Ana's session, without signing her out
        app.auth.signIn("ben@example.com", "secret123").getOrThrow()

        assertTrue(app.cartIds().isEmpty())
        assertTrue(app.favoriteIds().isEmpty())
    }

    @Test
    fun changingThePasswordKeepsTheOwnerAndTheThrottle() = runTest {
        val app = App(backgroundScope)
        app.auth.signUp("ana@example.com", "secret123").getOrThrow()
        app.addData("ana")

        repeat(5) { assertFalse(app.auth.changePassword("wrong-pass", "newpass456").getOrThrow()) }

        assertIs<Result.Error>(app.auth.changePassword("secret123", "newpass456"), "locked out through the wrapper")
        assertEquals(listOf("order-ana"), app.orderIds(), "still Ana's data")
    }

    @Test
    fun aFailedSignInChangesNothing() = runTest {
        val app = App(backgroundScope)
        app.addData("guest")

        assertFalse(app.auth.signIn("nobody@example.com", "secret123").getOrThrow())

        assertEquals(listOf("cart-guest" to 1), app.cartIds())
        assertEquals(listOf("order-guest"), app.orderIds())
    }

    @Test
    fun observersSwitchToTheNewOwnersData() = runTest {
        val app = App(backgroundScope)
        app.auth.signUp("ana@example.com", "secret123").getOrThrow()
        app.cart.addToCart(CocktailCartItem(testCocktail("ana"), 1)).getOrThrow()
        app.auth.signOut().getOrThrow()

        app.cart.observeCartItems().test {
            assertEquals(emptyList(), awaitItem().map { it.cocktail.id }, "the guest's empty cart")

            app.auth.signIn("ana@example.com", "secret123").getOrThrow()
            assertEquals(listOf("ana"), awaitItem().map { it.cocktail.id })

            app.auth.signOut().getOrThrow()
            assertEquals(emptyList(), awaitItem().map { it.cocktail.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun theGuestsPinnedCocktailsStayCachedForThem() = runTest {
        // Another owner's favorites keep pinning their cocktails while signed out
        val app = App(backgroundScope)
        app.auth.signUp("ana@example.com", "secret123").getOrThrow()
        app.favorites.addToFavorites(testCocktail("fav")).getOrThrow()
        app.auth.signOut().getOrThrow()

        assertNotNull(app.cache.getCachedCocktail("fav"))
    }
}

private fun order(id: String) = Order(
    id = id,
    date = "2026-10-19",
    items = listOf(OrderItem(name = "Mojito", quantity = 1, price = Money(900), cocktailId = "1")),
    total = Money(1499),
    status = OrderStatus.PROCESSING
)

private fun review(id: String) =
    Review(id = id, cocktailId = "1", userName = "Ana", rating = 4f, comment = "Nice", date = "2026-10-19")
