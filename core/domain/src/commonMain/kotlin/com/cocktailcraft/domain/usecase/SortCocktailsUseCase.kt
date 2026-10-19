package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import kotlin.native.HiddenFromObjC

@HiddenFromObjC // Public for :presentation; not Swift API
class SortCocktailsUseCase {
    /**
     * Sorts [cocktails] by [sortType]. The sorts are stable, so ties keep
     * their input order. [ratingOf] supplies the rating that [SortType.RATING]
     * ranks by — e.g. a review average instead of [Cocktail.rating].
     */
    operator fun invoke(
        cocktails: List<Cocktail>,
        sortType: SortType,
        ratingOf: (Cocktail) -> Float = { it.rating }
    ): List<Cocktail> = when (sortType) {
        SortType.PRICE_ASC -> cocktails.sortedBy { it.price }

        SortType.PRICE_DESC -> cocktails.sortedByDescending { it.price }

        SortType.RATING -> cocktails.sortedByDescending(ratingOf)

        SortType.POPULARITY -> cocktails.sortedByDescending { it.popularity }

        // Case-insensitive, so "mai tai" doesn't sort after "Zombie"
        SortType.NAME_ASC -> cocktails.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })

        SortType.NAME_DESC -> cocktails.sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.name })
    }

    enum class SortType {
        PRICE_ASC, PRICE_DESC, RATING, POPULARITY, NAME_ASC, NAME_DESC
    }
}
