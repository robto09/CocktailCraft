package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailDetailRepository
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.repository.CocktailSearchRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrDefault

class GetCocktailDetailUseCase(
    private val detailRepository: CocktailDetailRepository,
    private val favoritesRepository: CocktailFavoritesRepository,
    private val searchRepository: CocktailSearchRepository
) {
    suspend operator fun invoke(cocktailId: String): Result<Cocktail?> {
        return detailRepository.getCocktailById(cocktailId)
    }

    suspend fun isFavorite(cocktailId: String): Boolean {
        return try {
            val favorites = favoritesRepository.getFavoriteCocktails().getOrDefault(emptyList())
            favorites.any { it.id == cocktailId }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getRelatedCocktails(cocktail: Cocktail, limit: Int = 3): List<Cocktail> {
        return try {
            val cocktails = searchRepository.filterByCategory(cocktail.category ?: "Cocktail").getOrDefault(emptyList())
            cocktails.filter { it.id != cocktail.id }.shuffled().take(limit)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun refresh(cocktailId: String): Result<Cocktail?> {
        return detailRepository.refreshCocktailById(cocktailId)
    }
}

