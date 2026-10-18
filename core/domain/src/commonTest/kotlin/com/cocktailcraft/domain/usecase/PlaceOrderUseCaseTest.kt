package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.config.DeliveryPolicy
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.OrderItem
import com.cocktailcraft.domain.model.OrderStatus
import com.cocktailcraft.domain.util.ErrorCode
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.testutil.FakeOrderRepository
import com.cocktailcraft.testutil.testCocktail
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PlaceOrderUseCaseTest {

    private val repository = FakeOrderRepository()
    private val useCase = PlaceOrderUseCase(repository)

    private fun item(id: String, price: Double, quantity: Int = 1) =
        CocktailCartItem(cocktail = testCocktail(id, name = "Cocktail $id", price = price), quantity = quantity)

    @Test
    fun invokeBuildsOrderItemsFromCartAndPersistsIt() = runTest {
        val cart = listOf(
            CocktailCartItem(cocktail = testCocktail("1", name = "Margarita", price = 8.0), quantity = 2),
            CocktailCartItem(cocktail = testCocktail("2", name = "Mojito", price = 12.0), quantity = 1)
        )

        val result = useCase(cart)

        val order = (result as Result.Success).data
        assertEquals(
            listOf(
                OrderItem(name = "Margarita", quantity = 2, price = 8.0),
                OrderItem(name = "Mojito", quantity = 1, price = 12.0)
            ),
            order.items
        )
        assertEquals(OrderStatus.PROCESSING, order.status)
        // The exact order the caller gets back is what was persisted.
        assertEquals(listOf(order), repository.placedOrders)
    }

    @Test
    fun totalIsSubtotalPlusDeliveryFeeBelowTheFreeDeliveryThreshold() = runTest {
        // 2 x 8.00 + 12.00 = 28.00, under the threshold: standard fee applies
        val order = (useCase(listOf(item("1", 8.0, quantity = 2), item("2", 12.0))) as Result.Success).data

        assertEquals(28.0 + DeliveryPolicy.STANDARD_FEE, order.total)
    }

    @Test
    fun totalHasNoDeliveryFeeAtOrAboveTheThreshold() = runTest {
        val order = (useCase(listOf(item("1", 25.0, quantity = 2))) as Result.Success).data

        assertEquals(50.0, order.total)
    }

    @Test
    fun invokeChecksOutSoTheCartIsClearedInTheSameTransaction() = runTest {
        // Checkout goes through placeOrderAndClearCart, never plain
        // placeOrder: callers rely on the cart being emptied atomically.
        val order = (useCase(listOf(item("1", 10.0))) as Result.Success).data

        assertEquals(listOf(order), repository.checkedOutOrders)
    }

    @Test
    fun invokeGeneratesOrderIdAndIsoLikeDate() = runTest {
        val order = (useCase(listOf(item("1", 10.0))) as Result.Success).data

        assertTrue(order.id.startsWith("ORD-"), "order id must carry the ORD- prefix, got ${order.id}")
        assertTrue(
            Regex("""\d{4}-\d{2}-\d{2}""").matches(order.date),
            "order date must be zero-padded YYYY-MM-DD, got ${order.date}"
        )
    }

    @Test
    fun ordersPlacedBackToBackGetDistinctIds() = runTest {
        // A double tap places two orders within the same millisecond; the
        // old ORD-<epoch ms> ids collided and the second insert failed.
        val cart = listOf(item("1", 10.0))

        val first = (useCase(cart) as Result.Success).data
        val second = (useCase(cart) as Result.Success).data

        assertNotEquals(first.id, second.id)
        assertEquals(2, repository.placedOrders.size)
    }

    @Test
    fun invokePropagatesRepositoryErrorMessageAndCodeWithoutPersisting() = runTest {
        repository.errorMessage = "storage full"
        repository.errorCode = ErrorCode.SERVER_ERROR

        val result = useCase(listOf(item("1", 10.0)))

        val error = result as Result.Error
        assertEquals("storage full", error.message)
        assertEquals(ErrorCode.SERVER_ERROR, error.code)
        assertTrue(repository.placedOrders.isEmpty())
    }

    @Test
    fun emptyCartIsRejectedInsteadOfPlacingAnOrderForJustTheDeliveryFee() = runTest {
        val result = useCase(emptyList())

        assertEquals(ErrorCode.INVALID_DATA, (result as Result.Error).code)
        assertTrue(repository.placedOrders.isEmpty())
    }
}
