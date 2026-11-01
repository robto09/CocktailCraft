package com.cocktailcraft.testutil

import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderStatus
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.util.ErrorCode
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.native.HiddenFromObjC

/** In-memory [OrderRepository]; cancellation follows [OrderStatus.isCancellable] like the real one. */
@HiddenFromObjC // implements a Kotlin-only repository port
class FakeOrderRepository : OrderRepository {
    private val orders = MutableStateFlow<List<Order>>(emptyList())

    /** Every stored order, in placement order. */
    val placedOrders: List<Order> get() = orders.value

    /** Orders that went through the checkout path (placeOrderAndClearCart). */
    val checkedOutOrders = mutableListOf<Order>()

    /** When set, placing an order returns Result.Error with this message and [errorCode]. */
    var errorMessage: String? = null
    var errorCode: ErrorCode = ErrorCode.UNKNOWN

    fun seed(vararg seeded: Order) {
        orders.value = seeded.toList()
    }

    override fun observeOrders(): Flow<List<Order>> = orders.asStateFlow()
    override suspend fun getOrders(): Result<List<Order>> = Result.Success(orders.value)

    override suspend fun placeOrderAndClearCart(order: Order): Result<Unit> {
        val failure = errorMessage?.let { Result.Error(it, errorCode) }
            ?: Result.Error("Duplicate order id ${order.id}").takeIf { orders.value.any { it.id == order.id } }
        if (failure != null) return failure
        orders.value = orders.value + order
        checkedOutOrders += order
        return Result.Success(Unit)
    }

    override suspend fun getOrderById(id: String): Result<Order?> =
        Result.Success(orders.value.find { it.id == id })

    override suspend fun deleteOrder(id: String): Result<Unit> {
        orders.value = orders.value.filterNot { it.id == id }
        return Result.Success(Unit)
    }

    override suspend fun cancelOrder(orderId: String): Result<Boolean> {
        val order = orders.value.find { it.id == orderId } ?: return Result.Success(false)
        if (!order.status.isCancellable) return Result.Success(false)
        orders.value = orders.value.map { if (it.id == orderId) it.copy(status = OrderStatus.CANCELLED) else it }
        return Result.Success(true)
    }
}
