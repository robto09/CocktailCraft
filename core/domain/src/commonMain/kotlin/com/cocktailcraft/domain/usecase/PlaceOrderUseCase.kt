package com.cocktailcraft.domain.usecase

import co.touchlab.kermit.Logger
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderItem
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.runCatchingResult
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.native.HiddenFromObjC

/**
 * Checkout: turns the cart into an order. The order is stored and the cart
 * emptied in one transaction ([OrderRepository.placeOrderAndClearCart]), so
 * callers must not clear the cart themselves.
 */
@HiddenFromObjC // Public for :presentation; not Swift API (AR-12)
class PlaceOrderUseCase(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(cartItems: List<CocktailCartItem>, totalPrice: Double): Result<Order> {
        return runCatchingResult("Unknown error occurred") {
            Logger.d { "PlaceOrderUseCase: Starting order placement" }
            val orderId = "ORD-${Clock.System.now().toEpochMilliseconds()}"
            val currentInstant = Clock.System.now()
            val currentDate = currentInstant.toLocalDateTime(TimeZone.currentSystemDefault())
                .let { "${it.year}-${it.month.number.toString().padStart(2, '0')}-${it.day.toString().padStart(2, '0')}" }

            Logger.d { "PlaceOrderUseCase: Generated order ID: $orderId" }

            val orderItems = cartItems.map { cartItem ->
                OrderItem(
                    name = cartItem.cocktail.name,
                    quantity = cartItem.quantity,
                    price = cartItem.cocktail.price
                )
            }

            val order = Order(
                id = orderId,
                date = currentDate,
                items = orderItems,
                total = totalPrice,
                status = "Processing"
            )

            Logger.d { "PlaceOrderUseCase: Created order object, calling repository.placeOrderAndClearCart" }
            val placeResult = orderRepository.placeOrderAndClearCart(order)
            if (placeResult.isError()) {
                return Result.Error((placeResult as Result.Error).message)
            }

            Logger.d { "PlaceOrderUseCase: Order added to repository, returning success result" }
            Result.Success(order)
        }
    }
}