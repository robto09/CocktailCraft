import SwiftUI
import shared

struct CartView: View {
    // Injected via .environment(cartViewModel) at the ContentView root
    @Environment(CartViewModelSKIE.self) private var cartViewModel
    @State private var orderViewModel = OrderViewModelSKIE()
    @Environment(AppRouter.self) private var router
    @State private var showCheckoutConfirmation = false
    
    var body: some View {
        Group {
            if cartViewModel.state.cartItems.isEmpty {
                EmptyStateView(
                    icon: "cart",
                    title: L(\.cart_empty_title),
                    message: "Add some cocktails to get started"
                )
                .navigationTitle("Cart")
                .brandedNavigationBar()
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
                            deliveryFee: cartViewModel.state.deliveryFee,
                            total: cartViewModel.state.finalTotal
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
                .brandedNavigationBar()
            }
        }
        .alert(isPresented: $showCheckoutConfirmation) {
            Alert(
                title: Text("Confirm Order"),
                message: Text(L(\.confirm_order_message, cartViewModel.state.finalTotal.asPrice)),
                primaryButton: .default(Text("Confirm")) {
                    Task {
                        // Order and cart state propagate reactively from the
                        // shared flows — no notifications or delays needed.
                        // placeOrder empties the cart in the same transaction
                        // as the order insert, so there is no clearCart here.
                        let success = await orderViewModel.placeOrder(cartItems: cartViewModel.state.cartItems, totalPrice: cartViewModel.state.finalTotal)
                        if success {
                            router.selectedTab = .orders
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

