package com.cocktailcraft.data.repository

import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import com.cocktailcraft.data.cache.db.SelectAllWithItems
import com.cocktailcraft.data.cache.db.SelectByIdWithItems
import com.cocktailcraft.data.local.LocalDatabase
import com.cocktailcraft.domain.model.Money
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
 *
 * Each owner (user, or the guest) sees, cancels and deletes only the orders
 * they placed (see UserDataOwnership.kt).
 */
internal class OrderRepositoryImpl(
    private val localDatabase: LocalDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : OrderRepository {

    private fun List<OrderRow>.toOrders(): List<Order> =
        // groupBy keeps first-occurrence order, i.e. the query's order position
        groupBy { it.id }.map { (_, rows) ->
            val order = rows.first()
            Order(
                id = order.id,
                date = order.date,
                items = rows.mapNotNull { it.item },
                total = Money(order.totalCents),
                status = OrderStatus.fromStorageValue(order.status)
            )
        }

    private fun CocktailCraftDatabase.readOrders(ownerId: String): List<Order> =
        customerOrderQueries.selectAllWithItems(ownerId).executeAsList().map { it.toRow() }.toOrders()

    private fun withId(order: Order): Order =
        if (order.id.isBlank()) order.copy(id = UUID.randomUUID()) else order

    override fun observeOrders(): Flow<List<Order>> =
        localDatabase.observe(ioDispatcher, { ownerId -> customerOrderQueries.selectAllWithItems(ownerId) }) { rows ->
            rows.map { it.toRow() }.toOrders()
        }

    override suspend fun getOrders(): Result<List<Order>> = withContext(ioDispatcher) {
        runCatchingResult("Failed to load orders") {
            Result.Success(localDatabase.get().readOrders(localDatabase.ownerId()))
        }
    }

    override suspend fun placeOrderAndClearCart(order: Order): Result<Unit> = withContext(ioDispatcher) {
        runCatchingResult("Failed to place order") {
            val db = localDatabase.get()
            val ownerId = localDatabase.ownerId()
            // One transaction: either the order exists and the cart is empty,
            // or neither changed — a failure can't lose the cart or leave it
            // behind to be ordered twice.
            db.transaction {
                db.insertOrderWithItems(withId(order), ownerId)
                db.cartItemQueries.deleteAll(ownerId)
            }
            Result.Success(Unit)
        }
    }

    override suspend fun getOrderById(id: String): Result<Order?> = withContext(ioDispatcher) {
        runCatchingResult("Failed to get order by ID") {
            val rows = localDatabase.get().customerOrderQueries.selectByIdWithItems(localDatabase.ownerId(), id)
                .executeAsList()
                .map { it.toRow() }
            Result.Success(rows.toOrders().firstOrNull())
        }
    }

    override suspend fun deleteOrder(id: String): Result<Unit> = withContext(ioDispatcher) {
        runCatchingResult("Failed to delete order") {
            val db = localDatabase.get()
            val ownerId = localDatabase.ownerId()
            db.transaction {
                db.customerOrderQueries.deleteItems(id = id, ownerId = ownerId)
                db.customerOrderQueries.deleteOrder(ownerId = ownerId, id = id)
            }
            Result.Success(Unit)
        }
    }

    override suspend fun cancelOrder(orderId: String): Result<Boolean> = withContext(ioDispatcher) {
        runCatchingResult("Failed to cancel order") {
            val db = localDatabase.get()
            val ownerId = localDatabase.ownerId()
            // Check-then-update in one transaction so a concurrent status
            // change can't slip in between.
            val cancelled = db.transactionWithResult {
                val status = db.customerOrderQueries.selectStatus(ownerId, orderId).executeAsOneOrNull()
                    ?.let(OrderStatus::fromStorageValue)
                if (status?.isCancellable == true) {
                    db.customerOrderQueries.updateStatus(
                        status = OrderStatus.CANCELLED.storageValue,
                        ownerId = ownerId,
                        id = orderId
                    )
                    true
                } else {
                    false
                }
            }
            Result.Success(cancelled)
        }
    }
}

/** One row of the order/item join; [item] is null for an order without items. */
private class OrderRow(val id: String, val date: String, val totalCents: Long, val status: String, val item: OrderItem?)

private fun SelectAllWithItems.toRow() =
    OrderRow(id, date, total_cents, status, orderItem(item_cocktail_id, item_name, item_quantity, item_price_cents))

private fun SelectByIdWithItems.toRow() =
    OrderRow(id, date, total_cents, status, orderItem(item_cocktail_id, item_name, item_quantity, item_price_cents))

/** The item a joined row carries, or null when its item columns are (no items). */
private fun orderItem(cocktailId: String?, name: String?, quantity: Long?, priceCents: Long?): OrderItem? =
    name?.let {
        OrderItem(
            name = it,
            quantity = quantity?.toInt() ?: 0,
            price = Money(priceCents ?: 0L),
            cocktailId = cocktailId
        )
    }

/**
 * Inserts [order] and its items as [ownerId]'s; a duplicate order id fails.
 * Call inside a transaction.
 */
internal fun CocktailCraftDatabase.insertOrderWithItems(order: Order, ownerId: String) {
    customerOrderQueries.insertOrder(
        id = order.id,
        date = order.date,
        totalCents = order.total.cents,
        status = order.status.storageValue,
        ownerId = ownerId
    )
    order.items.forEachIndexed { index, item ->
        customerOrderQueries.insertItem(
            order_id = order.id,
            position = index.toLong(),
            cocktail_id = item.cocktailId,
            name = item.name,
            quantity = item.quantity.toLong(),
            price_cents = item.price.cents
        )
    }
}
