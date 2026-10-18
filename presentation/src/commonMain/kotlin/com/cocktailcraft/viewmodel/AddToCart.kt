package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.usecase.ManageCartUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.added_to_cart
import com.cocktailcraft.resources.error_add_item_to_cart_failed
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.util.runCatchingNonCancellation
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format

/**
 * Add-to-cart as every screen offers it (Home, Favorites, Detail): adds
 * [quantity] of [cocktail] and announces the outcome through [announce] —
 * "Added <name> to cart" only on success, else the failure message — so the
 * screen that triggered it can toast it. Returns whether it was added.
 */
internal suspend fun ManageCartUseCase.addToCartAnnounced(
    cocktail: Cocktail,
    quantity: Int,
    announce: (UiEvent) -> Unit
): Boolean {
    val result = runCatchingNonCancellation(onFailure = { Result.Error(it.message.orEmpty()) }) {
        addToCart(CocktailCartItem(cocktail, quantity))
    }
    return when (result) {
        is Result.Success -> {
            announce(UiEvent.Success(MR.strings.added_to_cart.format(cocktail.name)))
            true
        }
        is Result.Error -> {
            announce(
                UiEvent.Failure(
                    ErrorHandler.rawMessage(result.message)
                        ?: MR.strings.error_add_item_to_cart_failed.desc()
                )
            )
            false
        }
    }
}
