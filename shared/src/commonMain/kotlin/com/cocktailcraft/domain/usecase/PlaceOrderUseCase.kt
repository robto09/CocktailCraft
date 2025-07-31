package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderItem
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class PlaceOrderUseCase(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(cartItems: List<CocktailCartItem>, totalPrice: Double): Flow<Result<Order>> = flow {
        try {
            println("PlaceOrderUseCase: Starting order placement")
            // Generate order ID and date
            val orderId = "ORD-${Clock.System.now().toEpochMilliseconds()}"
            val currentInstant = Clock.System.now()
            val currentDate = currentInstant.toLocalDateTime(TimeZone.currentSystemDefault())
                .let { "${it.year}-${it.monthNumber.toString().padStart(2, '0')}-${it.dayOfMonth.toString().padStart(2, '0')}" }

            println("PlaceOrderUseCase: Generated order ID: $orderId")

            // Map cart items to order items
            val orderItems = cartItems.map { cartItem ->
                OrderItem(
                    name = cartItem.cocktail.name,
                    quantity = cartItem.quantity,
                    price = cartItem.cocktail.price
                )
            }

            // Create order object
            val order = Order(
                id = orderId,
                date = currentDate,
                items = orderItems,
                total = totalPrice,
                status = "Processing"
            )

            println("PlaceOrderUseCase: Created order object, calling repository.addOrder")
            // Add order to repository
            orderRepository.addOrder(order)

            println("PlaceOrderUseCase: Order added to repository, emitting success result")
            // Emit success result with created order
            emit(Result.Success(order))
            println("PlaceOrderUseCase: Success result emitted")
        } catch (e: Exception) {
            println("PlaceOrderUseCase: Exception occurred: ${e.message}")
            // Emit error result
            emit(Result.Error(e.message ?: "Unknown error occurred"))
        }
    }
} 