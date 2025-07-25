package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.domain.repository.CartRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.inject

/**
 * Shared ViewModel for Cocktail Detail functionality.
 * Designed for full SKIE interoperability with iOS.
 *
 * Key SKIE features:
 * - StateFlows are automatically converted to Swift AsyncSequence
 * - Suspend functions become Swift async functions
 * - No FlowCollector bridge needed
 */
class SharedCocktailDetailViewModel : SharedViewModel() {

    private val repository: CocktailRepository by inject()
    private val cartRepository: CartRepository by inject()
    
    // UI State - SKIE will convert these to Swift AsyncSequence
    private val _cocktail = MutableStateFlow<Cocktail?>(null)
    val cocktail: StateFlow<Cocktail?> = _cocktail.asStateFlow()
    
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()
    
    private val _isInCart = MutableStateFlow(false)
    val isInCart: StateFlow<Boolean> = _isInCart.asStateFlow()
    
    private val _cartQuantity = MutableStateFlow(0)
    val cartQuantity: StateFlow<Int> = _cartQuantity.asStateFlow()
    
    private val _relatedCocktails = MutableStateFlow<List<Cocktail>>(emptyList())
    val relatedCocktails: StateFlow<List<Cocktail>> = _relatedCocktails.asStateFlow()
    
    private val _ingredientsByType = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val ingredientsByType: StateFlow<Map<String, List<String>>> = _ingredientsByType.asStateFlow()

    /**
     * Load cocktail details by ID.
     * SKIE will convert this to Swift async function.
     */
    suspend fun loadCocktail(cocktailId: String) {
        setLoading(true)
        clearError()

        try {
            repository.getCocktailById(cocktailId)
                .catch { e -> handleException(e, "Failed to load cocktail details") }
                .collect { loadedCocktail ->
                    _cocktail.value = loadedCocktail
                    if (loadedCocktail != null) {
                        // Check if cocktail is favorite
                        viewModelScope.launch {
                            try {
                                repository.getFavoriteCocktails().collect { favorites ->
                                    _isFavorite.value = favorites.any { it.id == cocktailId }
                                }
                            } catch (e: Exception) {
                                // Silent fail for favorites check
                            }
                        }
                        updateCartStatus(cocktailId)
                        loadRelatedCocktails(loadedCocktail)
                        processIngredients(loadedCocktail)
                    }
                    setLoading(false)
                }
        } catch (e: Exception) {
            handleException(e, "Failed to load cocktail details")
            setLoading(false)
        }
    }

    /**
     * Toggle favorite status.
     * SKIE will convert this to Swift async function.
     */
    suspend fun toggleFavorite() {
        val currentCocktail = _cocktail.value ?: return
        try {
            if (_isFavorite.value) {
                repository.removeFromFavorites(currentCocktail)
            } else {
                repository.addToFavorites(currentCocktail)
            }
            _isFavorite.value = !_isFavorite.value
        } catch (e: Exception) {
            handleException(e, "Failed to update favorites", showAsEvent = true)
        }
    }

    /**
     * Add to cart or update quantity.
     * SKIE will convert this to Swift async function.
     */
    suspend fun addToCart(quantity: Int = 1) {
        val currentCocktail = _cocktail.value ?: return
        try {
            val cartItem = CocktailCartItem(currentCocktail, quantity)
            cartRepository.addToCart(cartItem)
            updateCartStatus(currentCocktail.id)
        } catch (e: Exception) {
            handleException(e, "Failed to add to cart", showAsEvent = true)
        }
    }
    
    /**
     * Update cart quantity.
     * SKIE will convert this to Swift async function.
     */
    suspend fun updateCartQuantity(quantity: Int) {
        val currentCocktail = _cocktail.value ?: return
        try {
            if (quantity > 0) {
                cartRepository.updateQuantity(currentCocktail.id, quantity)
            } else {
                cartRepository.removeFromCart(currentCocktail.id)
            }
            updateCartStatus(currentCocktail.id)
        } catch (e: Exception) {
            handleException(e, "Failed to update cart quantity", showAsEvent = true)
        }
    }
    
    /**
     * Remove from cart.
     * SKIE will convert this to Swift async function.
     */
    suspend fun removeFromCart() {
        val currentCocktail = _cocktail.value ?: return
        try {
            cartRepository.removeFromCart(currentCocktail.id)
            updateCartStatus(currentCocktail.id)
        } catch (e: Exception) {
            handleException(e, "Failed to remove from cart", showAsEvent = true)
        }
    }
    
    // MARK: - Private Helper Methods
    
    private suspend fun updateCartStatus(cocktailId: String) {
        try {
            cartRepository.getCartItems().collect { cartItems ->
                val cartItem = cartItems.find { it.cocktail.id == cocktailId }
                _isInCart.value = cartItem != null
                _cartQuantity.value = cartItem?.quantity ?: 0
            }
        } catch (e: Exception) {
            // Silent fail for cart status
        }
    }
    
    private suspend fun loadRelatedCocktails(cocktail: Cocktail) {
        try {
            // Get cocktails from the same category
            repository.filterByCategory(cocktail.category ?: "Cocktail")
                .catch { /* Silent fail */ }
                .collect { cocktails ->
                    _relatedCocktails.value = cocktails
                        .filter { it.id != cocktail.id }
                        .shuffled()
                        .take(3)
                }
        } catch (e: Exception) {
            // Silent fail for related cocktails
        }
    }
    
    private fun processIngredients(cocktail: Cocktail) {
        val ingredientsByType = mutableMapOf<String, MutableList<String>>()
        
        cocktail.ingredients.forEach { ingredient ->
            val type = categorizeIngredient(ingredient.name)
            ingredientsByType.getOrPut(type) { mutableListOf() }.add(ingredient.name)
        }
        
        _ingredientsByType.value = ingredientsByType.mapValues { it.value.toList() }
    }
    
    private fun categorizeIngredient(ingredientName: String): String {
        val name = ingredientName.lowercase()
        return when {
            name.contains("rum") || name.contains("vodka") || name.contains("gin") ||
            name.contains("whiskey") || name.contains("tequila") || name.contains("bourbon") -> "Spirits"
            name.contains("juice") || name.contains("lime") || name.contains("lemon") ||
            name.contains("orange") -> "Citrus & Juices"
            name.contains("syrup") || name.contains("sugar") || name.contains("honey") -> "Sweeteners"
            name.contains("bitters") || name.contains("salt") || name.contains("pepper") -> "Seasonings"
            else -> "Other"
        }
    }
    
    // MARK: - Synchronous Helper Methods
    
    /**
     * Get shareable text representation of the cocktail.
     */
    fun getShareableText(): String {
        val cocktail = _cocktail.value ?: return ""
        
        val ingredients = cocktail.ingredients.joinToString("\n") {
            "• ${it.measure} ${it.name}".trim()
        }
        
        return """
            🍹 ${cocktail.name}
            
            Ingredients:
            $ingredients
            
            Instructions:
            ${cocktail.instructions}
            
            Glass: ${cocktail.glass}
            Category: ${cocktail.category}
        """.trimIndent()
    }
    
    /**
     * Get nutrition facts (example calculation).
     */
    fun getNutritionFacts(): Map<String, String> {
        val cocktail = _cocktail.value ?: return emptyMap()
        
        // Simple estimation based on ingredients
        var calories = 0
        var alcohol = 0.0
        
        cocktail.ingredients.forEach { ingredient ->
            val name = ingredient.name.lowercase()
            when {
                name.contains("rum") || name.contains("vodka") || name.contains("gin") ||
                name.contains("whiskey") || name.contains("tequila") -> {
                    calories += 65 // ~65 calories per oz of spirits
                    alcohol += 14.0 // ~14g alcohol per oz
                }
                name.contains("juice") -> calories += 15 // ~15 calories per oz of juice
                name.contains("syrup") || name.contains("sugar") -> calories += 25 // ~25 calories per oz
            }
        }
        
        return mapOf(
            "Calories" to "${calories} kcal",
            "Alcohol" to "${alcohol.toInt()}g",
            "Carbs" to "${(calories * 0.1).toInt()}g",
            "Sugar" to "${(calories * 0.15).toInt()}g"
        )
    }
    
    /**
     * Refresh cocktail details.
     */
    fun refresh() {
        val currentCocktail = _cocktail.value
        if (currentCocktail != null) {
            viewModelScope.launch {
                loadCocktail(currentCocktail.id)
            }
        }
    }
}