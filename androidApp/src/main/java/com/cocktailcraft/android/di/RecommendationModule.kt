package com.cocktailcraft.android.di

import com.cocktailcraft.android.domain.recommendation.CocktailRecommendationEngine
import com.cocktailcraft.domain.repository.CocktailCatalogRepository
import com.cocktailcraft.domain.repository.FavoritesRepository
import org.koin.dsl.module

/**
 * Koin module for recommendation-related dependencies.
 */
val recommendationModule = module {
    // Recommendation Engine
    single { CocktailRecommendationEngine(get<CocktailCatalogRepository>(), get<FavoritesRepository>()) }
}
