package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.config.DeliveryPolicy
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderItem
import com.cocktailcraft.domain.model.OrderStatus
import com.cocktailcraft.domain.model.sumOfMoney
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.util.ErrorCode
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.UUID
import com.cocktailcraft.util.runCatchingResult
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.native.HiddenFromObjC
import kotlin.time.Clock

/**
 * Checkout: turns the cart into an order. The order is stored and the cart
 * emptied in one transaction ([OrderRepository.placeOrderAndClearCart]), so
 * callers must not clear the cart themselves.
 *
 * The total is computed here from the items and [DeliveryPolicy] — never
 * taken from the caller — so what is stored always matches what was ordered.
 */
@HiddenFromObjC // Public for :presentation; not Swift API (AR-12)
class PlaceOrderUseCase(private val orderRepository: OrderRepository) {
    suspend operator fun invoke(cartItems: List<CocktailCartItem>): Result<Order> {
        return runCatchingResult("Failed to place order") {
            if (cartItems.isEmpty()) {
                return Result.Error("Cannot place an order for an empty cart", ErrorCode.INVALID_DATA)
            }

            val orderId = newOrderId()
            val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                .let {
                    "${it.year}-${it.month.number.toString().padStart(
                        2,
                        '0'
                    )}-${it.day.toString().padStart(2, '0')}"
                }

            val orderItems = cartItems.map { cartItem ->
                OrderItem(
                    name = cartItem.cocktail.name,
                    quantity = cartItem.quantity,
                    price = cartItem.cocktail.price,
                    // Lets the order detail screen offer Reorder
                    cocktailId = cartItem.cocktail.id
                )
            }
            // Whole cents throughout: the stored total is exact
            val subtotal = orderItems.sumOfMoney { it.lineTotal }

            val order = Order(
                id = orderId,
                date = currentDate,
                items = orderItems,
                total = DeliveryPolicy.finalTotal(subtotal),
                status = OrderStatus.PROCESSING
            )

            // A failed placement keeps the repository's message AND code
            when (val placed = orderRepository.placeOrderAndClearCart(order)) {
                is Result.Error -> placed
                else -> Result.Success(order)
            }
        }
    }

    internal companion object {
        /**
         * Random, so two orders placed in the same millisecond (a double
         * tap) can't collide the way the old `ORD-<epoch ms>` ids could.
         */
        fun newOrderId(): String = "ORD-${UUID.randomUUID()}"
    }
}
