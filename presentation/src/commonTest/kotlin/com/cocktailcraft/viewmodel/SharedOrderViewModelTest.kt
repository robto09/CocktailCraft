package com.cocktailcraft.viewmodel

import app.cash.turbine.test
import com.cocktailcraft.domain.config.DeliveryPolicy
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Money
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderItem
import com.cocktailcraft.domain.model.OrderStatus
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.usecase.ManageCartUseCase
import com.cocktailcraft.domain.usecase.PlaceOrderUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.error_empty_cart_title
import com.cocktailcraft.resources.error_load_orders_failed
import com.cocktailcraft.resources.error_order_failed_title
import com.cocktailcraft.resources.error_place_order_failed
import com.cocktailcraft.testutil.FakeCartRepository
import com.cocktailcraft.testutil.FakeDetailRepository
import com.cocktailcraft.testutil.MainDispatcherTest
import com.cocktailcraft.testutil.testCocktail
import com.cocktailcraft.util.ErrorHandler
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun testOrder(
    id: String,
    date: String = "2026-01-15",
    total: Money = Money(2000),
    status: OrderStatus = OrderStatus.PROCESSING
) = Order(
    id = id,
    date = date,
    items = listOf(OrderItem(name = "Cocktail $id", quantity = 2, price = Money(total.cents / 2))),
    total = total,
    status = status
)

private class FakeOrderRepository : OrderRepository {
    private val orders = MutableStateFlow<List<Order>>(emptyList())

    /** When set, getOrders and placeOrder return Result.Error with this message. */
    var errorMessage: String? = null

    /** When set, getOrders suspends until completed (to observe loading states). */
    var gate: CompletableDeferred<Unit>? = null

    fun seed(vararg seeded: Order) {
        orders.value = seeded.toList()
    }

    override fun observeOrders(): Flow<List<Order>> = orders.asStateFlow()

    override suspend fun getOrders(): Result<List<Order>> {
        gate?.await()
        errorMessage?.let { return Result.Error(it) }
        return Result.Success(orders.value)
    }

    /** When set, placeOrderAndClearCart suspends until completed (to observe the in-flight state). */
    var placeGate: CompletableDeferred<Unit>? = null

    var placeOrderCalls = 0

    override suspend fun placeOrderAndClearCart(order: Order): Result<Unit> {
        placeOrderCalls++
        placeGate?.await()
        errorMessage?.let { return Result.Error(it) }
        orders.value = orders.value + order
        return Result.Success(Unit)
    }

    override suspend fun getOrderById(id: String): Result<Order?> =
        Result.Success(orders.value.find { it.id == id })

    override suspend fun deleteOrder(id: String): Result<Unit> {
        orders.value = orders.value.filterNot { it.id == id }
        return Result.Success(Unit)
    }

    override suspend fun cancelOrder(orderId: String): Result<Boolean> {
        if (orders.value.none { it.id == orderId }) return Result.Success(false)
        orders.value = orders.value.map { if (it.id == orderId) it.copy(status = OrderStatus.CANCELLED) else it }
        return Result.Success(true)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SharedOrderViewModelTest : MainDispatcherTest() {

    private val cart = FakeCartRepository()

    private fun viewModel(repository: FakeOrderRepository) = SharedOrderViewModel(
        orderRepository = repository,
        placeOrderUseCase = PlaceOrderUseCase(repository),
        manageCartUseCase = ManageCartUseCase(cart, FakeDetailRepository())
    )

    @Test
    fun ordersObserverEmitsWhenRepositoryFlowChanges() = runTest {
        // SH-12 (Turbine): the init-block observeOrders collector must push
        // repository changes into uiState without a manual reload.
        val repository = FakeOrderRepository()
        val vm = viewModel(repository)
        advanceUntilIdle()

        vm.uiState.test {
            assertEquals(0, awaitItem().orderCount)
            repository.seed(testOrder("o1", date = "2026-07-01", total = Money(1200)))
            var state = awaitItem()
            while (state.orders.isEmpty()) state = awaitItem()
            assertEquals(listOf("o1"), state.orders.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun initCollectsPersistedOrdersMostRecentFirst() = runTest {
        val repository = FakeOrderRepository()
        repository.seed(
            testOrder("o1", date = "2026-01-05", total = Money(1000)),
            testOrder("o2", date = "2026-03-01", total = Money(3000))
        )

        val vm = viewModel(repository)
        advanceUntilIdle()

        assertEquals(listOf("o2", "o1"), vm.uiState.value.orders.map { it.id })
        assertEquals(2, vm.uiState.value.orderCount)
        assertEquals(Money(4000), vm.uiState.value.totalSpent)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun loadOrdersShowsLoadingUntilRepositoryResponds() = runTest {
        val repository = FakeOrderRepository()
        repository.seed(testOrder("o1"))
        val vm = viewModel(repository)
        advanceUntilIdle()

        repository.gate = CompletableDeferred()
        val load = launch { vm.loadOrders() }
        runCurrent()
        assertTrue(vm.uiState.value.isLoading, "loading must be visible while the repository call is in flight")

        repository.gate?.complete(Unit)
        advanceUntilIdle()
        load.join()

        assertFalse(vm.uiState.value.isLoading)
        assertEquals(listOf("o1"), vm.uiState.value.orders.map { it.id })
        assertNull(vm.error.value)
    }

    @Test
    fun loadOrdersErrorReportsAndStopsLoading() = runTest {
        val repository = FakeOrderRepository()
        val vm = viewModel(repository)
        advanceUntilIdle()
        repository.errorMessage = "orders backend down"

        vm.loadOrders()

        val error = assertNotNull(vm.error.value)
        assertEquals(MR.strings.error_load_orders_failed.desc(), error.message)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun placeOrderCreatesProcessingOrderAndUpdatesState() = runTest {
        val repository = FakeOrderRepository()
        val vm = viewModel(repository)
        advanceUntilIdle()
        val cartItems = listOf(CocktailCartItem(testCocktail("1", price = Money(1000)), quantity = 2))

        assertTrue(vm.placeOrder(cartItems))
        advanceUntilIdle() // the observed orders flow delivers the new order

        val placed = assertNotNull(vm.uiState.value.currentOrder)
        assertEquals(OrderStatus.PROCESSING, placed.status)
        // Computed from the items + delivery fee
        assertEquals(Money(2000) + DeliveryPolicy.STANDARD_FEE, placed.total)
        assertEquals(
            listOf(OrderItem(name = "Cocktail 1", quantity = 2, price = Money(1000), cocktailId = "1")),
            placed.items
        )
        assertEquals(listOf(placed.id), vm.uiState.value.orders.map { it.id })
        assertFalse(vm.uiState.value.isPlacingOrder)
        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.error.value)
    }

    @Test
    fun placeOrderWithEmptyCartFailsWithoutTouchingRepository() = runTest {
        val repository = FakeOrderRepository()
        val vm = viewModel(repository)
        advanceUntilIdle()

        assertFalse(vm.placeOrder(emptyList()))
        advanceUntilIdle()

        val error = assertNotNull(vm.error.value)
        assertEquals(MR.strings.error_empty_cart_title.desc(), error.title)
        assertEquals(ErrorHandler.ErrorCategory.DATA, error.category)
        assertTrue(vm.uiState.value.orders.isEmpty())
        assertFalse(vm.uiState.value.isPlacingOrder)
    }

    @Test
    fun placeOrderRepositoryFailureSetsOrderFailedError() = runTest {
        val repository = FakeOrderRepository()
        val vm = viewModel(repository)
        advanceUntilIdle()
        repository.errorMessage = "order store failed"
        val cartItems = listOf(CocktailCartItem(testCocktail("1", price = Money(1000)), quantity = 1))

        assertFalse(vm.placeOrder(cartItems))
        advanceUntilIdle()

        val error = assertNotNull(vm.error.value)
        assertEquals(MR.strings.error_order_failed_title.desc(), error.title)
        assertEquals(MR.strings.error_place_order_failed.desc(), error.message)
        assertEquals(ErrorHandler.ErrorCategory.SERVER, error.category)
        assertNull(vm.uiState.value.currentOrder)
        assertFalse(vm.uiState.value.isPlacingOrder)
        assertTrue(vm.uiState.value.orders.isEmpty())
    }

    @Test
    fun retryAfterFailedLoadClearsTheError() = runTest {
        // Retry must be able to leave the error state (it never cleared before)
        val repository = FakeOrderRepository()
        repository.seed(testOrder("o1"))
        val vm = viewModel(repository)
        advanceUntilIdle()
        repository.errorMessage = "orders backend down"
        vm.loadOrders()
        assertNotNull(vm.error.value)

        repository.errorMessage = null
        vm.loadOrders()

        assertNull(vm.error.value, "a successful reload must clear the earlier error")
        assertEquals(listOf("o1"), vm.uiState.value.orders.map { it.id })
    }

    @Test
    fun placeOrderWhileOneIsInFlightIsIgnored() = runTest {
        val repository = FakeOrderRepository()
        val gate = CompletableDeferred<Unit>()
        repository.placeGate = gate
        val vm = viewModel(repository)
        advanceUntilIdle()
        val cartItems = listOf(CocktailCartItem(testCocktail("1", price = Money(1000)), quantity = 1))

        val first = async { vm.placeOrder(cartItems) }
        runCurrent()
        assertTrue(vm.uiState.value.isPlacingOrder)

        assertFalse(vm.placeOrder(cartItems), "a double tap must not order twice")
        gate.complete(Unit)

        assertTrue(first.await())
        assertEquals(1, repository.placeOrderCalls)
        assertEquals(1, vm.uiState.value.orders.size)
        assertFalse(vm.uiState.value.isPlacingOrder)
    }

    @Test
    fun cancelOrderMarksOrderCancelled() = runTest {
        val repository = FakeOrderRepository()
        repository.seed(testOrder("o1", status = OrderStatus.PENDING))
        val vm = viewModel(repository)
        advanceUntilIdle()

        assertTrue(vm.cancelOrder("o1"))
        advanceUntilIdle()

        assertEquals(OrderStatus.CANCELLED, vm.uiState.value.orders.single().status)
    }
}
