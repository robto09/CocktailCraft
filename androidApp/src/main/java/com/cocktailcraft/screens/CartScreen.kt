package com.cocktailcraft.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.ui.theme.AppColors
import com.cocktailcraft.viewmodel.CartViewModel
import com.cocktailcraft.viewmodel.OrderViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: CartViewModel,
    orderViewModel: OrderViewModel,
    navController: NavController,
    onStartShopping: () -> Unit
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val totalPrice by viewModel.totalPrice.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

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
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        onRemove = { viewModel.removeFromCart(item.cocktail.id) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Order Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Subtotal",
                            color = AppColors.TextSecondary
                        )
                        Text(
                            text = currencyFormatter.format(totalPrice),
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Delivery Fee",
                            color = AppColors.TextSecondary
                        )
                        Text(
                            text = currencyFormatter.format(5.99),
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(thickness = 1.dp, color = AppColors.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = AppColors.TextPrimary
                        )
                        Text(
                            text = currencyFormatter.format(totalPrice + 5.99),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
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
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Primary
                ),
                shape = RoundedCornerShape(8.dp)
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
            title = { Text("Confirm Order") },
            text = { Text("Are you sure you want to place this order for ${currencyFormatter.format(totalPrice + 5.99)}?") },
            confirmButton = {
                Button(
                    onClick = {
                        orderViewModel.placeOrder(cartItems, totalPrice)
                        viewModel.clearCart()
                        showPlaceOrderDialog = false
                        navController.navigate("orders")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showPlaceOrderDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EmptyCartMessage(onStartShopping: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = null,
            tint = AppColors.Gray,
            modifier = Modifier.size(120.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Your cart is empty",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Add some cocktails to your cart and they will appear here",
            fontSize = 16.sp,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onStartShopping,
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.Primary
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Start Shopping",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun CartItemCard(
    item: CocktailCartItem,
    onIncreaseQuantity: () -> Unit,
    onDecreaseQuantity: () -> Unit,
    onRemove: () -> Unit
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image
            AsyncImage(
                model = item.cocktail.imageUrl,
                contentDescription = item.cocktail.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            // Product Details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = item.cocktail.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AppColors.TextPrimary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = currencyFormatter.format(item.cocktail.price),
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Primary,
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Quantity Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDecreaseQuantity,
                        modifier = Modifier
                            .size(24.dp)
                            .background(AppColors.LightGray, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease",
                            tint = AppColors.TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    Text(
                        text = item.quantity.toString(),
                        modifier = Modifier.padding(horizontal = 12.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    
                    IconButton(
                        onClick = onIncreaseQuantity,
                        modifier = Modifier
                            .size(24.dp)
                            .background(AppColors.Primary, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            // Price and Remove
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = currencyFormatter.format(item.cocktail.price * item.quantity),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AppColors.TextPrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = AppColors.Error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
} 