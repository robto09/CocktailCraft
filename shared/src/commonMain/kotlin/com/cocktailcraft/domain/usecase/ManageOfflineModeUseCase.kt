package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailCatalogRepository
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrDefault

internal class ManageOfflineModeUseCase(
    private val offlineRepository: CocktailOfflineRepository,
    private val catalogRepository: CocktailCatalogRepository
) {
    suspend fun isOfflineModeEnabled(): Boolean {
        return offlineRepository.isOfflineModeEnabled()
    }

    suspend fun setOfflineMode(enabled: Boolean) {
        offlineRepository.setOfflineMode(enabled)
    }

    suspend fun getRecentlyViewedCocktails(): Result<List<Cocktail>> {
        return offlineRepository.getRecentlyViewedCocktails()
    }

    suspend fun syncCachedData(): Result<List<Cocktail>> {
        return catalogRepository.getCocktailsSortedByNewest()
    }

    suspend fun checkConnectivity(): Result<Boolean> {
        return offlineRepository.checkApiConnectivity()
    }

    suspend fun clearCache(): Result<Unit> {
        return offlineRepository.clearCache()
    }
}

