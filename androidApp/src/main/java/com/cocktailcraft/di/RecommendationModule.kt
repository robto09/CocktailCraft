package com.cocktailcraft.di

import com.cocktailcraft.domain.recommendation.CocktailRecommendationEngine
import com.cocktailcraft.viewmodel.CocktailDetailViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for recommendation-related dependencies.
 */
val recommendationModule = module {
    // Recommendation Engine
    single { CocktailRecommendationEngine(get(), get()) }

    // ViewModels
    viewModel { CocktailDetailViewModel(get(), get()) }
}
