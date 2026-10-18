package com.cocktailcraft.viewmodel

import app.cash.turbine.test
import com.cocktailcraft.domain.config.DeliveryPolicy
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Money
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderItem
import com.cocktailcraft.domain.model.OrderStatus
import com.cocktailcraft.domain.usecase.ManageCartUseCase
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
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The order detail screen's ViewModel: loading, derived values, cancel, reorder. */
@OptIn(ExperimentalCoroutinesApi::class)
class SharedOrderDetailViewModelTest : MainDispatcherTest() {

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

    private fun viewModel(repository: FakeOrderRepository) = SharedOrderDetailViewModel(
        orderRepository = repository,
        manageCartUseCase = ManageCartUseCase(cart, detail)
    )

    /** A detail screen showing order [orderId], loaded. */
    private fun TestScope.screen(repository: FakeOrderRepository, orderId: String): SharedOrderDetailViewModel {
        val vm = viewModel(repository)
        vm.loadOrder(orderId)
        advanceUntilIdle()
        return vm
    }

    @Test
    fun aNewScreenIsLoadingUntilItsOrderIsLookedUp() = runTest {
        val repository = FakeOrderRepository()
        repository.seed(pendingOrder)
        val vm = viewModel(repository)

        assertTrue(vm.uiState.value.isLoading, "not loaded yet is not \"not found\"")
        assertNull(vm.uiState.value.order)
    }

    @Test
    fun loadOrderPublishesTheOrderWithItsDerivedValues() = runTest {
        val repository = FakeOrderRepository()
        repository.seed(pendingOrder)

        val vm = screen(repository, pendingOrder.id)

        val state = vm.uiState.value
        assertEquals(pendingOrder, state.order)
        assertFalse(state.isLoading)
        assertEquals(Money(3400), state.subtotal)
        assertEquals(DeliveryPolicy.STANDARD_FEE, state.deliveryFee)
        assertEquals(
            DeliveryPolicy.estimatedDeliveryTimeForStatus(OrderStatus.PENDING),
            state.deliveryEstimate
        )
        assertTrue(state.canCancel)
        assertNull(vm.error.value)
    }

    @Test
    fun loadingAnUnknownOrderLeavesNothingShown() = runTest {
        val repository = FakeOrderRepository()
        repository.seed(pendingOrder)
        val vm = screen(repository, pendingOrder.id)

        vm.loadOrder("ORD-missing")
        advanceUntilIdle()

        assertNull(vm.uiState.value.order, "a previous order must not stand in for a missing one")
        assertFalse(vm.uiState.value.isLoading)
        assertFalse(vm.uiState.value.canCancel)
        assertNull(vm.error.value)
    }

    @Test
    fun eachScreenHasItsOwnOrder() = runTest {
        // Screen-scoped: a second detail screen never starts out showing
        // the order an earlier one left selected (the Koin single did)
        val repository = FakeOrderRepository()
        repository.seed(pendingOrder, reorderableOrder)
        screen(repository, pendingOrder.id)

        val second = viewModel(repository)

        assertNull(second.uiState.value.order)
        second.loadOrder(reorderableOrder.id)
        advanceUntilIdle()
        assertEquals(reorderableOrder.id, second.uiState.value.order?.id)
    }

    @Test
    fun cancelFromDetailUpdatesTheSelectedOrderAndAnnouncesIt() = runTest {
        val repository = FakeOrderRepository()
        repository.seed(pendingOrder)
        val vm = screen(repository, pendingOrder.id)

        vm.events.test {
            vm.cancelOrder()
            val event = assertIs<UiEvent.Success>(awaitItem())
            assertEquals(MR.strings.order_cancelled_message.desc(), event.message)
            assertNull(event.action, "a cancel cannot be undone, so it offers no action")
        }
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(OrderStatus.CANCELLED, state.order?.status)
        assertEquals(OrderStatus.CANCELLED, repository.getOrderById(pendingOrder.id).getOrThrow()?.status)
        assertFalse(state.canCancel)
        assertFalse(state.isCancelling)
        assertNull(vm.error.value)
    }

    @Test
    fun cancelOfANonCancellableOrderReportsAndKeepsItsStatus() = runTest {
        val repository = FakeOrderRepository()
        repository.seed(pendingOrder.copy(status = OrderStatus.SHIPPED))
        val vm = screen(repository, pendingOrder.id)
        assertFalse(vm.uiState.value.canCancel)

        vm.events.test {
            vm.cancelOrder()
            advanceUntilIdle()
            expectNoEvents()
        }

        val error = assertNotNull(vm.error.value)
        assertEquals(MR.strings.error_cancellation_failed_title.desc(), error.title)
        assertEquals(OrderStatus.SHIPPED, vm.uiState.value.order?.status)
        assertFalse(vm.uiState.value.isCancelling)
    }

    @Test
    fun reorderPutsTheItemsBackInTheCartAndAnnouncesHowMany() = runTest {
        val repository = FakeOrderRepository()
        repository.seed(reorderableOrder)
        val vm = screen(repository, reorderableOrder.id)
        assertTrue(vm.uiState.value.canReorder)

        val event = vm.eventFrom { vm.reorder() }

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
        val vm = screen(repository, reorderableOrder.id)

        vm.eventFrom { vm.reorder() }
        vm.eventFrom { vm.reorder() }

        assertEquals(listOf(4, 2), cart.getCartItems().getOrThrow().map { it.quantity })
    }

    @Test
    fun aSecondReorderTapWhileTheFirstRunsIsIgnored() = runTest {
        val repository = FakeOrderRepository()
        repository.seed(reorderableOrder)
        val vm = screen(repository, reorderableOrder.id)

        vm.reorder()
        vm.reorder()
        advanceUntilIdle()

        assertEquals(listOf(2, 1), cart.getCartItems().getOrThrow().map { it.quantity })
    }

    @Test
    fun ordersFromBeforeItemsRecordedTheirCocktailCantBeReordered() = runTest {
        // pendingOrder's items have no cocktail id, like every order migrated
        // from database version 4
        val repository = FakeOrderRepository()
        repository.seed(pendingOrder)
        val vm = screen(repository, pendingOrder.id)

        assertFalse(vm.uiState.value.canReorder)
        val event = vm.eventFrom { vm.reorder() }

        assertEquals(MR.strings.error_reorder_failed.desc(), assertIs<UiEvent.Failure>(event).message)
        assertTrue(cart.getCartItems().getOrThrow().isEmpty())
        assertFalse(vm.uiState.value.isReordering)
    }

    @Test
    fun aFailedReorderIsAnnouncedAndAddsNothing() = runTest {
        val repository = FakeOrderRepository()
        repository.seed(reorderableOrder)
        val vm = screen(repository, reorderableOrder.id)
        cart.failNextMutation = true

        val event = vm.eventFrom { vm.reorder() }

        assertIs<UiEvent.Failure>(event)
        assertTrue(cart.getCartItems().getOrThrow().isEmpty())
        assertFalse(vm.uiState.value.isReordering)
    }

    @Test
    fun reorderWithoutAnOrderShownDoesNothing() = runTest {
        val vm = screen(FakeOrderRepository(), "ORD-missing")

        vm.events.test {
            vm.reorder()
            advanceUntilIdle()
            expectNoEvents()
        }
        assertTrue(cart.getCartItems().getOrThrow().isEmpty())
    }
}
