package com.cocktailcraft.data.repository

import app.cash.sqldelight.db.QueryResult
import app.cash.turbine.test
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderItem
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
        status: String = "Processing",
        items: List<OrderItem> = listOf(OrderItem(name = "Margarita", quantity = 1, price = 9.5))
    ) = Order(
        id = id,
        date = "2026-07-07",
        items = items,
        total = items.sumOf { it.price * it.quantity },
        status = status
    )

    @Test
    fun placeOrderGeneratesIdForBlankIdAndKeepsProvidedId() = runTest {
        val repo = repository()
        repo.placeOrder(order(id = "")).getOrThrow()
        repo.placeOrder(order(id = "ORD-42")).getOrThrow()

        val orders = repo.getOrders().getOrThrow()
        assertEquals(2, orders.size)
        assertTrue(orders[0].id.isNotBlank(), "placeOrder must generate an id for a blank one")
        assertEquals("ORD-42", orders[1].id)
    }

    @Test
    fun placedOrderSurvivesRepositoryRecreation() = runTest {
        repository().placeOrder(order(id = "ORD-1")).getOrThrow()

        val reloaded = repository().getOrders().getOrThrow()
        assertEquals(listOf("ORD-1"), reloaded.map { it.id })
    }

    @Test
    fun ordersRoundTripWithTheirItemsInOrder() = runTest {
        val multi = order(
            id = "ORD-1",
            items = listOf(
                OrderItem(name = "Margarita", quantity = 2, price = 9.5),
                OrderItem(name = "Mojito", quantity = 1, price = 11.0),
                OrderItem(name = "Negroni", quantity = 3, price = 12.25)
            )
        )
        val empty = order(id = "ORD-2", items = emptyList())
        val repo = repository()
        repo.placeOrder(multi).getOrThrow()
        repo.placeOrder(empty).getOrThrow()

        assertEquals(listOf(multi, empty), repository().getOrders().getOrThrow())
        assertEquals(multi, repo.getOrderById("ORD-1").getOrThrow())
        assertEquals(empty, repo.getOrderById("ORD-2").getOrThrow())
        assertNull(repo.getOrderById("missing").getOrThrow())
    }

    @Test
    fun placeOrderWithDuplicateIdFailsAndKeepsTheOriginal() = runTest {
        val repo = repository()
        repo.placeOrder(order(id = "ORD-1")).getOrThrow()

        val duplicate = order(id = "ORD-1", items = listOf(OrderItem("Mojito", 4, 11.0)))
        assertIs<Result.Error>(repo.placeOrder(duplicate))

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
        repo.placeOrder(order(id = "ORD-1")).getOrThrow()
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

            repo.placeOrder(order(id = "ORD-1")).getOrThrow()
            assertEquals(listOf("ORD-1"), awaitItem().map { it.id })

            repo.cancelOrder("ORD-1").getOrThrow()
            assertEquals(listOf("Cancelled"), awaitItem().map { it.status })

            repo.deleteOrder("ORD-1").getOrThrow()
            assertEquals(emptyList(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun cancelOrderOnlyCancelsOpenOrders() = runTest {
        val repo = repository()
        repo.placeOrder(order(id = "processing", status = "Processing")).getOrThrow()
        repo.placeOrder(order(id = "pending", status = "Pending")).getOrThrow()
        repo.placeOrder(order(id = "shipped", status = "Shipped")).getOrThrow()

        assertTrue(repo.cancelOrder("processing").getOrThrow())
        assertTrue(repo.cancelOrder("pending").getOrThrow())
        assertFalse(repo.cancelOrder("shipped").getOrThrow())
        assertFalse(repo.cancelOrder("missing").getOrThrow())

        assertEquals(
            listOf("Cancelled", "Cancelled", "Shipped"),
            repo.getOrders().getOrThrow().map { it.status }
        )
    }

    @Test
    fun updateOrderStatusPersists() = runTest {
        repository().placeOrder(order(id = "ORD-1")).getOrThrow()

        repository().updateOrderStatus("ORD-1", "Delivered").getOrThrow()

        assertEquals("Delivered", repository().getOrderById("ORD-1").getOrThrow()?.status)
    }

    @Test
    fun deleteOrderRemovesTheOrderAndItsItems() = runTest {
        val repo = repository()
        repo.placeOrder(order(id = "ORD-1")).getOrThrow()
        repo.placeOrder(order(id = "ORD-2")).getOrThrow()

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
