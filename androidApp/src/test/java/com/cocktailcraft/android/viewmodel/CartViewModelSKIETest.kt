package com.cocktailcraft.android.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.viewmodel.SharedCartViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.Rule
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for CartViewModelSKIE using JUnit Jupiter, kotlin-test, and MockK.
 * Tests the Android wrapper around SharedCartViewModel.
 */
@ExperimentalCoroutinesApi
class CartViewModelSKIETest : KoinTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var cartViewModel: CartViewModelSKIE
    private lateinit var mockSharedViewModel: SharedCartViewModel

    // Mock StateFlows
    private val cartItemsFlow = MutableStateFlow<List<CocktailCartItem>>(emptyList())
    private val totalPriceFlow = MutableStateFlow(0.0)
    private val itemCountFlow = MutableStateFlow(0)
    private val isLoadingFlow = MutableStateFlow(false)
    private val errorFlow = MutableStateFlow<ErrorHandler.UserFriendlyError?>(null)

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this)

        // Stop any existing Koin instance
        try {
            stopKoin()
        } catch (e: Exception) {
            // Ignore if no Koin instance exists
        }

        // Mock SharedCartViewModel
        mockSharedViewModel = mockk(relaxed = true) {
            every { cartItems } returns cartItemsFlow
            every { totalPrice } returns totalPriceFlow
            every { itemCount } returns itemCountFlow
            every { isLoading } returns isLoadingFlow
            every { error } returns errorFlow
            every { isEmpty } returns true
            every { hasItems } returns false
            every { getEstimatedDeliveryTime() } returns "30-45 minutes"
            every { isFreeDelivery() } returns false
            every { getDeliveryFee() } returns 5.99
            every { getFinalTotal() } returns 25.99
        }

        // Setup Koin with mocked dependencies
        startKoin {
            modules(module {
                single<SharedCartViewModel> { mockSharedViewModel }
            })
        }

        cartViewModel = CartViewModelSKIE()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    @Test
    fun `viewModel is properly initialized`() = runTest {
        // When & Then - Just verify the ViewModel can be created and basic properties exist
        assertFalse(cartViewModel.isLoading.value)
        assertEquals(0, cartViewModel.itemCount.value)
        assertEquals(0.0, cartViewModel.totalPrice.value)
        assertEquals(emptyList(), cartViewModel.cartItems.value)
    }


    @Test
    fun `viewModel has basic properties accessible`() = runTest {
        // When & Then - Test that basic properties are accessible
        assertTrue(cartViewModel.isEmpty)
        assertFalse(cartViewModel.hasItems)
    }

    // Note: onCleared is protected in ViewModel, so we can't test it directly

    /**
     * Helper function to create test cocktail data
     */
    private fun createTestCocktail(id: String, name: String) = Cocktail(
        id = id,
        name = name,
        instructions = "Test instructions for $name",
        imageUrl = "test_image_$id.jpg",
        category = "Test Category",
        alcoholic = "Alcoholic",
        glass = "Test Glass",
        ingredients = emptyList(),
        price = 15.0,
        rating = 4.5f
    )

    /**
     * Helper function to create test cart item data
     */
    private fun createTestCartItem(id: String, name: String, quantity: Int) = CocktailCartItem(
        cocktail = createTestCocktail(id, name),
        quantity = quantity
    )
}
