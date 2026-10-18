package com.cocktailcraft.testutil

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.util.Result

/**
 * [FakeFavoritesRepository] whose add/remove can be made to fail, for the
 * ViewModels' toggle/clear error paths. [failingMutations] mutations from
 * now on return Result.Error (and change nothing), then it behaves normally.
 */
class FlakyFavoritesRepository(
    private val delegate: FakeFavoritesRepository = FakeFavoritesRepository()
) : CocktailFavoritesRepository by delegate {

    var failingMutations = 0

    val stored: List<Cocktail> get() = delegate.stored

    private fun consumeFailure(): Boolean =
        (failingMutations > 0).also { if (it) failingMutations-- }

    override suspend fun addToFavorites(cocktail: Cocktail): Result<Unit> =
        if (consumeFailure()) Result.Error("favorites store unavailable")
        else delegate.addToFavorites(cocktail)

    override suspend fun removeFromFavorites(cocktail: Cocktail): Result<Unit> =
        if (consumeFailure()) Result.Error("favorites store unavailable")
        else delegate.removeFromFavorites(cocktail)
}
