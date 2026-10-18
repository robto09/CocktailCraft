package com.cocktailcraft.android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocktailcraft.android.navigation.NavigationManager
import com.cocktailcraft.android.ui.components.CartItemCard
import com.cocktailcraft.android.ui.components.CartLoadingShimmer
import com.cocktailcraft.android.ui.components.ConfirmationDialog
import com.cocktailcraft.android.ui.components.EmptyStateComponent
import com.cocktailcraft.android.ui.components.ErrorDialog
import com.cocktailcraft.android.ui.components.ErrorStateView
import com.cocktailcraft.android.ui.components.ErrorToastEffect
import com.cocktailcraft.android.ui.components.OrderSummaryCard
import com.cocktailcraft.android.ui.components.PrimaryButton
import com.cocktailcraft.android.ui.components.rememberCurrencyFormatter
import com.cocktailcraft.android.util.rememberHapticHandler
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.android.util.toFavoriteIdSet
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.cancel
import com.cocktailcraft.resources.cart_confirm
import com.cocktailcraft.resources.cart_empty_message
import com.cocktailcraft.resources.cart_empty_title
import com.cocktailcraft.resources.cart_place_order
import com.cocktailcraft.resources.cart_start_shopping
import com.cocktailcraft.resources.confirm_order_message
import com.cocktailcraft.resources.confirm_order_title
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.viewmodel.SharedCartViewModel
import com.cocktailcraft.viewmodel.SharedFavoritesViewModel
import com.cocktailcraft.viewmodel.SharedOrderViewModel
import com.cocktailcraft.viewmodel.state.CartUiState
import kotlinx.coroutines.launch

@Composable
fun CartScreen(
    viewModel: SharedCartViewModel,
    onStartShopping: () -> Unit,
    navigationManager: NavigationManager,
    orderViewModel: SharedOrderViewModel,
    favoritesViewModel: SharedFavoritesViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val orderState by orderViewModel.uiState.collectAsStateWithLifecycle()
    val favoritesState by favoritesViewModel.uiState.collectAsStateWithLifecycle()
    val favoritesError by favoritesViewModel.error.collectAsStateWithLifecycle()
    val favorites = favoritesState.favorites
    // O(1) membership per row instead of favorites.any {} per visible item (AN-3)
    val favoriteIds = remember(favorites) { favorites.toFavoriteIdSet() }
    val scope = rememberCoroutineScope()

    // A failed checkout, kept here (and taken off the order ViewModel) so it
    // is shown on the cart it belongs to, not later on the Orders tab
    var orderError by remember { mutableStateOf<ErrorHandler.UserFriendlyError?>(null) }

    // Failures with the cart still on screen (a quantity change or removal
    // that was rolled back, a favorite toggle from a row) are toasts
    ErrorToastEffect(
        error = error.takeIf { state.cartItems.isNotEmpty() },
        onConsumed = { viewModel.clearError() }
    )
    ErrorToastEffect(error = favoritesError, onConsumed = { favoritesViewModel.clearError() })

    orderError?.let { failure ->
        ErrorDialog(error = failure, onDismiss = { orderError = null })
    }

    CartContent(
        state = state,
        error = error,
        favoriteIds = favoriteIds,
        isPlacingOrder = orderState.isPlacingOrder,
        onStartShopping = onStartShopping,
        onRetry = { viewModel.refresh() },
        onIncreaseQuantity = { item -> scope.launch { viewModel.updateQuantity(item.cocktail.id, item.quantity + 1) } },
        onDecreaseQuantity = { item ->
            scope.launch {
                if (item.quantity > 1) {
                    viewModel.updateQuantity(item.cocktail.id, item.quantity - 1)
                } else {
                    viewModel.removeFromCart(item.cocktail.id)
                }
            }
        },
        onRemove = { item -> scope.launch { viewModel.removeFromCart(item.cocktail.id) } },
        onToggleFavorite = { item -> scope.launch { favoritesViewModel.toggleFavorite(item.cocktail) } },
        onConfirmOrder = {
            // Navigate only once the order is persisted — this scope dies with
            // the screen, so leaving early would cancel the work. placeOrder
            // empties the cart in the same transaction and leaves it alone if
            // the order fails; it also ignores a second tap while in flight.
            scope.launch {
                // The use case computes the total from the items and DeliveryPolicy
                val placed = orderViewModel.placeOrder(cartItems = state.cartItems)
                if (placed) {
                    navigationManager.navigateToOrderList()
                } else {
                    orderViewModel.error.value?.let { failure ->
                        orderError = failure
                        orderViewModel.clearError()
                    }
                }
            }
        }
    )
}

/**
 * Above this font scale (the 1.3x maximum of Android's standard sizes, i.e.
 * the accessibility "non-linear" sizes), the cart's checkout section scrolls
 * with the items instead of being pinned.
 */
private const val LARGE_FONT_SCALE = 1.3f

/**
 * Stateless cart content: renders [state] and reports every intent upward,
 * so it can be previewed/screenshot-tested without a ViewModel. [error] is
 * rendered full-screen only when there is no cart to show; over a cart it
 * is the caller's toast.
 */
@Composable
internal fun CartContent(
    state: CartUiState,
    error: ErrorHandler.UserFriendlyError?,
    favoriteIds: Set<String>,
    onStartShopping: () -> Unit,
    onIncreaseQuantity: (CocktailCartItem) -> Unit,
    onDecreaseQuantity: (CocktailCartItem) -> Unit,
    onRemove: (CocktailCartItem) -> Unit,
    onToggleFavorite: (CocktailCartItem) -> Unit,
    onConfirmOrder: () -> Unit,
    isPlacingOrder: Boolean = false,
    onRetry: () -> Unit = {}
) {
    val isLoading = state.isLoading
    val cartItems = state.cartItems
    val totalPrice = state.totalPrice
    val hapticHandler = rememberHapticHandler()

    val currencyFormatter = rememberCurrencyFormatter()

    var showPlaceOrderDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Skeleton cart rows while the cart loads
        if (isLoading) {
            CartLoadingShimmer()
        }

        // Nothing to show but the failure: full-screen error with Retry
        if (!isLoading && error != null && cartItems.isEmpty()) {
            ErrorStateView(error = error, onRetry = onRetry)
        }
        // Show empty cart state
        else if (!isLoading && cartItems.isEmpty()) {
            EmptyStateComponent(
                title = stringResource(MR.strings.cart_empty_title),
                message = stringResource(MR.strings.cart_empty_message),
                actionButtonText = stringResource(MR.strings.cart_start_shopping),
                onActionButtonClick = onStartShopping,
                icon = Icons.Filled.ShoppingCart
            )
        }
        // Show cart items
        else if (!isLoading) {
            // Order summary + checkout button: pinned below the list, except
            // at large font scales, where pinning them would leave almost no
            // room for the items — there they scroll with the list instead.
            val checkoutSection: @Composable () -> Unit = {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Order Summary
                    OrderSummaryCard(
                        subtotal = totalPrice,
                        deliveryFee = state.deliveryFee,
                        modifier = Modifier.padding(vertical = 8.dp),
                        currencyFormatter = currencyFormatter
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Checkout Button (confirm haptic, parity with iOS). Shows
                    // progress and takes no taps while the order is placed.
                    PrimaryButton(
                        text = stringResource(MR.strings.cart_place_order),
                        onClick = {
                            hapticHandler.performConfirm()
                            showPlaceOrderDialog = true
                        },
                        isLoading = isPlacingOrder,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            val scrollCheckoutWithItems = LocalDensity.current.fontScale > LARGE_FONT_SCALE

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                // Keyed by cocktail (one row per cocktail), so a removal
                // doesn't rebind every following row to the wrong item
                items(cartItems, key = { it.cocktail.id }) { item ->
                    CartItemCard(
                        item = item,
                        onIncreaseQuantity = { onIncreaseQuantity(item) },
                        onDecreaseQuantity = { onDecreaseQuantity(item) },
                        onRemove = { onRemove(item) },
                        isFavorite = item.cocktail.id in favoriteIds,
                        onToggleFavorite = { onToggleFavorite(item) }
                    )
                }

                if (scrollCheckoutWithItems) {
                    item(key = "checkout") { checkoutSection() }
                }
            }

            if (!scrollCheckoutWithItems) {
                checkoutSection()
            }
        }
    }

    // Order Confirmation Dialog
    ConfirmationDialog(
        showDialog = showPlaceOrderDialog,
        title = stringResource(MR.strings.confirm_order_title),
        message = stringResource(MR.strings.confirm_order_message, currencyFormatter.format(state.finalTotal)),
        confirmButtonText = stringResource(MR.strings.cart_confirm),
        dismissButtonText = stringResource(MR.strings.cancel),
        onConfirm = {
            showPlaceOrderDialog = false
            if (!isPlacingOrder) onConfirmOrder()
        },
        onDismiss = { showPlaceOrderDialog = false }
    )
}
