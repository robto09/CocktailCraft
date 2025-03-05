package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderItem
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlaceOrderUseCase(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(cartItems: List<CocktailCartItem>, totalPrice: Double): Flow<Result<Order>> = flow {
        try {
            // Generate order ID and date
            val orderId = "ORD-${System.currentTimeMillis()}"
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            
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
            
            // Add order to repository
            orderRepository.addOrder(order)
            
            // Emit success result with created order
            emit(Result.Success(order))
        } catch (e: Exception) {
            // Emit error result
            emit(Result.Error(e.message ?: "Unknown error occurred"))
        }
    }
} 