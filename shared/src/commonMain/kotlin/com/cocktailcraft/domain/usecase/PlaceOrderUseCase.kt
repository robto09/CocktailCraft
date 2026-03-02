package com.cocktailcraft.domain.usecase

import co.touchlab.kermit.Logger
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderItem
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrThrow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class PlaceOrderUseCase(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(cartItems: List<CocktailCartItem>, totalPrice: Double): Result<Order> {
        return try {
            Logger.d { "PlaceOrderUseCase: Starting order placement" }
            val orderId = "ORD-${Clock.System.now().toEpochMilliseconds()}"
            val currentInstant = Clock.System.now()
            val currentDate = currentInstant.toLocalDateTime(TimeZone.currentSystemDefault())
                .let { "${it.year}-${it.monthNumber.toString().padStart(2, '0')}-${it.dayOfMonth.toString().padStart(2, '0')}" }

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

            Logger.d { "PlaceOrderUseCase: Created order object, calling repository.addOrder" }
            val addResult = orderRepository.addOrder(order)
            if (addResult.isError()) {
                return Result.Error((addResult as Result.Error).message)
            }

            Logger.d { "PlaceOrderUseCase: Order added to repository, returning success result" }
            Result.Success(order)
        } catch (e: Exception) {
            Logger.e(e) { "PlaceOrderUseCase: Exception occurred: ${e.message}" }
            Result.Error(e.message ?: "Unknown error occurred")
        }
    }
}