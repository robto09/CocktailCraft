package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.util.ErrorUtils
import com.cocktailcraft.util.NetworkMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    @Mock
    private lateinit var cocktailRepository: CocktailRepository
    
    @Mock
    private lateinit var networkMonitor: NetworkMonitor
    
    private lateinit var viewModel: HomeViewModel
    
    private val sampleCocktails = listOf(
        Cocktail(
            id = "1",
            name = "Mojito",
            description = "A refreshing cocktail",
            imageUrl = "https://example.com/mojito.jpg",
            ingredients = listOf("Rum", "Mint", "Lime", "Sugar", "Soda"),
            instructions = "Muddle mint and sugar, add rum and lime, top with soda",
            category = "Refreshing",
            price = 8.99,
            rating = 4.5,
            popularity = 95,
            isAlcoholic = true
        ),
        Cocktail(
            id = "2",
            name = "Margarita",
            description = "A classic tequila cocktail",
            imageUrl = "https://example.com/margarita.jpg",
            ingredients = listOf("Tequila", "Triple Sec", "Lime Juice", "Salt"),
            instructions = "Shake ingredients with ice, strain into salt-rimmed glass",
            category = "Classic",
            price = 9.99,
            rating = 4.7,
            popularity = 98,
            isAlcoholic = true
        )
    )
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        // Setup default mocks
        `when`(networkMonitor.isNetworkAvailable()).thenReturn(flowOf(true))
        `when`(cocktailRepository.getCocktailsSortedByNewest()).thenReturn(flowOf(sampleCocktails))
        
        viewModel = HomeViewModel(cocktailRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should have empty cocktails and no error`() = runTest {
        // Initial state before loading
        assertTrue(viewModel.cocktails.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
        assertEquals("", viewModel.errorString.value)
        assertNull(viewModel.error.value)
    }
    
    @Test
    fun `loadCocktails should update cocktails state`() = runTest {
        // When
        viewModel.loadCocktails()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertEquals(sampleCocktails, viewModel.cocktails.value)
        assertFalse(viewModel.isLoading.value)
        assertEquals("", viewModel.errorString.value)
        assertNull(viewModel.error.value)
    }
    
    @Test
    fun `loadCocktails should handle network error`() = runTest {
        // Given
        val errorMessage = "Network error"
        `when`(cocktailRepository.getCocktailsSortedByNewest()).thenThrow(RuntimeException(errorMessage))
        
        // When
        viewModel.loadCocktails()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertTrue(viewModel.cocktails.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
        assertTrue(viewModel.errorString.value.isNotEmpty())
        assertNotNull(viewModel.error.value)
        assertEquals(ErrorUtils.ErrorCategory.UNKNOWN, viewModel.error.value?.category)
    }
    
    @Test
    fun `loadCocktails should handle offline mode`() = runTest {
        // Given
        `when`(networkMonitor.isNetworkAvailable()).thenReturn(flowOf(false))
        `when`(cocktailRepository.getRecentlyViewedCocktails()).thenReturn(flowOf(sampleCocktails))
        
        // When
        viewModel.loadCocktails()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertEquals(sampleCocktails, viewModel.cocktails.value)
        assertTrue(viewModel.isOfflineMode.value)
        assertFalse(viewModel.isLoading.value)
    }
    
    @Test
    fun `retry should reload cocktails`() = runTest {
        // Given
        val errorMessage = "Network error"
        `when`(cocktailRepository.getCocktailsSortedByNewest())
            .thenThrow(RuntimeException(errorMessage))
            .thenReturn(flowOf(sampleCocktails))
        
        // When - first load fails
        viewModel.loadCocktails()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then - error state
        assertTrue(viewModel.errorString.value.isNotEmpty())
        assertNotNull(viewModel.error.value)
        
        // When - retry succeeds
        viewModel.retry()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then - success state
        assertEquals(sampleCocktails, viewModel.cocktails.value)
        assertEquals("", viewModel.errorString.value)
        assertNull(viewModel.error.value)
    }
    
    @Test
    fun `clearError should reset error states`() = runTest {
        // Given - set an error
        val errorMessage = "Network error"
        `when`(cocktailRepository.getCocktailsSortedByNewest()).thenThrow(RuntimeException(errorMessage))
        viewModel.loadCocktails()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.clearLegacyError()
        
        // Then
        assertEquals("", viewModel.errorString.value)
        assertNull(viewModel.error.value)
    }
    
    @Test
    fun `setOfflineMode should update repository and state`() = runTest {
        // When
        viewModel.setOfflineMode(true)
        
        // Then
        assertTrue(viewModel.isOfflineMode.value)
        verify(cocktailRepository).setOfflineMode(true)
    }
}
