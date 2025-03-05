package com.cocktailcraft.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.ui.components.CartItemCard
import com.cocktailcraft.ui.theme.AppColors
import com.cocktailcraft.viewmodel.CartViewModel
import com.cocktailcraft.viewmodel.FavoritesViewModel
import com.cocktailcraft.viewmodel.OrderViewModel
import com.cocktailcraft.navigation.NavigationManager
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CartScreen(
    viewModel: CartViewModel,
    orderViewModel: OrderViewModel,
    navigationManager: NavigationManager,
    onStartShopping: () -> Unit,
    favoritesViewModel: FavoritesViewModel
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val totalPrice by viewModel.totalPrice.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val favorites by favoritesViewModel.favorites.collectAsState()

    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
    
    var showPlaceOrderDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(16.dp)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppColors.Primary)
            }
        } else if (error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = error ?: "An unknown error occurred",
                    color = AppColors.Error,
                    textAlign = TextAlign.Center
                )
            }
        } else if (cartItems.isEmpty()) {
            EmptyCartMessage(onStartShopping)
        } else {
            Text(
                text = "Your Cart",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
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
                        onIncreaseQuantity = { viewModel.updateQuantity(item.cocktail.id, item.quantity + 1) },
                        onDecreaseQuantity = { 
                            if (item.quantity > 1) {
                                viewModel.updateQuantity(item.cocktail.id, item.quantity - 1)
                            } else {
                                viewModel.removeFromCart(item.cocktail.id)
                            }
                        },
                        onRemove = { viewModel.removeFromCart(item.cocktail.id) },
                        isFavorite = favorites.any { it.id == item.cocktail.id },
                        onToggleFavorite = { favoritesViewModel.toggleFavorite(item.cocktail) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Order Summary
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Order Summary",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Subtotal",
                            fontSize = 15.sp,
                            color = AppColors.TextSecondary
                        )
                        Text(
                            text = currencyFormatter.format(totalPrice),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = AppColors.TextPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Delivery Fee",
                            fontSize = 15.sp,
                            color = AppColors.TextSecondary
                        )
                        Text(
                            text = currencyFormatter.format(5.99),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = AppColors.TextPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(thickness = 1.dp, color = AppColors.LightGray)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = AppColors.TextPrimary
                        )
                        Text(
                            text = currencyFormatter.format(totalPrice + 5.99),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = AppColors.Primary
                        )
                    }
                }
            }
            
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
                    text = "Place Order",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
    
    // Order Confirmation Dialog
    if (showPlaceOrderDialog) {
        AlertDialog(
            onDismissRequest = { showPlaceOrderDialog = false },
            title = { 
                Text(
                    "Confirm Order",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = { 
                Text(
                    "Are you sure you want to place this order for ${currencyFormatter.format(totalPrice + 5.99)}?",
                    fontSize = 16.sp,
                    lineHeight = 22.sp
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        orderViewModel.placeOrder(cartItems, totalPrice)
                        viewModel.clearCart()
                        showPlaceOrderDialog = false
                        navigationManager.navigateToOrderList()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Confirm",
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showPlaceOrderDialog = false },
                    border = BorderStroke(1.dp, AppColors.Primary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AppColors.Primary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Cancel",
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            containerColor = AppColors.Surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun EmptyCartMessage(onStartShopping: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = null,
            tint = AppColors.Gray,
            modifier = Modifier.size(100.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Your cart is empty",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Add some cocktails to your cart and they will appear here",
            fontSize = 16.sp,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onStartShopping,
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.Primary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(48.dp)
        ) {
            Text(
                text = "Start Shopping",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
} 