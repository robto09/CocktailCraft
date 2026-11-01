package com.cocktailcraft.viewmodel.state

import com.cocktailcraft.domain.config.DeliveryPolicy
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Money

/**
 * Consolidated UI state for the Cart screen.
 * Delivery pricing is derived from [totalPrice] via the shared
 * [DeliveryPolicy], so it always matches the emitted cart and stays
 * reactive on both platforms (no snapshot getters on the ViewModel).
 */
data class CartUiState(
    val cartItems: List<CocktailCartItem> = emptyList(),
    /** The items' subtotal, before delivery. */
    val totalPrice: Money = Money.ZERO,
    val itemCount: Int = 0,
    val isLoading: Boolean = false
) {
    val deliveryFee: Money get() = DeliveryPolicy.deliveryFee(totalPrice)
    val finalTotal: Money get() = DeliveryPolicy.finalTotal(totalPrice)
}
