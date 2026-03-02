package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail

class SortCocktailsUseCase {
    operator fun invoke(cocktails: List<Cocktail>, sortType: SortType): List<Cocktail> {
        return when (sortType) {
            SortType.PRICE_ASC -> cocktails.sortedBy { it.price }
            SortType.PRICE_DESC -> cocktails.sortedByDescending { it.price }
            SortType.RATING -> cocktails.sortedByDescending { it.rating }
            SortType.POPULARITY -> cocktails.sortedByDescending { it.popularity }
            SortType.NAME_ASC -> cocktails.sortedBy { it.name }
            SortType.NAME_DESC -> cocktails.sortedByDescending { it.name }
        }
    }

    enum class SortType {
        PRICE_ASC, PRICE_DESC, RATING, POPULARITY, NAME_ASC, NAME_DESC
    }
}

