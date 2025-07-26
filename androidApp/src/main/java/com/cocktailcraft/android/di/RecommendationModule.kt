package com.cocktailcraft.android.di

import com.cocktailcraft.android.domain.recommendation.CocktailRecommendationEngine
import com.cocktailcraft.android.viewmodel.CocktailDetailViewModelSKIE
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.domain.repository.FavoritesRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for recommendation-related dependencies.
 */
val recommendationModule = module {
    // Recommendation Engine
    single { CocktailRecommendationEngine(get<CocktailRepository>(), get<FavoritesRepository>()) }

    // ViewModels
    viewModel { CocktailDetailViewModelSKIE() }
}
