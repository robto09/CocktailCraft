package com.cocktailcraft.android.widget

import android.content.Context
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailDetailRepository
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import com.cocktailcraft.domain.util.getOrDefault
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Data provider for widgets to access cocktail data.
 * Bridges widgets with existing repositories and caching layer.
 * Uses Koin dependency injection to get repositories.
 */
class WidgetDataProvider : KoinComponent {
    
    private val detailRepository: CocktailDetailRepository by inject()
    private val offlineRepository: CocktailOfflineRepository by inject()
    private val favoritesRepository: CocktailFavoritesRepository by inject()
    
    /**
     * Get a random cocktail from the API.
     * Falls back to cached cocktails if offline or API fails.
     */
    suspend fun getRandomCocktail(): Cocktail? {
        return try {
            // Try to get from API first
            detailRepository.getRandomCocktail().getOrNull()
        } catch (e: Exception) {
            // Fallback to cached cocktails if offline
            try {
                val recentlyViewed = offlineRepository.getRecentlyViewedCocktails().getOrDefault(emptyList())
                if (recentlyViewed.isNotEmpty()) {
                    recentlyViewed.random()
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Get the user's favorite cocktails.
     * Uses cached data when available for offline support.
     */
    suspend fun getFavoriteCocktails(): List<Cocktail> {
        return try {
            favoritesRepository.getFavoriteCocktails().getOrDefault(emptyList())
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Get a single favorite cocktail for display.
     * Returns the first favorite or null if no favorites.
     */
    suspend fun getFirstFavoriteCocktail(): Cocktail? {
        return getFavoriteCocktails().firstOrNull()
    }
    
    /**
     * Get the count of favorite cocktails.
     */
    suspend fun getFavoritesCount(): Int {
        return getFavoriteCocktails().size
    }
    
    /**
     * Get cocktail image URL with fallback.
     */
    fun getCocktailImageUrl(cocktail: Cocktail): String {
        return cocktail.imageUrl ?: ""
    }
    
    companion object {
        @Volatile
        private var instance: WidgetDataProvider? = null
        
        fun getInstance(): WidgetDataProvider {
            return instance ?: synchronized(this) {
                instance ?: WidgetDataProvider().also { instance = it }
            }
        }
    }
}

