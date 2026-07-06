import SwiftUI
import shared

struct CartView: View {
    // Injected via .environment(cartViewModel) at the ContentView root
    @Environment(CartViewModelSKIE.self) private var cartViewModel
    @State private var orderViewModel = OrderViewModelSKIE()
    @Binding var selectedTab: Int
    @State private var showCheckoutConfirmation = false
    
    var body: some View {
        Group {
            if cartViewModel.state.cartItems.isEmpty {
                EmptyStateView(
                    icon: "cart",
                    title: "Your cart is empty",
                    message: "Add some cocktails to get started"
                )
                .navigationTitle("Cart")
            } else {
                ScrollView {
                    VStack(spacing: AppTheme.Spacing.xl) {
                        // Cart Items
                        VStack(spacing: AppTheme.Spacing.lg) {
                            ForEach(cartViewModel.state.cartItems, id: \.cocktail.id) { item in
                                CartItemCard(
                                    item: item,
                                    onIncrementQuantity: {
                                        Task {
                                            await cartViewModel.incrementQuantity(item.cocktail.id)
                                        }
                                    },
                                    onDecrementQuantity: {
                                        Task {
                                            await cartViewModel.decrementQuantity(item.cocktail.id)
                                        }
                                    },
                                    onRemoveFromCart: {
                                        Task {
                                            await cartViewModel.removeFromCart(item.cocktail.id)
                                        }
                                    },
                                    onToggleFavorite: nil
                                )
                                .padding(.horizontal, AppTheme.Spacing.lg)
                            }
                        }
                        .padding(.top, AppTheme.Spacing.sm)

                        // Order Summary
                        CartSummaryCard(
                            subtotal: cartViewModel.state.totalPrice,
                            deliveryFee: cartViewModel.deliveryFee,
                            total: cartViewModel.finalTotal
                        )

                        // Place Order Button
                        CheckoutButton(
                            title: "Place Order",
                            isEnabled: !cartViewModel.state.cartItems.isEmpty,
                            isLoading: orderViewModel.state.isLoading,
                            action: {
                                showCheckoutConfirmation = true
                            }
                        )
                        .padding(.bottom, 20)
                    }
                }
                .navigationTitle("Cart")
            }
        }
        .alert(isPresented: $showCheckoutConfirmation) {
            Alert(
                title: Text("Confirm Order"),
                message: Text("Are you sure you want to place this order for \(cartViewModel.finalTotal.asPrice)?"),
                primaryButton: .default(Text("Confirm")) {
                    Task {
                        // Order state propagates reactively from the shared
                        // orders flow — no notifications or delays needed.
                        let success = await orderViewModel.placeOrder(cartItems: cartViewModel.state.cartItems, totalPrice: cartViewModel.finalTotal)
                        if success {
                            await cartViewModel.clearCart()
                            selectedTab = 3
                        }
                    }
                },
                secondaryButton: .cancel()
            )
        }
        .sharedErrorAlert(cartViewModel.error, clear: { cartViewModel.clearError() })
        .sharedErrorAlert(orderViewModel.error, clear: { orderViewModel.clearError() })
    }
}

