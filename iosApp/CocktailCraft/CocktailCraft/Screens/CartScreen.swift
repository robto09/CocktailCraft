import SwiftUI
import shared

struct CartScreen: View {
    @ObservedObject private var cartViewModel = ViewModelProvider.shared.cartViewModel
    @ObservedObject private var orderViewModel = ViewModelProvider.shared.orderViewModel
    @ObservedObject private var favoritesViewModel = ViewModelProvider.shared.favoritesViewModel
    @EnvironmentObject var navigationCoordinator: NavigationCoordinator
    
    @State private var showPlaceOrderAlert = false
    
    private var currencyFormatter: NumberFormatter {
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.locale = Locale(identifier: "en_US")
        return formatter
    }
    
    var body: some View {
        NavigationView {
            ZStack {
                if cartViewModel.isLoading {
                    ProgressView("Loading cart...")
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if let error = cartViewModel.errorString {
                    EmptyStateView(
                        title: "Error",
                        message: error,
                        systemImage: "exclamationmark.triangle",
                        actionButtonText: "Try Again",
                        onActionButtonClick: {
                            // Cart doesn't have refresh, reload from parent view
                        }
                    )
                } else if cartViewModel.cartItems.isEmpty {
                    EmptyStateView(
                        title: "Your cart is empty",
                        message: "Add some cocktails to your cart and they will appear here",
                        systemImage: "cart",
                        actionButtonText: "Start Shopping",
                        onActionButtonClick: {
                            navigationCoordinator.selectedTab = .home
                        }
                    )
                } else {
                    VStack(spacing: 0) {
                        // Cart Items List
                        ScrollView {
                            LazyVStack(spacing: 12) {
                                ForEach(cartViewModel.cartItems, id: \.cocktail.id) { item in
                                    CartItemView(
                                        item: item,
                                        isFavorite: favoritesViewModel.favorites.contains { $0.id == item.cocktail.id },
                                        onIncreaseQuantity: {
                                            cartViewModel.updateQuantity(
                                                cocktailId: item.cocktail.id,
                                                quantity: item.quantity + 1
                                            )
                                        },
                                        onDecreaseQuantity: {
                                            if item.quantity > 1 {
                                                cartViewModel.updateQuantity(
                                                    cocktailId: item.cocktail.id,
                                                    quantity: item.quantity - 1
                                                )
                                            } else {
                                                cartViewModel.removeFromCart(cocktailId: item.cocktail.id)
                                            }
                                        },
                                        onRemove: {
                                            cartViewModel.removeFromCart(cocktailId: item.cocktail.id)
                                        },
                                        onToggleFavorite: {
                                            favoritesViewModel.toggleFavorite(cocktail: item.cocktail)
                                        }
                                    )
                                }
                            }
                            .padding()
                        }
                        
                        // Bottom Section with Order Summary and Checkout Button
                        VStack(spacing: 16) {
                            OrderSummaryView(
                                subtotal: cartViewModel.totalPrice,
                                deliveryFee: 5.99,
                                total: nil,
                                showDeliveryFee: true,
                                additionalItems: nil
                            )
                            
                            Button(action: {
                                showPlaceOrderAlert = true
                            }) {
                                Text("Place Order")
                                    .font(.system(size: 16, weight: .bold))
                                    .foregroundColor(.white)
                                    .frame(maxWidth: .infinity)
                                    .frame(height: 54)
                                    .background(Color.blue)
                                    .cornerRadius(12)
                            }
                        }
                        .padding()
                        .background(Color(UIColor.systemBackground))
                        .shadow(color: Color.black.opacity(0.05), radius: 8, x: 0, y: -2)
                    }
                }
            }
            .navigationTitle("Cart")
            .navigationBarTitleDisplayMode(.large)
            .confirmationAlert(
                isPresented: $showPlaceOrderAlert,
                title: "Confirm Order",
                message: "Are you sure you want to place this order for \(currencyFormatter.string(from: NSNumber(value: cartViewModel.totalPrice + 5.99)) ?? "$0.00")?",
                confirmButtonText: "Confirm",
                onConfirm: {
                    placeOrder()
                }
            )
        }
    }
    
    private func placeOrder() {
        orderViewModel.placeOrder(
            items: cartViewModel.cartItems,
            totalPrice: cartViewModel.totalPrice
        )
        cartViewModel.clearCart()
        // Navigate to orders tab
        navigationCoordinator.selectedTab = .orders
    }
}