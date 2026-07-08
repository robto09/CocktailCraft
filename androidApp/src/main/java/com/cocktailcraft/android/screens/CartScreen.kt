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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.android.R
import com.cocktailcraft.android.ui.components.CartItemCard
import com.cocktailcraft.android.ui.components.ConfirmationDialog
import com.cocktailcraft.android.ui.components.EmptyStateComponent
import com.cocktailcraft.android.ui.components.LoadingStateComponent
import com.cocktailcraft.android.ui.components.OrderSummaryCard
import com.cocktailcraft.android.ui.components.SectionHeader
import com.cocktailcraft.android.ui.theme.AppColors
import com.cocktailcraft.viewmodel.SharedCartViewModel
import com.cocktailcraft.viewmodel.SharedFavoritesViewModel
import com.cocktailcraft.viewmodel.SharedOrderViewModel
import com.cocktailcraft.android.navigation.NavigationManager
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CartScreen(
    viewModel: SharedCartViewModel,
    onStartShopping: () -> Unit,
    navigationManager: NavigationManager,
    orderViewModel: SharedOrderViewModel,
    favoritesViewModel: SharedFavoritesViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading = state.isLoading
    val cartItems = state.cartItems
    val totalPrice = state.totalPrice
    val error by viewModel.error.collectAsStateWithLifecycle()
    val favoritesState by favoritesViewModel.uiState.collectAsStateWithLifecycle()
    val favorites = favoritesState.favorites
    val scope = rememberCoroutineScope()

    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    var showPlaceOrderDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(16.dp)
    ) {
        // Show loading state
        LoadingStateComponent(isLoading = isLoading)

        // Show error state
        if (!isLoading && error != null) {
            EmptyStateComponent(
                title = stringResource(R.string.error),
                message = error?.toString() ?: stringResource(R.string.cart_unknown_error),
                actionButtonText = stringResource(R.string.cart_try_again),
                onActionButtonClick = { /* Add retry logic here */ }
            )
        }
        // Show empty cart state
        else if (!isLoading && cartItems.isEmpty()) {
            EmptyStateComponent(
                title = stringResource(R.string.cart_empty_title),
                message = stringResource(R.string.cart_empty_message),
                actionButtonText = stringResource(R.string.cart_start_shopping),
                onActionButtonClick = onStartShopping,
                icon = Icons.Filled.ShoppingCart
            )
        }
        // Show cart items
        else if (!isLoading) {
            SectionHeader(
                title = stringResource(R.string.cart_section_title),
                fontSize = 20,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                itemsIndexed(cartItems) { _, item ->
                    CartItemCard(
                        item = item,
                        onIncreaseQuantity = { scope.launch { viewModel.updateQuantity(item.cocktail.id, item.quantity + 1) } },
                        onDecreaseQuantity = {
                            scope.launch {
                                if (item.quantity > 1) {
                                    viewModel.updateQuantity(item.cocktail.id, item.quantity - 1)
                                } else {
                                    viewModel.removeFromCart(item.cocktail.id)
                                }
                            }
                        },
                        onRemove = { scope.launch { viewModel.removeFromCart(item.cocktail.id) } },
                        isFavorite = favorites.any { it.id == item.cocktail.id },
                        onToggleFavorite = { scope.launch { favoritesViewModel.toggleFavorite(item.cocktail) } }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Order Summary
            OrderSummaryCard(
                subtotal = totalPrice,
                deliveryFee = 5.99,
                modifier = Modifier.padding(vertical = 8.dp),
                currencyFormatter = currencyFormatter
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Checkout Button
            Button(
                onClick = { showPlaceOrderDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.cart_place_order),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }

    // Order Confirmation Dialog
    ConfirmationDialog(
        showDialog = showPlaceOrderDialog,
        title = stringResource(R.string.confirm_order_title),
        message = stringResource(R.string.confirm_order_message, currencyFormatter.format(totalPrice + 5.99)),
        confirmButtonText = stringResource(R.string.cart_confirm),
        dismissButtonText = stringResource(R.string.cancel),
        onConfirm = {
            showPlaceOrderDialog = false
            // Navigate only after the order is persisted and the cart cleared —
            // this scope dies with the screen, so leaving early would cancel the work.
            scope.launch {
                orderViewModel.placeOrder(
                    cartItems = cartItems,
                    totalPrice = totalPrice + 5.99 // Include delivery fee
                )
                viewModel.clearCart()
                navigationManager.navigateToOrderList()
            }
        },
        onDismiss = { showPlaceOrderDialog = false }
    )
}

