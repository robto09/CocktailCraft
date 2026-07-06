package com.cocktailcraft.android.fakes

import com.cocktailcraft.domain.model.Address
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.domain.model.User
import com.cocktailcraft.domain.model.UserPreferences
import com.cocktailcraft.domain.repository.AuthRepository
import com.cocktailcraft.domain.repository.CartRepository
import com.cocktailcraft.domain.repository.CocktailCatalogRepository
import com.cocktailcraft.domain.repository.CocktailDetailRepository
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import com.cocktailcraft.domain.repository.CocktailSearchRepository
import com.cocktailcraft.domain.repository.FavoritesRepository
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Deterministic fixtures the fake repositories serve. */
object TestCocktails {
    val mojito = Cocktail(id = "1", name = "Test Mojito", category = "Cocktail", alcoholic = "Alcoholic", price = 8.50)
    val negroni = Cocktail(id = "2", name = "Test Negroni", category = "Cocktail", alcoholic = "Alcoholic", price = 11.00)
    val cola = Cocktail(id = "3", name = "Test Cola", category = "Soft Drink", alcoholic = "Non alcoholic", price = 3.25)
    val all = listOf(mojito, negroni, cola)
}

/**
 * One fake bound to all cocktail-facing repository interfaces, mirroring
 * production where a single impl backs the five focused interfaces.
 */
class FakeCocktailRepository(
    private val cocktails: List<Cocktail> = TestCocktails.all
) : CocktailSearchRepository, CocktailCatalogRepository, CocktailDetailRepository,
    CocktailFavoritesRepository, CocktailOfflineRepository, FavoritesRepository {

    private val favorites = MutableStateFlow<List<Cocktail>>(emptyList())
    private var offlineMode = false

    // Search
    override suspend fun searchCocktailsByName(name: String): Result<List<Cocktail>> =
        Result.Success(cocktails.filter { it.name.contains(name, ignoreCase = true) })
    override suspend fun searchCocktailsByFirstLetter(letter: Char): Result<List<Cocktail>> =
        Result.Success(cocktails.filter { it.name.startsWith(letter, ignoreCase = true) })
    override suspend fun advancedSearch(filters: SearchFilters): Result<List<Cocktail>> = Result.Success(cocktails)
    override suspend fun filterByIngredient(ingredient: String): Result<List<Cocktail>> = Result.Success(cocktails)
    override suspend fun filterByAlcoholic(alcoholic: Boolean): Result<List<Cocktail>> = Result.Success(cocktails)
    override suspend fun filterByCategory(category: String): Result<List<Cocktail>> =
        Result.Success(cocktails.filter { it.category == category })
    override suspend fun filterByGlass(glass: String): Result<List<Cocktail>> = Result.Success(cocktails)

    // Catalog
    override suspend fun getCategories(): Result<List<String>> =
        Result.Success(cocktails.mapNotNull { it.category }.distinct())
    override suspend fun getGlasses(): Result<List<String>> = Result.Success(emptyList())
    override suspend fun getIngredients(): Result<List<String>> = Result.Success(emptyList())
    override suspend fun getAlcoholicFilters(): Result<List<String>> = Result.Success(emptyList())
    override suspend fun getCocktailsSortedByNewest(): Result<List<Cocktail>> = Result.Success(cocktails)
    override suspend fun getCocktailsSortedByPriceLowToHigh(): Result<List<Cocktail>> =
        Result.Success(cocktails.sortedBy { it.price })
    override suspend fun getCocktailsSortedByPriceHighToLow(): Result<List<Cocktail>> =
        Result.Success(cocktails.sortedByDescending { it.price })
    override suspend fun getCocktailsSortedByPopularity(): Result<List<Cocktail>> = Result.Success(cocktails)
    override suspend fun getCocktailsByPriceRange(minPrice: Double, maxPrice: Double): Result<List<Cocktail>> =
        Result.Success(cocktails.filter { it.price in minPrice..maxPrice })
    override suspend fun getCocktailsByCategory(category: String): Result<List<Cocktail>> =
        Result.Success(cocktails.filter { it.category == category })
    override suspend fun getCocktailsByIngredient(ingredient: String): Result<List<Cocktail>> = Result.Success(cocktails)
    override suspend fun getCocktailsByAlcoholicFilter(alcoholicFilter: String): Result<List<Cocktail>> =
        Result.Success(cocktails)

    // Detail
    override suspend fun getCocktailById(id: String): Result<Cocktail?> =
        Result.Success(cocktails.firstOrNull { it.id == id })
    override suspend fun refreshCocktailById(id: String): Result<Cocktail?> = getCocktailById(id)
    override suspend fun getRandomCocktail(): Result<Cocktail?> = Result.Success(cocktails.firstOrNull())
    override fun getCocktailImageUrl(cocktail: Cocktail): String = cocktail.imageUrl ?: ""

    // Favorites (both interfaces)
    override suspend fun getFavoriteCocktails(): Result<List<Cocktail>> = Result.Success(favorites.value)
    override suspend fun getFavorites(): Result<List<Cocktail>> = Result.Success(favorites.value)
    override suspend fun addToFavorites(cocktail: Cocktail): Result<Unit> {
        favorites.update { it + cocktail }
        return Result.Success(Unit)
    }
    override suspend fun removeFromFavorites(cocktail: Cocktail): Result<Unit> {
        favorites.update { list -> list.filterNot { it.id == cocktail.id } }
        return Result.Success(Unit)
    }
    override suspend fun isCocktailFavorite(id: String): Result<Boolean> =
        Result.Success(favorites.value.any { it.id == id })
    override suspend fun isFavorite(id: String): Result<Boolean> = isCocktailFavorite(id)
    override suspend fun toggleFavorite(cocktail: Cocktail): Result<Unit> =
        if (favorites.value.any { it.id == cocktail.id }) removeFromFavorites(cocktail) else addToFavorites(cocktail)

    // Offline
    override suspend fun checkApiConnectivity(): Result<Boolean> = Result.Success(true)
    override suspend fun getRecentlyViewedCocktails(): Result<List<Cocktail>> = Result.Success(emptyList())
    override fun setOfflineMode(enabled: Boolean) { offlineMode = enabled }
    override fun isOfflineModeEnabled(): Boolean = offlineMode
}

class FakeCartRepository : CartRepository {
    private val items = MutableStateFlow<List<CocktailCartItem>>(emptyList())

    override fun observeCartItems(): Flow<List<CocktailCartItem>> = items
    override suspend fun getCartItems(): Result<List<CocktailCartItem>> = Result.Success(items.value)
    override suspend fun addToCart(cartItem: CocktailCartItem): Result<Unit> {
        items.update { list ->
            val existing = list.firstOrNull { it.cocktail.id == cartItem.cocktail.id }
            if (existing != null) {
                list.map {
                    if (it.cocktail.id == cartItem.cocktail.id)
                        it.copy(quantity = it.quantity + cartItem.quantity) else it
                }
            } else list + cartItem
        }
        return Result.Success(Unit)
    }
    override suspend fun removeFromCart(cocktailId: String): Result<Unit> {
        items.update { list -> list.filterNot { it.cocktail.id == cocktailId } }
        return Result.Success(Unit)
    }
    override suspend fun updateQuantity(cocktailId: String, quantity: Int): Result<Unit> {
        items.update { list -> list.map { if (it.cocktail.id == cocktailId) it.copy(quantity = quantity) else it } }
        return Result.Success(Unit)
    }
    override suspend fun clearCart(): Result<Unit> {
        items.value = emptyList()
        return Result.Success(Unit)
    }
    override suspend fun getCartTotal(): Result<Double> =
        Result.Success(items.value.sumOf { it.cocktail.price * it.quantity })
}

class FakeOrderRepository : OrderRepository {
    private val orders = MutableStateFlow<List<Order>>(emptyList())

    override fun observeOrders(): Flow<List<Order>> = orders
    override suspend fun getOrders(): Result<List<Order>> = Result.Success(orders.value)
    override suspend fun addOrder(order: Order): Result<Unit> {
        orders.update { it + order }
        return Result.Success(Unit)
    }
    override suspend fun getOrderById(id: String): Result<Order?> =
        Result.Success(orders.value.firstOrNull { it.id == id })
    override suspend fun updateOrderStatus(id: String, status: String): Result<Unit> = Result.Success(Unit)
    override suspend fun deleteOrder(id: String): Result<Unit> {
        orders.update { list -> list.filterNot { it.id == id } }
        return Result.Success(Unit)
    }
    override suspend fun placeOrder(order: Order): Result<Boolean> {
        orders.update { it + order }
        return Result.Success(true)
    }
    override suspend fun getOrderHistory(): Result<List<Order>> = Result.Success(orders.value)
    override suspend fun cancelOrder(orderId: String): Result<Boolean> = Result.Success(true)
}

class FakeAuthRepository : AuthRepository {
    private var user: User? = null

    override suspend fun signUp(email: String, password: String): Result<Boolean> {
        user = User(id = "test", name = "Test User", email = email)
        return Result.Success(true)
    }
    override suspend fun signIn(email: String, password: String): Result<Boolean> {
        user = User(id = "test", name = "Test User", email = email)
        return Result.Success(true)
    }
    override suspend fun signOut(): Result<Boolean> {
        user = null
        return Result.Success(true)
    }
    override suspend fun changePassword(oldPassword: String, newPassword: String): Result<Boolean> =
        Result.Success(true)
    override suspend fun isUserSignedIn(): Result<Boolean> = Result.Success(user != null)
    override suspend fun getCurrentUser(): Result<User?> = Result.Success(user)
    override suspend fun updateUserName(name: String): Result<Boolean> = Result.Success(true)
    override suspend fun updateUserEmail(email: String, password: String): Result<Boolean> = Result.Success(true)
    override suspend fun updateUserAddress(address: Address): Result<Boolean> = Result.Success(true)
    override suspend fun updateUserPreferences(preferences: UserPreferences): Result<Boolean> = Result.Success(true)
    override suspend fun getUserPreferences(): Result<UserPreferences> = Result.Success(UserPreferences())
}

class FakeNetworkMonitor(initiallyOnline: Boolean = true) : NetworkMonitor {
    private val _isOnline = MutableStateFlow(initiallyOnline)
    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    override fun startMonitoring() = Unit
    override fun stopMonitoring() = Unit

    fun setOnline(online: Boolean) {
        _isOnline.value = online
    }
}
