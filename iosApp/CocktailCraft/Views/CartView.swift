import SwiftUI
import shared

struct CartView: View {
    @ObservedObject var cartViewModel: CartViewModelSKIE
    @StateObject private var orderViewModel = OrderViewModelSKIE()
    @Binding var selectedTab: Int
    @State private var showCheckoutConfirmation = false
    
    var body: some View {
        Group {
            if cartViewModel.cartItems.isEmpty {
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
                            ForEach(cartViewModel.cartItems, id: \.cocktail.id) { item in
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
                            subtotal: cartViewModel.totalPrice,
                            deliveryFee: cartViewModel.deliveryFee,
                            total: cartViewModel.finalTotal
                        )

                        // Place Order Button
                        CheckoutButton(
                            title: "Place Order",
                            isEnabled: !cartViewModel.cartItems.isEmpty,
                            isLoading: orderViewModel.isLoading,
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
                message: Text("Are you sure you want to place this order for $\(cartViewModel.finalTotal, specifier: "%.2f")?"),
                primaryButton: .default(Text("Confirm")) {
                    Task {
                        let success = await orderViewModel.placeOrder(cartItems: cartViewModel.cartItems, totalPrice: cartViewModel.finalTotal)
                        print("Order placement result: \(success)")
                        if success {
                            // Clear cart after successful order
                            await cartViewModel.clearCart()
                            // Post notification for order placed
                            NotificationCenter.default.post(name: NSNotification.Name("OrderPlaced"), object: nil)
                            // Small delay to ensure order is saved
                            try? await Task.sleep(nanoseconds: 500_000_000) // 0.5 seconds
                            // Navigate to Orders tab (tag 3) - ensure this runs on main thread
                            await MainActor.run {
                                print("Navigating to Orders tab")
                                selectedTab = 3
                            }
                        } else {
                            print("Order placement failed")
                        }
                    }
                },
                secondaryButton: .cancel()
            )
        }
    }
}

