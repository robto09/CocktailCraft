package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.usecase.ManageCartUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.added_to_cart
import com.cocktailcraft.resources.error_add_item_to_cart_failed
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.util.runCatchingResult
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format

/**
 * Add-to-cart as every screen offers it (Home, Favorites, Detail): adds
 * [quantity] of [cocktail] and returns the outcome for the screen that
 * triggered it to toast — "Added <name> to cart" only on success, else the
 * failure message. Never throws but for cancellation.
 */
internal suspend fun ManageCartUseCase.addToCartOutcome(cocktail: Cocktail, quantity: Int): UiEvent {
    val result = runCatchingResult("Failed to add to cart") {
        addToCart(CocktailCartItem(cocktail, quantity))
    }
    return when (result) {
        is Result.Success -> UiEvent.Success(MR.strings.added_to_cart.format(cocktail.name))
        is Result.Error -> UiEvent.Failure(
            ErrorHandler.errorFromResult(result, MR.strings.error_add_item_to_cart_failed.desc()).message
        )
    }
}
