package com.cocktailcraft.viewmodel

import app.cash.turbine.test
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailIngredient
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderItem
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.usecase.PlaceOrderUseCase
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class OrderViewModelTest {
    
    private lateinit var viewModel: OrderViewModel
    
    @Mock
    private lateinit var orderRepository: OrderRepository
    
    @Mock
    private lateinit var placeOrderUseCase: PlaceOrderUseCase
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = OrderViewModel(orderRepository, placeOrderUseCase)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should have empty orders, no loading, and no error`() = runTest {
        // Given
        val emptyOrderList = emptyList<Order>()
        whenever(orderRepository.getOrderHistory()).thenReturn(flowOf(emptyOrderList))
        
        // When
        viewModel.loadOrders()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        viewModel.orders.test {
            assertEquals(emptyOrderList, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        
        viewModel.isLoading.test {
            assertEquals(false, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        
        viewModel.error.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun `loadOrders should update orders state with repository data`() = runTest {
        // Given
        val ordersList = listOf(
            Order(
                id = "order1",
                date = "2023-05-01",
                items = listOf(OrderItem("Mojito", 2, 10.0)),
                total = 20.0,
                status = "Delivered"
            ),
            Order(
                id = "order2",
                date = "2023-05-02",
                items = listOf(OrderItem("Margarita", 1, 12.0)),
                total = 12.0,
                status = "Processing"
            )
        )
        whenever(orderRepository.getOrderHistory()).thenReturn(flowOf(ordersList))
        
        // When
        viewModel.loadOrders()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        viewModel.orders.test {
            assertEquals(ordersList, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun `addOrder should call repository and reload orders`() = runTest {
        // Given
        val order = Order(
            id = "order3",
            date = "2023-05-03",
            items = listOf(OrderItem("Daiquiri", 1, 11.0)),
            total = 11.0,
            status = "Pending"
        )
        val updatedOrdersList = listOf(order)
        whenever(orderRepository.getOrderHistory()).thenReturn(flowOf(updatedOrdersList))
        
        // When
        viewModel.addOrder(order)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        verify(orderRepository).addOrder(order)
        viewModel.orders.test {
            assertEquals(updatedOrdersList, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun `placeOrder should call useCase and update state on success`() = runTest {
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
            )
        )
        val totalPrice = 20.0
        val newOrder = Order(
            id = "order1",
            date = "2023-05-01",
            items = listOf(OrderItem("Mojito", 2, 10.0)),
            total = 20.0,
            status = "Processing"
        )
        val updatedOrdersList = listOf(newOrder)
        
        whenever(placeOrderUseCase(cartItems, totalPrice)).thenReturn(
            flowOf(Result.Success(newOrder))
        )
        whenever(orderRepository.getOrderHistory()).thenReturn(flowOf(updatedOrdersList))
        
        // When
        viewModel.placeOrder(cartItems, totalPrice)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        verify(placeOrderUseCase).invoke(cartItems, totalPrice)
        viewModel.orders.test {
            assertEquals(updatedOrdersList, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        
        viewModel.isLoading.test {
            assertEquals(false, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        
        viewModel.error.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun `placeOrder should update error state on failure`() = runTest {
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
            )
        )
        val totalPrice = 20.0
        val errorMessage = "Network error"
        
        whenever(placeOrderUseCase(cartItems, totalPrice)).thenReturn(
            flowOf(Result.Error(errorMessage))
        )
        
        // When
        viewModel.placeOrder(cartItems, totalPrice)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        verify(placeOrderUseCase).invoke(cartItems, totalPrice)
        
        viewModel.error.test {
            assertEquals("Failed to place order: $errorMessage", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        
        viewModel.isLoading.test {
            assertEquals(false, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun `placeOrderDirectly should create order and call repository`() = runTest {
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
            )
        )
        val totalPrice = 20.0
        val updatedOrdersList = listOf(
            Order(
                id = "order1",
                date = "2023-05-01",
                items = listOf(OrderItem("Mojito", 2, 10.0)),
                total = 20.0,
                status = "Processing"
            )
        )
        
        // We can't mock the exact order since it uses System.currentTimeMillis()
        whenever(orderRepository.placeOrder(any())).thenReturn(flowOf(true))
        whenever(orderRepository.getOrderHistory()).thenReturn(flowOf(updatedOrdersList))
        
        // When
        viewModel.placeOrderDirectly(cartItems, totalPrice)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        // Verify placeOrder was called with any Order object
        verify(orderRepository).placeOrder(any())
        
        viewModel.orders.test {
            assertEquals(updatedOrdersList, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        
        viewModel.isLoading.test {
            assertEquals(false, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        
        viewModel.error.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun `updateOrderStatus should call repository and reload orders`() = runTest {
        // Given
        val orderId = "order1"
        val newStatus = "Delivered"
        val updatedOrdersList = listOf(
            Order(
                id = orderId,
                date = "2023-05-01",
                items = listOf(OrderItem("Mojito", 2, 10.0)),
                total = 20.0,
                status = newStatus
            )
        )
        
        whenever(orderRepository.getOrderHistory()).thenReturn(flowOf(updatedOrdersList))
        
        // When
        viewModel.updateOrderStatus(orderId, newStatus)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        verify(orderRepository).updateOrderStatus(orderId, newStatus)
        
        viewModel.orders.test {
            assertEquals(updatedOrdersList, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun `cancelOrder should call repository and reload orders on success`() = runTest {
        // Given
        val orderId = "order1"
        val updatedOrdersList = listOf(
            Order(
                id = orderId,
                date = "2023-05-01",
                items = listOf(OrderItem("Mojito", 2, 10.0)),
                total = 20.0,
                status = "Cancelled"
            )
        )
        
        whenever(orderRepository.cancelOrder(orderId)).thenReturn(flowOf(true))
        whenever(orderRepository.getOrderHistory()).thenReturn(flowOf(updatedOrdersList))
        
        // When
        viewModel.cancelOrder(orderId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        verify(orderRepository).cancelOrder(orderId)
        
        viewModel.orders.test {
            assertEquals(updatedOrdersList, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        
        viewModel.error.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun `cancelOrder should update error state on failure`() = runTest {
        // Given
        val orderId = "order1"
        
        whenever(orderRepository.cancelOrder(orderId)).thenReturn(flowOf(false))
        
        // When
        viewModel.cancelOrder(orderId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        verify(orderRepository).cancelOrder(orderId)
        
        viewModel.error.test {
            assertEquals("Failed to cancel order. It may be too late to cancel.", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun `getOrderById should return order from current list if available`() = runTest {
        // Given
        val orderId = "order1"
        val order = Order(
            id = orderId,
            date = "2023-05-01",
            items = listOf(OrderItem("Mojito", 2, 10.0)),
            total = 20.0,
            status = "Processing"
        )
        val ordersList = listOf(order)
        
        // Set up the current orders list
        whenever(orderRepository.getOrderHistory()).thenReturn(flowOf(ordersList))
        viewModel.loadOrders()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When & Then
        viewModel.getOrderById(orderId).test {
            val result = awaitItem()
            assertNotNull(result)
            assertEquals(orderId, result?.id)
            assertEquals("Processing", result?.status)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun `getOrderById should fetch from repository if not in current list`() = runTest {
        // Given
        val orderId = "order2"
        val order = Order(
            id = orderId,
            date = "2023-05-02",
            items = listOf(OrderItem("Margarita", 1, 12.0)),
            total = 12.0,
            status = "Pending"
        )
        
        // Current list doesn't contain the order
        whenever(orderRepository.getOrderHistory()).thenReturn(flowOf(emptyList()))
        viewModel.loadOrders()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Repository will return the order
        whenever(orderRepository.getOrderById(orderId)).thenReturn(flowOf(order))
        
        // When & Then
        viewModel.getOrderById(orderId).test {
            val result = awaitItem()
            assertNotNull(result)
            assertEquals(orderId, result?.id)
            assertEquals("Pending", result?.status)
            cancelAndIgnoreRemainingEvents()
        }
    }
} 