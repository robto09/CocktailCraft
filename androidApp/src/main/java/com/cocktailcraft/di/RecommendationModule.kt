package com.cocktailcraft.di

import com.cocktailcraft.domain.recommendation.CocktailRecommendationEngine
import org.koin.dsl.module

/**
 * Koin module for recommendation-related dependencies.
 */
val recommendationModule = module {
    // Recommendation Engine
    single { CocktailRecommendationEngine(get(), get()) }
    
    // CocktailDetailViewModel is provided by androidViewModelModule
}
