package com.cocktailcraft

import com.cocktailcraft.domain.repository.AuthRepository
import com.cocktailcraft.domain.repository.CartRepository
import com.cocktailcraft.domain.repository.CocktailRepository
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
}