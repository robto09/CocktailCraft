package com.cocktailcraft.testutil

import com.cocktailcraft.data.remote.AlcoholicFilterDto
import com.cocktailcraft.data.remote.CategoryDto
import com.cocktailcraft.data.remote.CocktailApi
import com.cocktailcraft.data.remote.CocktailDto
import com.cocktailcraft.data.remote.GlassDto
import com.cocktailcraft.data.remote.IngredientDto
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.domain.repository.CartRepository
import com.cocktailcraft.domain.repository.CocktailCatalogRepository
import com.cocktailcraft.domain.repository.CocktailDetailRepository
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import com.cocktailcraft.domain.repository.CocktailSearchRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeNetworkMonitor(initiallyOnline: Boolean = true) : NetworkMonitor {
    private val _online = MutableStateFlow(initiallyOnline)
    override val isOnline: StateFlow<Boolean> = _online.asStateFlow()
    override fun startMonitoring() {}
    override fun stopMonitoring() {}
    fun setOnline(online: Boolean) {
        _online.value = online
    }
}

class FakeCartRepository : CartRepository {
    private val items = MutableStateFlow<List<CocktailCartItem>>(emptyList())

    /** When true, the next mutating call returns Result.Error (then resets). */
    var failNextMutation = false
    private fun consumeFailFlag() = failNextMutation.also { failNextMutation = false }

    override fun observeCartItems(): Flow<List<CocktailCartItem>> = items
    override suspend fun getCartItems(): Result<List<CocktailCartItem>> = Result.Success(items.value)

    override suspend fun addToCart(cartItem: CocktailCartItem): Result<Unit> {
        if (consumeFailFlag()) return Result.Error("add failed")
        val existing = items.value.find { it.cocktail.id == cartItem.cocktail.id }
        items.value = if (existing != null) {
            items.value.map {
                if (it.cocktail.id == cartItem.cocktail.id) it.copy(quantity = it.quantity + cartItem.quantity) else it
            }
        } else {
            items.value + cartItem
        }
        return Result.Success(Unit)
    }

    override suspend fun removeFromCart(cocktailId: String): Result<Unit> {
        if (consumeFailFlag()) return Result.Error("remove failed")
        items.value = items.value.filterNot { it.cocktail.id == cocktailId }
        return Result.Success(Unit)
    }

    override suspend fun updateQuantity(cocktailId: String, quantity: Int): Result<Unit> {
        if (consumeFailFlag()) return Result.Error("update failed")
        items.value = items.value.map {
            if (it.cocktail.id == cocktailId) it.copy(quantity = quantity) else it
        }
        return Result.Success(Unit)
    }

    override suspend fun clearCart(): Result<Unit> {
        items.value = emptyList()
        return Result.Success(Unit)
    }

    override suspend fun getCartTotal(): Result<Double> =
        Result.Success(items.value.sumOf { it.cocktail.price * it.quantity })
}

class FakeDetailRepository(
    val byId: MutableMap<String, Cocktail> = mutableMapOf()
) : CocktailDetailRepository {
    override suspend fun getCocktailById(id: String): Result<Cocktail?> = Result.Success(byId[id])
    override suspend fun refreshCocktailById(id: String): Result<Cocktail?> = Result.Success(byId[id])
    override suspend fun getRandomCocktail(): Result<Cocktail?> = Result.Success(byId.values.firstOrNull())
    override fun getCocktailImageUrl(cocktail: Cocktail): String = cocktail.imageUrl.orEmpty()
}

class FakeFavoritesRepository : CocktailFavoritesRepository {
    val stored = mutableListOf<Cocktail>()

    override suspend fun getFavoriteCocktails(): Result<List<Cocktail>> = Result.Success(stored.toList())

    override suspend fun addToFavorites(cocktail: Cocktail): Result<Unit> {
        if (stored.none { it.id == cocktail.id }) stored.add(cocktail)
        return Result.Success(Unit)
    }

    override suspend fun removeFromFavorites(cocktail: Cocktail): Result<Unit> {
        stored.removeAll { it.id == cocktail.id }
        return Result.Success(Unit)
    }

    override suspend fun isCocktailFavorite(id: String): Result<Boolean> =
        Result.Success(stored.any { it.id == id })
}

class FakeSearchRepository(var all: List<Cocktail> = emptyList()) : CocktailSearchRepository {
    override suspend fun searchCocktailsByName(name: String): Result<List<Cocktail>> =
        Result.Success(all.filter { it.name.contains(name, ignoreCase = true) })

    override suspend fun searchCocktailsByFirstLetter(letter: Char): Result<List<Cocktail>> =
        Result.Success(all.filter { it.name.startsWith(letter, ignoreCase = true) })

    override suspend fun advancedSearch(filters: SearchFilters): Result<List<Cocktail>> = Result.Success(all)
    override suspend fun filterByIngredient(ingredient: String): Result<List<Cocktail>> = Result.Success(all)
    override suspend fun filterByAlcoholic(alcoholic: Boolean): Result<List<Cocktail>> = Result.Success(all)

    override suspend fun filterByCategory(category: String): Result<List<Cocktail>> =
        Result.Success(all.filter { it.category == category })

    override suspend fun filterByGlass(glass: String): Result<List<Cocktail>> = Result.Success(all)
}

class FakeCatalogRepository(var all: List<Cocktail> = emptyList()) : CocktailCatalogRepository {
    override suspend fun getCategories(): Result<List<String>> =
        Result.Success(all.mapNotNull { it.category }.distinct())

    override suspend fun getGlasses(): Result<List<String>> = Result.Success(emptyList())
    override suspend fun getIngredients(): Result<List<String>> = Result.Success(emptyList())
    override suspend fun getAlcoholicFilters(): Result<List<String>> = Result.Success(emptyList())
    override suspend fun getCocktailsSortedByNewest(): Result<List<Cocktail>> = Result.Success(all)

    override suspend fun getCocktailsSortedByPriceLowToHigh(): Result<List<Cocktail>> =
        Result.Success(all.sortedBy { it.price })

    override suspend fun getCocktailsSortedByPriceHighToLow(): Result<List<Cocktail>> =
        Result.Success(all.sortedByDescending { it.price })

    override suspend fun getCocktailsSortedByPopularity(): Result<List<Cocktail>> =
        Result.Success(all.sortedByDescending { it.popularity })

    override suspend fun getCocktailsByPriceRange(minPrice: Double, maxPrice: Double): Result<List<Cocktail>> =
        Result.Success(all.filter { it.price in minPrice..maxPrice })

    override suspend fun getCocktailsByCategory(category: String): Result<List<Cocktail>> =
        Result.Success(all.filter { it.category == category })

    override suspend fun getCocktailsByIngredient(ingredient: String): Result<List<Cocktail>> =
        Result.Success(all.filter { c -> c.ingredients.any { it.name.equals(ingredient, ignoreCase = true) } })

    override suspend fun getCocktailsByAlcoholicFilter(alcoholicFilter: String): Result<List<Cocktail>> =
        Result.Success(all.filter { it.alcoholic == alcoholicFilter })
}

class FakeOfflineRepository : CocktailOfflineRepository {
    var offlineModeEnabled = false
    var recentlyViewed: List<Cocktail> = emptyList()

    override suspend fun checkApiConnectivity(): Result<Boolean> = Result.Success(true)
    override suspend fun getRecentlyViewedCocktails(): Result<List<Cocktail>> = Result.Success(recentlyViewed)
    override fun setOfflineMode(enabled: Boolean) {
        offlineModeEnabled = enabled
    }

    override fun isOfflineModeEnabled(): Boolean = offlineModeEnabled
}

internal class FakeCocktailApi(var drinks: List<CocktailDto> = emptyList()) : CocktailApi {
    override suspend fun searchCocktailsByName(name: String): List<CocktailDto> =
        drinks.filter { it.name.contains(name, ignoreCase = true) }

    override suspend fun searchCocktailsByFirstLetter(letter: Char): List<CocktailDto> =
        drinks.filter { it.name.startsWith(letter, ignoreCase = true) }

    override suspend fun getCocktailById(id: String): CocktailDto? = drinks.find { it.id == id }
    override suspend fun getRandomCocktail(): CocktailDto? = drinks.firstOrNull()
    override suspend fun filterByIngredient(ingredient: String): List<CocktailDto> = drinks
    override suspend fun filterByAlcoholic(alcoholic: Boolean): List<CocktailDto> = drinks
    override suspend fun filterByCategory(category: String): List<CocktailDto> =
        drinks.filter { it.category == category }

    override suspend fun filterByGlass(glass: String): List<CocktailDto> = drinks
    override suspend fun getCategories(): List<CategoryDto> = emptyList()
    override suspend fun getGlasses(): List<GlassDto> = emptyList()
    override suspend fun getIngredients(): List<IngredientDto> = emptyList()
    override suspend fun getAlcoholicFilters(): List<AlcoholicFilterDto> = emptyList()
    override suspend fun pingApi(): Boolean = true
}
