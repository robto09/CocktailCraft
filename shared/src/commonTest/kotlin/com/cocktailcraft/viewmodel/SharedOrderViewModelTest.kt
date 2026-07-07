package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderItem
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.usecase.ManageOrdersUseCase
import com.cocktailcraft.domain.usecase.PlaceOrderUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.testutil.MainDispatcherTest
import com.cocktailcraft.testutil.testCocktail
import com.cocktailcraft.util.ErrorHandler
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    total: Double = 20.0,
    status: String = "Processing"
) = Order(
    id = id,
    date = date,
    items = listOf(OrderItem(name = "Cocktail $id", quantity = 2, price = total / 2)),
    total = total,
    status = status
)

private class FakeOrderRepository : OrderRepository {
    private val orders = MutableStateFlow<List<Order>>(emptyList())

    /** When set, getOrders and addOrder return Result.Error with this message. */
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

    override suspend fun addOrder(order: Order): Result<Unit> {
        errorMessage?.let { return Result.Error(it) }
        orders.value = orders.value + order
        return Result.Success(Unit)
    }

    override suspend fun getOrderById(id: String): Result<Order?> =
        Result.Success(orders.value.find { it.id == id })

    override suspend fun updateOrderStatus(id: String, status: String): Result<Unit> {
        orders.value = orders.value.map { if (it.id == id) it.copy(status = status) else it }
        return Result.Success(Unit)
    }

    override suspend fun deleteOrder(id: String): Result<Unit> {
        orders.value = orders.value.filterNot { it.id == id }
        return Result.Success(Unit)
    }

    override suspend fun placeOrder(order: Order): Result<Boolean> = addOrder(order).map { true }

    override suspend fun getOrderHistory(): Result<List<Order>> = getOrders()

    override suspend fun cancelOrder(orderId: String): Result<Boolean> {
        if (orders.value.none { it.id == orderId }) return Result.Success(false)
        updateOrderStatus(orderId, "Cancelled")
        return Result.Success(true)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SharedOrderViewModelTest : MainDispatcherTest() {

    private fun viewModel(repository: FakeOrderRepository) = SharedOrderViewModel(
        manageOrdersUseCase = ManageOrdersUseCase(repository),
        placeOrderUseCase = PlaceOrderUseCase(repository)
    )

    @Test
    fun initCollectsPersistedOrdersMostRecentFirst() = runTest {
        val repository = FakeOrderRepository()
        repository.seed(
            testOrder("o1", date = "2026-01-05", total = 10.0),
            testOrder("o2", date = "2026-03-01", total = 30.0)
        )

        val vm = viewModel(repository)
        advanceUntilIdle()

        assertEquals(listOf("o2", "o1"), vm.uiState.value.orders.map { it.id })
        assertEquals(2, vm.uiState.value.orderCount)
        assertEquals(40.0, vm.uiState.value.totalSpent)
        assertTrue(vm.hasOrders)
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
        assertEquals("orders backend down", error.message)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun placeOrderCreatesProcessingOrderAndUpdatesState() = runTest {
        val repository = FakeOrderRepository()
        val vm = viewModel(repository)
        advanceUntilIdle()
        val cartItems = listOf(CocktailCartItem(testCocktail("1", price = 10.0), quantity = 2))

        assertTrue(vm.placeOrder(cartItems, totalPrice = 20.0))
        advanceUntilIdle() // the observed orders flow delivers the new order

        val placed = assertNotNull(vm.uiState.value.currentOrder)
        assertEquals("Processing", placed.status)
        assertEquals(20.0, placed.total)
        assertEquals(listOf(OrderItem(name = "Cocktail 1", quantity = 2, price = 10.0)), placed.items)
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

        assertFalse(vm.placeOrder(emptyList(), totalPrice = 0.0))
        advanceUntilIdle()

        val error = assertNotNull(vm.error.value)
        assertEquals("Empty Cart", error.title)
        assertEquals(ErrorHandler.ErrorCategory.DATA, error.category)
        assertTrue(vm.isEmpty)
        assertFalse(vm.uiState.value.isPlacingOrder)
    }

    @Test
    fun placeOrderRepositoryFailureSetsOrderFailedError() = runTest {
        val repository = FakeOrderRepository()
        val vm = viewModel(repository)
        advanceUntilIdle()
        repository.errorMessage = "order store failed"
        val cartItems = listOf(CocktailCartItem(testCocktail("1", price = 10.0), quantity = 1))

        assertFalse(vm.placeOrder(cartItems, totalPrice = 10.0))
        advanceUntilIdle()

        val error = assertNotNull(vm.error.value)
        assertEquals("Order Failed", error.title)
        assertEquals("order store failed", error.message)
        assertEquals(ErrorHandler.ErrorCategory.SERVER, error.category)
        assertNull(vm.uiState.value.currentOrder)
        assertFalse(vm.uiState.value.isPlacingOrder)
        assertTrue(vm.isEmpty)
    }

    @Test
    fun cancelOrderMarksOrderCancelled() = runTest {
        val repository = FakeOrderRepository()
        repository.seed(testOrder("o1", status = "Pending"))
        val vm = viewModel(repository)
        advanceUntilIdle()
        assertTrue(vm.canCancelOrder("o1"))

        assertTrue(vm.cancelOrder("o1"))
        advanceUntilIdle()

        assertEquals("Cancelled", vm.uiState.value.orders.single().status)
        assertFalse(vm.canCancelOrder("o1"))
    }

    @Test
    fun updateOrderStatusRefreshesOrders() = runTest {
        val repository = FakeOrderRepository()
        repository.seed(testOrder("o1", status = "Processing"))
        val vm = viewModel(repository)
        advanceUntilIdle()

        assertTrue(vm.updateOrderStatus("o1", "Delivered"))
        advanceUntilIdle()

        assertEquals(listOf("o1"), vm.getOrdersByStatus("delivered").map { it.id })
        assertFalse(vm.uiState.value.isLoading)
    }
}
