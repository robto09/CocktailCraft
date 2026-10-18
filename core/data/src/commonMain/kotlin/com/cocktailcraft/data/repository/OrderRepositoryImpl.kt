package com.cocktailcraft.data.repository

import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import com.cocktailcraft.data.local.LocalDatabase
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderItem
import com.cocktailcraft.domain.model.OrderStatus
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.UUID
import com.cocktailcraft.util.runCatchingResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Orders in the customer_order / customer_order_item tables (see
 * CustomerOrder.sq). [observeOrders] re-emits after every write to either
 * table, so there is no in-memory copy to keep in sync.
 *
 * The status column holds [OrderStatus.storageValue] ("Processing", ...),
 * the same strings stored before the enum existed, so old rows read back
 * unchanged.
 */
internal class OrderRepositoryImpl(
    private val localDatabase: LocalDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : OrderRepository {

    /** One row of the order/item join (item columns are null for an order without items). */
    private class OrderRow(
        val id: String,
        val date: String,
        val total: Double,
        val status: String,
        val itemName: String?,
        val itemQuantity: Long?,
        val itemPrice: Double?
    )

    private fun List<OrderRow>.toOrders(): List<Order> =
        // groupBy keeps first-occurrence order, i.e. the query's order position
        groupBy { it.id }.map { (_, rows) ->
            val order = rows.first()
            Order(
                id = order.id,
                date = order.date,
                items = rows.mapNotNull { row ->
                    row.itemName?.let { name ->
                        OrderItem(
                            name = name,
                            quantity = row.itemQuantity?.toInt() ?: 0,
                            price = row.itemPrice ?: 0.0
                        )
                    }
                },
                total = order.total,
                status = OrderStatus.fromStorageValue(order.status)
            )
        }

    private fun CocktailCraftDatabase.readOrders(): List<Order> =
        customerOrderQueries.selectAllWithItems(::OrderRow).executeAsList().toOrders()

    private fun withId(order: Order): Order =
        if (order.id.isBlank()) order.copy(id = UUID.randomUUID()) else order

    override fun observeOrders(): Flow<List<Order>> =
        localDatabase.observe(ioDispatcher, { customerOrderQueries.selectAllWithItems(::OrderRow) }) { it.toOrders() }

    override suspend fun getOrders(): Result<List<Order>> = withContext(ioDispatcher) {
        runCatchingResult("Failed to load orders") {
            Result.Success(localDatabase.get().readOrders())
        }
    }

    override suspend fun placeOrder(order: Order): Result<Unit> = withContext(ioDispatcher) {
        runCatchingResult("Failed to place order") {
            val db = localDatabase.get()
            db.transaction { db.insertOrderWithItems(withId(order)) }
            Result.Success(Unit)
        }
    }

    override suspend fun placeOrderAndClearCart(order: Order): Result<Unit> = withContext(ioDispatcher) {
        runCatchingResult("Failed to place order") {
            val db = localDatabase.get()
            // One transaction: either the order exists and the cart is empty,
            // or neither changed — a failure can't lose the cart or leave it
            // behind to be ordered twice.
            db.transaction {
                db.insertOrderWithItems(withId(order))
                db.cartItemQueries.deleteAll()
            }
            Result.Success(Unit)
        }
    }

    override suspend fun getOrderById(id: String): Result<Order?> = withContext(ioDispatcher) {
        runCatchingResult("Failed to get order by ID") {
            val rows = localDatabase.get().customerOrderQueries.selectByIdWithItems(id, ::OrderRow).executeAsList()
            Result.Success(rows.toOrders().firstOrNull())
        }
    }

    override suspend fun updateOrderStatus(id: String, status: OrderStatus): Result<Unit> = withContext(ioDispatcher) {
        runCatchingResult("Failed to update order status") {
            localDatabase.get().customerOrderQueries.updateStatus(status = status.storageValue, id = id)
            Result.Success(Unit)
        }
    }

    override suspend fun deleteOrder(id: String): Result<Unit> = withContext(ioDispatcher) {
        runCatchingResult("Failed to delete order") {
            val db = localDatabase.get()
            db.transaction {
                db.customerOrderQueries.deleteItems(id)
                db.customerOrderQueries.deleteOrder(id)
            }
            Result.Success(Unit)
        }
    }

    override suspend fun cancelOrder(orderId: String): Result<Boolean> = withContext(ioDispatcher) {
        runCatchingResult("Failed to cancel order") {
            val db = localDatabase.get()
            // Check-then-update in one transaction so a concurrent status
            // change can't slip in between.
            val cancelled = db.transactionWithResult {
                val status = db.customerOrderQueries.selectStatus(orderId).executeAsOneOrNull()
                    ?.let(OrderStatus::fromStorageValue)
                if (status?.isCancellable == true) {
                    db.customerOrderQueries.updateStatus(status = OrderStatus.CANCELLED.storageValue, id = orderId)
                    true
                } else {
                    false
                }
            }
            Result.Success(cancelled)
        }
    }
}

/**
 * Inserts [order] and its items; a duplicate order id fails. Call inside a
 * transaction. Shared with the legacy Settings import.
 */
internal fun CocktailCraftDatabase.insertOrderWithItems(order: Order) {
    customerOrderQueries.insertOrder(
        id = order.id,
        date = order.date,
        total = order.total,
        status = order.status.storageValue
    )
    order.items.forEachIndexed { index, item ->
        customerOrderQueries.insertItem(
            order_id = order.id,
            position = index.toLong(),
            name = item.name,
            quantity = item.quantity.toLong(),
            price = item.price
        )
    }
}
