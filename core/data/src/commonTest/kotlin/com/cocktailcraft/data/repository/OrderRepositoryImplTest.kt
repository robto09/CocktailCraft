package com.cocktailcraft.data.repository

import app.cash.sqldelight.db.QueryResult
import app.cash.turbine.test
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Money
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderItem
import com.cocktailcraft.domain.model.OrderStatus
import com.cocktailcraft.domain.model.sumOfMoney
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.testutil.createInMemorySqlDriver
import com.cocktailcraft.testutil.createTestDatabase
import com.cocktailcraft.testutil.createTestLocalDatabase
import com.cocktailcraft.testutil.testCocktail
import com.cocktailcraft.testutil.testJson
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OrderRepositoryImplTest {

    private val driver = createInMemorySqlDriver()
    private val localDatabase = createTestLocalDatabase(createTestDatabase(driver))

    // A new repository over the same database simulates an app relaunch.
    private fun repository() = OrderRepositoryImpl(localDatabase)

    private fun cart() = CartRepositoryImpl(localDatabase, testJson)

    private fun order(
        id: String,
        status: OrderStatus = OrderStatus.PROCESSING,
        items: List<OrderItem> = listOf(OrderItem(name = "Margarita", quantity = 1, price = Money(950)))
    ) = Order(
        id = id,
        date = "2026-07-07",
        items = items,
        total = items.sumOfMoney { it.lineTotal },
        status = status
    )

    @Test
    fun checkoutGeneratesIdForBlankIdAndKeepsProvidedId() = runTest {
        val repo = repository()
        repo.placeOrderAndClearCart(order(id = "")).getOrThrow()
        repo.placeOrderAndClearCart(order(id = "ORD-42")).getOrThrow()

        val orders = repo.getOrders().getOrThrow()
        assertEquals(2, orders.size)
        assertTrue(orders[0].id.isNotBlank(), "checkout must generate an id for a blank one")
        assertEquals("ORD-42", orders[1].id)
    }

    @Test
    fun placedOrderSurvivesRepositoryRecreation() = runTest {
        repository().placeOrderAndClearCart(order(id = "ORD-1")).getOrThrow()

        val reloaded = repository().getOrders().getOrThrow()
        assertEquals(listOf("ORD-1"), reloaded.map { it.id })
    }

    @Test
    fun ordersRoundTripWithTheirItemsInOrder() = runTest {
        val multi = order(
            id = "ORD-1",
            items = listOf(
                OrderItem(name = "Margarita", quantity = 2, price = Money(950)),
                OrderItem(name = "Mojito", quantity = 1, price = Money(1100)),
                OrderItem(name = "Negroni", quantity = 3, price = Money(1225))
            )
        )
        val empty = order(id = "ORD-2", items = emptyList())
        val repo = repository()
        repo.placeOrderAndClearCart(multi).getOrThrow()
        repo.placeOrderAndClearCart(empty).getOrThrow()

        assertEquals(listOf(multi, empty), repository().getOrders().getOrThrow())
        assertEquals(multi, repo.getOrderById("ORD-1").getOrThrow())
        assertEquals(empty, repo.getOrderById("ORD-2").getOrThrow())
        assertNull(repo.getOrderById("missing").getOrThrow())
    }

    @Test
    fun checkoutWithDuplicateIdFailsAndKeepsTheOriginal() = runTest {
        val repo = repository()
        repo.placeOrderAndClearCart(order(id = "ORD-1")).getOrThrow()

        val duplicate = order(id = "ORD-1", items = listOf(OrderItem("Mojito", 4, Money(1100))))
        assertIs<Result.Error>(repo.placeOrderAndClearCart(duplicate))

        assertEquals(listOf(order(id = "ORD-1")), repo.getOrders().getOrThrow())
    }

    @Test
    fun placeOrderAndClearCartStoresTheOrderAndEmptiesTheCart() = runTest {
        val cart = cart()
        cart.addToCart(CocktailCartItem(testCocktail("1"), quantity = 2)).getOrThrow()
        cart.addToCart(CocktailCartItem(testCocktail("2"), quantity = 1)).getOrThrow()

        repository().placeOrderAndClearCart(order(id = "ORD-1")).getOrThrow()

        assertEquals(listOf("ORD-1"), repository().getOrders().getOrThrow().map { it.id })
        assertTrue(cart.getCartItems().getOrThrow().isEmpty())
    }

    @Test
    fun failedCheckoutLeavesTheCartAndOrdersUntouched() = runTest {
        // The order insert and the cart clear share one transaction, so a
        // failed checkout (here: a duplicate order id) changes nothing.
        val repo = repository()
        repo.placeOrderAndClearCart(order(id = "ORD-1")).getOrThrow()
        val cart = cart()
        cart.addToCart(CocktailCartItem(testCocktail("1"), quantity = 2)).getOrThrow()

        assertIs<Result.Error>(repo.placeOrderAndClearCart(order(id = "ORD-1")))

        assertEquals(listOf(2), cart.getCartItems().getOrThrow().map { it.quantity })
        assertEquals(listOf("ORD-1"), repo.getOrders().getOrThrow().map { it.id })
    }

    @Test
    fun observeOrdersEmitsAfterEveryChange() = runTest {
        val repo = repository()

        repo.observeOrders().test {
            assertEquals(emptyList(), awaitItem())

            repo.placeOrderAndClearCart(order(id = "ORD-1")).getOrThrow()
            assertEquals(listOf("ORD-1"), awaitItem().map { it.id })

            repo.cancelOrder("ORD-1").getOrThrow()
            assertEquals(listOf(OrderStatus.CANCELLED), awaitItem().map { it.status })

            repo.deleteOrder("ORD-1").getOrThrow()
            assertEquals(emptyList(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun cancelOrderOnlyCancelsOpenOrders() = runTest {
        val repo = repository()
        repo.placeOrderAndClearCart(order(id = "processing", status = OrderStatus.PROCESSING)).getOrThrow()
        repo.placeOrderAndClearCart(order(id = "pending", status = OrderStatus.PENDING)).getOrThrow()
        repo.placeOrderAndClearCart(order(id = "shipped", status = OrderStatus.SHIPPED)).getOrThrow()

        assertTrue(repo.cancelOrder("processing").getOrThrow())
        assertTrue(repo.cancelOrder("pending").getOrThrow())
        assertFalse(repo.cancelOrder("shipped").getOrThrow())
        assertFalse(repo.cancelOrder("missing").getOrThrow())

        assertEquals(
            listOf(OrderStatus.CANCELLED, OrderStatus.CANCELLED, OrderStatus.SHIPPED),
            repo.getOrders().getOrThrow().map { it.status }
        )
    }

    @Test
    fun statusColumnKeepsThePreEnumStrings() = runTest {
        // Rows written while status was a raw String hold "Processing" etc.;
        // they must read back as the matching enum, and writes must keep
        // storing those same strings.
        val queries = localDatabase.get().customerOrderQueries
        queries.insertOrder(id = "old", date = "2026-01-01", totalCents = 950, status = "Processing")
        queries.insertOrder(id = "odd-case", date = "2026-01-01", totalCents = 950, status = "pending")

        val repo = repository()
        assertEquals(OrderStatus.PROCESSING, repo.getOrderById("old").getOrThrow()?.status)
        assertEquals(OrderStatus.PENDING, repo.getOrderById("odd-case").getOrThrow()?.status)

        assertTrue(repo.cancelOrder("old").getOrThrow())
        assertEquals("Cancelled", queries.selectStatus("old").executeAsOne())
        assertTrue(repo.cancelOrder("odd-case").getOrThrow())
        assertEquals("Cancelled", queries.selectStatus("odd-case").executeAsOne())
    }

    @Test
    fun deleteOrderRemovesTheOrderAndItsItems() = runTest {
        val repo = repository()
        repo.placeOrderAndClearCart(order(id = "ORD-1")).getOrThrow()
        repo.placeOrderAndClearCart(order(id = "ORD-2")).getOrThrow()

        repo.deleteOrder("ORD-1").getOrThrow()

        assertEquals(listOf("ORD-2"), repo.getOrders().getOrThrow().map { it.id })
        assertEquals(0L, itemCount("ORD-1"), "an order's items must go with it")
        assertEquals(1L, itemCount("ORD-2"))
    }

    /** Counts item rows directly: the order queries can't see orphaned items. */
    private fun itemCount(orderId: String): Long =
        driver.executeQuery(
            identifier = null,
            sql = "SELECT COUNT(*) FROM customer_order_item WHERE order_id = ?",
            mapper = { cursor -> QueryResult.Value(if (cursor.next().value) cursor.getLong(0) else null) },
            parameters = 1
        ) { bindString(0, orderId) }.value ?: 0L
}
