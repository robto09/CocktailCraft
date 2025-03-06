package com.cocktailcraft.domain.usecase

import app.cash.turbine.test
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.CocktailIngredient
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class PlaceOrderUseCaseTest {
    
    private lateinit var placeOrderUseCase: PlaceOrderUseCase
    
    @Mock
    private lateinit var orderRepository: OrderRepository
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        placeOrderUseCase = PlaceOrderUseCase(orderRepository)
    }
    
    @Test
    fun `invoke should create order and add to repository`() = runTest {
        // Given
        val cartItems = listOf(
            CocktailCartItem(
                cocktail = Cocktail(
                    id = "cocktail1",
                    name = "Mojito",
                    imageUrl = "mojito.jpg",
                    price = 10.0,
                    ingredients = listOf(
                        CocktailIngredient("Rum", "2 oz"),
                        CocktailIngredient("Mint", "6 leaves"),
                        CocktailIngredient("Lime", "1"),
                        CocktailIngredient("Sugar", "2 tsp"),
                        CocktailIngredient("Soda", "splash")
                    ),
                    instructions = "Muddle mint and lime...",
                    rating = 4.5f,
                    category = "Classic"
                ),
                quantity = 2
            ),
            CocktailCartItem(
                cocktail = Cocktail(
                    id = "cocktail2",
                    name = "Margarita",
                    imageUrl = "margarita.jpg",
                    price = 12.0,
                    ingredients = listOf(
                        CocktailIngredient("Tequila", "2 oz"),
                        CocktailIngredient("Triple Sec", "1 oz"),
                        CocktailIngredient("Lime Juice", "1 oz"),
                        CocktailIngredient("Salt", "pinch")
                    ),
                    instructions = "Shake with ice...",
                    rating = 4.7f,
                    category = "Classic"
                ),
                quantity = 1
            )
        )
        val totalPrice = 32.0 // (10.0 * 2) + (12.0 * 1)
        
        // When & Then
        placeOrderUseCase(cartItems, totalPrice).test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            
            val order = (result as Result.Success).data
            
            // Verify order properties
            assertTrue(order.id.startsWith("ORD-"))
            assertEquals(2, order.items.size)
            assertEquals("Mojito", order.items[0].name)
            assertEquals(2, order.items[0].quantity)
            assertEquals(10.0, order.items[0].price, 0.01)
            assertEquals("Margarita", order.items[1].name)
            assertEquals(1, order.items[1].quantity)
            assertEquals(12.0, order.items[1].price, 0.01)
            assertEquals(totalPrice, order.total, 0.01)
            assertEquals("Processing", order.status)
            
            // Verify repository was called
            verify(orderRepository).addOrder(order)
            
            awaitComplete()
        }
    }
    
    // Create a test implementation of OrderRepository that throws exceptions
    private class ExceptionThrowingOrderRepository : OrderRepository {
        override suspend fun addOrder(order: Order) {
            throw RuntimeException("Network error")
        }
        
        // Implement other methods with default behavior
        override suspend fun getOrderHistory(): Flow<List<Order>> = flowOf(emptyList())
        override suspend fun getOrderById(orderId: String): Flow<Order?> = flowOf(null)
        override suspend fun updateOrderStatus(orderId: String, status: String) {}
        override suspend fun cancelOrder(orderId: String): Flow<Boolean> = flowOf(false)
        override suspend fun placeOrder(order: Order): Flow<Boolean> = flowOf(false)
        override suspend fun deleteOrder(id: String) {}
        override suspend fun getOrders(): Flow<List<Order>> = flowOf(emptyList())
    }
    
    @Test
    fun `invoke should return error result when repository throws exception`() = runTest {
        // Given - Create a use case with a repository that throws exceptions
        val exceptionThrowingRepo = ExceptionThrowingOrderRepository()
        val useCase = PlaceOrderUseCase(exceptionThrowingRepo)
        
        val cartItems = listOf(
            CocktailCartItem(
                cocktail = Cocktail(
                    id = "cocktail1",
                    name = "Mojito",
                    imageUrl = "mojito.jpg",
                    price = 10.0,
                    ingredients = listOf(
                        CocktailIngredient("Rum", "2 oz"),
                        CocktailIngredient("Mint", "6 leaves"),
                        CocktailIngredient("Lime", "1"),
                        CocktailIngredient("Sugar", "2 tsp"),
                        CocktailIngredient("Soda", "splash")
                    ),
                    instructions = "Muddle mint and lime...",
                    rating = 4.5f,
                    category = "Classic"
                ),
                quantity = 2
            )
        )
        val totalPrice = 20.0
        
        // When & Then
        useCase(cartItems, totalPrice).test {
            val result = awaitItem()
            assertTrue(result is Result.Error)
            assertEquals("Network error", (result as Result.Error).message)
            awaitComplete()
        }
    }
} 