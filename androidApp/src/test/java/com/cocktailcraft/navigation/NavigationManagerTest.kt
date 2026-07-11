package com.cocktailcraft.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailIngredient
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.isNull

class NavigationManagerTest {
    
    private lateinit var navigationManager: NavigationManager
    private val navController: NavController = mock()
    
    @Before
    fun setup() {
        navigationManager = NavigationManager(navController)
    }
    
    @Test
    fun `navigateToHome should navigate to home route`() {
        navigationManager.navigateToHome()
        verify(navController).navigate(
            eq(Screen.Home.route),
            any<(NavOptionsBuilder) -> Unit>()
        )
    }
    
    @Test
    fun `navigateToCart should navigate to cart route`() {
        navigationManager.navigateToCart()
        verify(navController).navigate(eq(Screen.Cart.route), isNull(), isNull())
    }
    
    @Test
    fun `navigateToOrderList should navigate to order list route`() {
        navigationManager.navigateToOrderList()
        verify(navController).navigate(
            eq(Screen.OrderList.route),
            any<(NavOptionsBuilder) -> Unit>()
        )
    }
    
    @Test
    fun `navigateToProfile should navigate to profile route`() {
        navigationManager.navigateToProfile()
        verify(navController).navigate(
            eq(Screen.Profile.route),
            any<(NavOptionsBuilder) -> Unit>()
        )
    }
    
    @Test
    fun `navigateToFavorites should navigate to favorites route`() {
        navigationManager.navigateToFavorites()
        verify(navController).navigate(
            eq(Screen.Favorites.route),
            any<(NavOptionsBuilder) -> Unit>()
        )
    }
    
    @Test
    fun `navigateToCocktailDetail should navigate to cocktail detail route`() {
        val ingredients = listOf(
            CocktailIngredient("Ingredient 1", "1 oz"),
            CocktailIngredient("Ingredient 2", "2 oz")
        )
        
        val cocktail = Cocktail(
            id = "1",
            name = "Test Cocktail",
            instructions = "Step 1, Step 2",
            imageUrl = "test.jpg",
            price = 9.99,
            ingredients = ingredients,
            rating = 4.5f,
            category = "Test Category"
        )
        
        navigationManager.navigateToCocktailDetail(cocktail)
        verify(navController).navigate(eq("cocktail_detail/1"), isNull(), isNull())
    }
    
    @Test
    fun `navigateToReviews should navigate to reviews route`() {
        val cocktailId = "1"
        navigationManager.navigateToReviews(cocktailId)
        verify(navController).navigate(eq("reviews/1"), isNull(), isNull())
    }
    
    @Test
    fun `navigateBack should pop back stack`() {
        navigationManager.navigateBack()
        verify(navController).popBackStack()
    }
    
    @Test
    fun `navigateToBottomNavDestination should navigate to destination route`() {
        navigationManager.navigateToBottomNavDestination(Screen.Profile)
        verify(navController).navigate(
            eq(Screen.Profile.route),
            any<(NavOptionsBuilder) -> Unit>()
        )
    }
} 