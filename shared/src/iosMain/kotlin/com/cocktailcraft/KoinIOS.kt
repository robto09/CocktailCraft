package com.cocktailcraft

import com.cocktailcraft.domain.repository.AuthRepository
import com.cocktailcraft.domain.repository.CartRepository
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.viewmodel.SharedCocktailListViewModel
import com.cocktailcraft.viewmodel.SharedFavoritesViewModel
import com.cocktailcraft.viewmodel.SharedHomeViewModel
import com.cocktailcraft.viewmodel.SharedCartViewModel
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

fun doInitKoin(): KoinApplication = initKoin {}

// Helper class to provide dependencies to iOS without using reified generics
class KoinHelper : KoinComponent {
    fun getCocktailRepository(): CocktailRepository {
        val repository: CocktailRepository by inject()
        return repository
    }

    fun getAuthRepository(): AuthRepository {
        val repository: AuthRepository by inject()
        return repository
    }

    fun getCartRepository(): CartRepository {
        val repository: CartRepository by inject()
        return repository
    }

    fun getOrderRepository(): OrderRepository {
        val repository: OrderRepository by inject()
        return repository
    }

    // Shared ViewModels (Proof of Concept)
    fun getSharedCocktailListViewModel(): SharedCocktailListViewModel {
        val viewModel: SharedCocktailListViewModel by inject()
        return viewModel
    }

    fun getSharedFavoritesViewModel(): SharedFavoritesViewModel {
        val viewModel: SharedFavoritesViewModel by inject()
        return viewModel
    }
    
    fun getSharedHomeViewModel(): SharedHomeViewModel {
        val viewModel: SharedHomeViewModel by inject()
        return viewModel
    }
    
    fun getSharedCartViewModel(): SharedCartViewModel {
        val viewModel: SharedCartViewModel by inject()
        return viewModel
    }
}