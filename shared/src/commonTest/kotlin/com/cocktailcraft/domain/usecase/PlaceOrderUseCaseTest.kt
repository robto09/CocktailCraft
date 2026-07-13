package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderItem
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.testutil.testCocktail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeOrderRepository : OrderRepository {
    val placedOrders = mutableListOf<Order>()

    /** When set, placeOrder returns Result.Error with this message. */
    var errorMessage: String? = null

    override fun observeOrders(): Flow<List<Order>> = MutableStateFlow(placedOrders.toList())
    override suspend fun getOrders(): Result<List<Order>> = Result.Success(placedOrders.toList())

    override suspend fun placeOrder(order: Order): Result<Unit> {
        errorMessage?.let { return Result.Error(it) }
        placedOrders += order
        return Result.Success(Unit)
    }

    override suspend fun getOrderById(id: String): Result<Order?> =
        Result.Success(placedOrders.find { it.id == id })

    override suspend fun updateOrderStatus(id: String, status: String): Result<Unit> = Result.Success(Unit)
    override suspend fun deleteOrder(id: String): Result<Unit> = Result.Success(Unit)
    override suspend fun cancelOrder(orderId: String): Result<Boolean> = Result.Success(false)
}

class PlaceOrderUseCaseTest {

    private val repository = FakeOrderRepository()
    private val useCase = PlaceOrderUseCase(repository)

    @Test
    fun invokeBuildsOrderItemsFromCartAndPersistsIt() = runTest {
        val cart = listOf(
            CocktailCartItem(cocktail = testCocktail("1", name = "Margarita", price = 8.0), quantity = 2),
            CocktailCartItem(cocktail = testCocktail("2", name = "Mojito", price = 12.0), quantity = 1)
        )

        val result = useCase(cart, totalPrice = 28.0)

        val order = (result as Result.Success).data
        assertEquals(
            listOf(
                OrderItem(name = "Margarita", quantity = 2, price = 8.0),
                OrderItem(name = "Mojito", quantity = 1, price = 12.0)
            ),
            order.items
        )
        assertEquals(28.0, order.total)
        assertEquals("Processing", order.status)
        // The exact order the caller gets back is what was persisted.
        assertEquals(listOf(order), repository.placedOrders)
    }

    @Test
    fun invokeGeneratesOrderIdAndIsoLikeDate() = runTest {
        val cart = listOf(CocktailCartItem(cocktail = testCocktail("1"), quantity = 1))

        val order = (useCase(cart, totalPrice = 10.0) as Result.Success).data

        assertTrue(order.id.startsWith("ORD-"), "order id must carry the ORD- prefix, got ${order.id}")
        assertTrue(
            Regex("""\d{4}-\d{2}-\d{2}""").matches(order.date),
            "order date must be zero-padded YYYY-MM-DD, got ${order.date}"
        )
    }

    @Test
    fun invokePropagatesRepositoryErrorWithoutPersisting() = runTest {
        repository.errorMessage = "storage full"
        val cart = listOf(CocktailCartItem(cocktail = testCocktail("1"), quantity = 1))

        val result = useCase(cart, totalPrice = 10.0)

        assertEquals("storage full", (result as Result.Error).message)
        assertTrue(repository.placedOrders.isEmpty())
    }

    @Test
    fun emptyCartStillPlacesAnOrderWithNoItems() = runTest {
        // The use case does not guard against empty carts — that policy lives
        // with the caller. Lock in the pass-through behavior.
        val result = useCase(emptyList(), totalPrice = 0.0)

        val order = (result as Result.Success).data
        assertTrue(order.items.isEmpty())
        assertEquals(0.0, order.total)
        assertEquals(1, repository.placedOrders.size)
    }
}
