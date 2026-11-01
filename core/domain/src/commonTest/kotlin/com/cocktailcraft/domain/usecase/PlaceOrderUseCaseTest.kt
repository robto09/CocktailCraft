package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.config.DeliveryPolicy
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Money
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

    private fun item(id: String, cents: Long, quantity: Int = 1) =
        CocktailCartItem(cocktail = testCocktail(id, name = "Cocktail $id", price = Money(cents)), quantity = quantity)

    @Test
    fun invokeBuildsOrderItemsFromCartAndPersistsIt() = runTest {
        val cart = listOf(
            CocktailCartItem(cocktail = testCocktail("1", name = "Margarita", price = Money(800)), quantity = 2),
            CocktailCartItem(cocktail = testCocktail("2", name = "Mojito", price = Money(1200)), quantity = 1)
        )

        val result = useCase(cart)

        val order = (result as Result.Success).data
        assertEquals(
            listOf(
                OrderItem(name = "Margarita", quantity = 2, price = Money(800), cocktailId = "1"),
                OrderItem(name = "Mojito", quantity = 1, price = Money(1200), cocktailId = "2")
            ),
            order.items
        )
        assertEquals(OrderStatus.PROCESSING, order.status)
        // The exact order the caller gets back is what was persisted.
        assertEquals(listOf(order), repository.placedOrders)
    }

    @Test
    fun orderItemsRecordTheirCocktailSoTheOrderCanBeReordered() = runTest {
        val order = (useCase(listOf(item("11007", 1250), item("17222", 999))) as Result.Success).data

        assertEquals(listOf("11007", "17222"), order.items.map { it.cocktailId })
        assertTrue(order.canReorder)
    }

    @Test
    fun totalIsSubtotalPlusDeliveryFeeBelowTheFreeDeliveryThreshold() = runTest {
        // 2 x 8.00 + 12.00 = 28.00, under the threshold: standard fee applies
        val order = (useCase(listOf(item("1", 800, quantity = 2), item("2", 1200))) as Result.Success).data

        assertEquals(Money(2800) + DeliveryPolicy.STANDARD_FEE, order.total)
        assertEquals(Money(3399), order.total)
        assertEquals(Money(2800), order.subtotal)
        assertEquals(DeliveryPolicy.STANDARD_FEE, order.deliveryFee)
    }

    @Test
    fun totalHasNoDeliveryFeeAtOrAboveTheThreshold() = runTest {
        val order = (useCase(listOf(item("1", 2500, quantity = 2))) as Result.Success).data

        assertEquals(Money(5000), order.total)
        assertEquals(Money.ZERO, order.deliveryFee)
    }

    @Test
    fun centsJustUnderTheThresholdStillPayTheFee() = runTest {
        // 3 x 16.66 = 49.98: summing Doubles here used to risk landing a
        // hair either side of 50.00
        val order = (useCase(listOf(item("1", 1666, quantity = 3))) as Result.Success).data

        assertEquals(Money(4998 + 599), order.total)
    }

    @Test
    fun totalsOfManyOddCentPricesAreExact() = runTest {
        // 0.10 + 0.20 is 0.30000000000000004 as Doubles; in cents it is 30
        val cart = listOf(item("1", 10), item("2", 20), item("3", 1999, quantity = 7))

        val order = (useCase(cart) as Result.Success).data

        assertEquals(Money(10 + 20 + 1999 * 7), order.subtotal)
        assertEquals(Money(10 + 20 + 1999 * 7), order.total)
    }

    @Test
    fun invokeChecksOutSoTheCartIsClearedInTheSameTransaction() = runTest {
        // Checkout goes through placeOrderAndClearCart, never plain
        // placeOrder: callers rely on the cart being emptied atomically.
        val order = (useCase(listOf(item("1", 1000))) as Result.Success).data

        assertEquals(listOf(order), repository.checkedOutOrders)
    }

    @Test
    fun invokeGeneratesOrderIdAndIsoLikeDate() = runTest {
        val order = (useCase(listOf(item("1", 1000))) as Result.Success).data

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
        val cart = listOf(item("1", 1000))

        val first = (useCase(cart) as Result.Success).data
        val second = (useCase(cart) as Result.Success).data

        assertNotEquals(first.id, second.id)
        assertEquals(2, repository.placedOrders.size)
    }

    @Test
    fun invokePropagatesRepositoryErrorMessageAndCodeWithoutPersisting() = runTest {
        repository.errorMessage = "storage full"
        repository.errorCode = ErrorCode.SERVER_ERROR

        val result = useCase(listOf(item("1", 1000)))

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
