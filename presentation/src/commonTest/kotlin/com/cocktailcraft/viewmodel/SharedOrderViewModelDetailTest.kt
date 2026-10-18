package com.cocktailcraft.viewmodel

import app.cash.turbine.test
import com.cocktailcraft.domain.config.DeliveryPolicy
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Money
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderItem
import com.cocktailcraft.domain.model.OrderStatus
import com.cocktailcraft.domain.usecase.ManageCartUseCase
import com.cocktailcraft.domain.usecase.PlaceOrderUseCase
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.error_cancellation_failed_title
import com.cocktailcraft.resources.error_reorder_failed
import com.cocktailcraft.resources.order_cancelled_message
import com.cocktailcraft.resources.order_reorder_added
import com.cocktailcraft.testutil.FakeCartRepository
import com.cocktailcraft.testutil.FakeDetailRepository
import com.cocktailcraft.testutil.FakeOrderRepository
import com.cocktailcraft.testutil.MainDispatcherTest
import com.cocktailcraft.testutil.testCocktail
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The order detail screen's slice of SharedOrderViewModel: loading, derived values, cancel. */
@OptIn(ExperimentalCoroutinesApi::class)
class SharedOrderViewModelDetailTest : MainDispatcherTest() {

    private val pendingOrder = Order(
        id = "ORD-42052",
        date = "2026-07-02",
        items = listOf(
            OrderItem(name = "Margarita", quantity = 2, price = Money(1250)),
            OrderItem(name = "Mojito", quantity = 1, price = Money(900))
        ),
        // 34.0 of items + the standard fee, as PlaceOrderUseCase stores it
        total = Money(3400) + DeliveryPolicy.STANDARD_FEE,
        status = OrderStatus.PENDING
    )

    private val cart = FakeCartRepository()

    private val margarita = testCocktail("11007", name = "Margarita", price = Money(1250))
    private val mojito = testCocktail("11000", name = "Mojito", price = Money(900))

    /** Lookups find the cocktails, as the stored catalog would. */
    private val detail = FakeDetailRepository(mutableMapOf(margarita.id to margarita, mojito.id to mojito))

    /** [pendingOrder] as placed today: its items know their cocktails. */
    private val reorderableOrder = pendingOrder.copy(
        id = "ORD-77001",
        status = OrderStatus.DELIVERED,
        items = listOf(
            OrderItem(name = "Margarita", quantity = 2, price = Money(1250), cocktailId = margarita.id),
            OrderItem(name = "Mojito", quantity = 1, price = Money(900), cocktailId = mojito.id)
        )
    )

    private fun viewModel(repository: FakeOrderRepository) = SharedOrderViewModel(
        orderRepository = repository,
        placeOrderUseCase = PlaceOrderUseCase(repository),
        manageCartUseCase = ManageCartUseCase(cart, detail)
    )

    @Test
    fun loadOrderDetailPublishesTheOrderWithItsDerivedValues() = runTest {
        val repository = FakeOrderRepository()
        repository.seed(pendingOrder)
        val vm = viewModel(repository)
        advanceUntilIdle()

        vm.loadOrderDetail(pendingOrder.id)

        val state = vm.uiState.value
        assertEquals(pendingOrder, state.selectedOrder)
        assertFalse(state.isLoadingOrderDetail)
        assertEquals(Money(3400), state.selectedOrderSubtotal)
        assertEquals(DeliveryPolicy.STANDARD_FEE, state.selectedOrderDeliveryFee)
        assertEquals(
            DeliveryPolicy.estimatedDeliveryTimeForStatus(OrderStatus.PENDING),
            state.selectedOrderDeliveryEstimate
        )
        assertTrue(state.canCancelSelectedOrder)
        assertNull(vm.error.value)
    }

    @Test
    fun loadOrderDetailForAnUnknownOrderLeavesNoSelection() = runTest {
        val repository = FakeOrderRepository()
        repository.seed(pendingOrder)
        val vm = viewModel(repository)
        advanceUntilIdle()
        vm.loadOrderDetail(pendingOrder.id)

        vm.loadOrderDetail("ORD-missing")

        assertNull(vm.uiState.value.selectedOrder, "a previous order must not stand in for a missing one")
        assertFalse(vm.uiState.value.isLoadingOrderDetail)
        assertFalse(vm.uiState.value.canCancelSelectedOrder)
        assertNull(vm.error.value)
    }

    @Test
    fun cancelFromDetailUpdatesTheSelectedOrderAndAnnouncesIt() = runTest {
        val repository = FakeOrderRepository()
        repository.seed(pendingOrder)
        val vm = viewModel(repository)
        advanceUntilIdle()
        vm.loadOrderDetail(pendingOrder.id)

        vm.events.test {
            assertTrue(vm.cancelOrder(pendingOrder.id))
            val event = assertIs<UiEvent.Success>(awaitItem())
            assertEquals(MR.strings.order_cancelled_message.desc(), event.message)
            assertNull(event.action, "a cancel cannot be undone, so it offers no action")
        }
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(OrderStatus.CANCELLED, state.selectedOrder?.status)
        assertEquals(OrderStatus.CANCELLED, state.orders.single().status)
        assertFalse(state.canCancelSelectedOrder)
        assertFalse(state.isCancellingOrder)
        assertNull(vm.error.value)
    }

    @Test
    fun cancelOfANonCancellableOrderReportsAndKeepsItsStatus() = runTest {
        val repository = FakeOrderRepository()
        repository.seed(pendingOrder.copy(status = OrderStatus.SHIPPED))
        val vm = viewModel(repository)
        advanceUntilIdle()
        vm.loadOrderDetail(pendingOrder.id)
        assertFalse(vm.uiState.value.canCancelSelectedOrder)

        vm.events.test {
            assertFalse(vm.cancelOrder(pendingOrder.id))
            expectNoEvents()
        }

        val error = assertNotNull(vm.error.value)
        assertEquals(MR.strings.error_cancellation_failed_title.desc(), error.title)
        assertEquals(OrderStatus.SHIPPED, vm.uiState.value.selectedOrder?.status)
        assertFalse(vm.uiState.value.isCancellingOrder)
    }

    @Test
    fun reorderPutsTheItemsBackInTheCartAndAnnouncesHowMany() = runTest {
        val repository = FakeOrderRepository()
        repository.seed(reorderableOrder)
        val vm = viewModel(repository)
        advanceUntilIdle()
        vm.loadOrderDetail(reorderableOrder.id)
        assertTrue(vm.uiState.value.canReorderSelectedOrder)

        val event = vm.eventFrom { assertTrue(vm.reorder(reorderableOrder.id)) }

        val success = assertIs<UiEvent.Success>(event)
        assertEquals(MR.plurals.order_reorder_added.format(3, 3), success.message)
        assertEquals(
            listOf(CocktailCartItem(margarita, 2), CocktailCartItem(mojito, 1)),
            cart.getCartItems().getOrThrow()
        )
        assertFalse(vm.uiState.value.isReordering)
        assertNull(vm.error.value)
    }

    @Test
    fun reorderingTwiceAddsTheQuantitiesAgain() = runTest {
        val repository = FakeOrderRepository()
        repository.seed(reorderableOrder)
        val vm = viewModel(repository)
        advanceUntilIdle()
        vm.loadOrderDetail(reorderableOrder.id)

        vm.eventFrom { vm.reorder(reorderableOrder.id) }
        vm.eventFrom { vm.reorder(reorderableOrder.id) }

        assertEquals(listOf(4, 2), cart.getCartItems().getOrThrow().map { it.quantity })
    }

    @Test
    fun reorderWorksWithoutTheOrderOnScreen() = runTest {
        val repository = FakeOrderRepository()
        repository.seed(reorderableOrder)
        val vm = viewModel(repository)
        advanceUntilIdle()

        vm.eventFrom { assertTrue(vm.reorder(reorderableOrder.id)) }

        assertEquals(2, cart.getCartItems().getOrThrow().size)
    }

    @Test
    fun ordersFromBeforeItemsRecordedTheirCocktailCantBeReordered() = runTest {
        // pendingOrder's items have no cocktail id, like every order migrated
        // from database version 4
        val repository = FakeOrderRepository()
        repository.seed(pendingOrder)
        val vm = viewModel(repository)
        advanceUntilIdle()
        vm.loadOrderDetail(pendingOrder.id)

        assertFalse(vm.uiState.value.canReorderSelectedOrder)
        val event = vm.eventFrom { assertFalse(vm.reorder(pendingOrder.id)) }

        assertEquals(MR.strings.error_reorder_failed.desc(), assertIs<UiEvent.Failure>(event).message)
        assertTrue(cart.getCartItems().getOrThrow().isEmpty())
        assertFalse(vm.uiState.value.isReordering)
    }

    @Test
    fun aFailedReorderIsAnnouncedAndAddsNothing() = runTest {
        val repository = FakeOrderRepository()
        repository.seed(reorderableOrder)
        val vm = viewModel(repository)
        advanceUntilIdle()
        vm.loadOrderDetail(reorderableOrder.id)
        cart.failNextMutation = true

        val event = vm.eventFrom { assertFalse(vm.reorder(reorderableOrder.id)) }

        assertIs<UiEvent.Failure>(event)
        assertTrue(cart.getCartItems().getOrThrow().isEmpty())
        assertFalse(vm.uiState.value.isReordering)
    }

    @Test
    fun reorderOfAnUnknownOrderIsAnnouncedAsAFailure() = runTest {
        val vm = viewModel(FakeOrderRepository())
        advanceUntilIdle()

        assertIs<UiEvent.Failure>(vm.eventFrom { assertFalse(vm.reorder("ORD-missing")) })
        assertTrue(cart.getCartItems().getOrThrow().isEmpty())
    }
}
