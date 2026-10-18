import SwiftUI
import shared

struct CartView: View {
    // Injected via .environment(cartViewModel) at the ContentView root
    @Environment(CartViewModelSKIE.self) private var cartViewModel
    // Built once via ViewModelHolder, not on every parent re-render (IO-13)
    @State private var orderModel = ViewModelHolder(OrderViewModelSKIE.init)
    // Cart rows carry a favorite heart, as on Android
    @State private var favoritesModel = ViewModelHolder(FavoritesViewModelSKIE.init)
    private var orderViewModel: OrderViewModelSKIE { orderModel.wrapper }
    private var favoritesViewModel: FavoritesViewModelSKIE { favoritesModel.wrapper }
    @Environment(AppRouter.self) private var router
    @State private var showCheckoutConfirmation = false
    @Environment(\.isDarkMode) private var isDarkMode
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    
    var body: some View {
        Group {
            if cartViewModel.state.cartItems.isEmpty {
                // "Start Shopping" goes to Home, as on Android
                EmptyStateView(
                    icon: "cart",
                    title: L(\.cart_empty_title),
                    message: L(\.cart_empty_message_ios),
                    actionTitle: L(\.cart_start_shopping),
                    action: { router.selectedTab = .home }
                )
                .brandedNavigationBar(title: L(\.cart))
            } else {
                let favoriteIds = Set(favoritesViewModel.state.favorites.map(\.id))
                ScrollView {
                    VStack(spacing: AppTheme.Spacing.xl) {
                        // Cart Items
                        VStack(spacing: AppTheme.Spacing.lg) {
                            ForEach(cartViewModel.state.cartItems, id: \.cocktail.id) { item in
                                CartItemCard(
                                    item: item,
                                    isFavorite: favoriteIds.contains(item.cocktail.id),
                                    onIncrementQuantity: {
                                        cartViewModel.incrementQuantity(item.cocktail.id)
                                    },
                                    onDecrementQuantity: {
                                        cartViewModel.decrementQuantity(item.cocktail.id)
                                    },
                                    onRemoveFromCart: {
                                        cartViewModel.removeFromCart(item.cocktail.id)
                                    },
                                    onToggleFavorite: {
                                        favoritesViewModel.toggleFavorite(item.cocktail)
                                    }
                                )
                                .padding(.horizontal, AppTheme.Spacing.lg)
                            }
                        }
                        .padding(.top, AppTheme.Spacing.sm)

                        if scrollCheckoutWithItems {
                            checkoutSection
                                .padding(.bottom, 20)
                        }
                    }
                    .padding(.bottom, AppTheme.Spacing.lg)
                }
                // Summary + Place Order pinned above the tab bar (as on
                // Android), except at accessibility sizes, where pinning
                // would leave almost no room for the items.
                .safeAreaInset(edge: .bottom) {
                    if !scrollCheckoutWithItems {
                        checkoutSection
                            .padding(.top, AppTheme.Spacing.md)
                            .padding(.bottom, AppTheme.Spacing.sm)
                            .background(AppColors.background(isDarkMode: isDarkMode))
                    }
                }
                .brandedNavigationBar(title: L(\.cart))
            }
        }
        // App background (not the system black/white) behind every state
        .background(AppColors.background(isDarkMode: isDarkMode).ignoresSafeArea())
        .alert(isPresented: $showCheckoutConfirmation) {
            Alert(
                title: Text(L(\.confirm_order_title)),
                message: Text(L(\.confirm_order_message, cartViewModel.state.finalTotal.formatted())),
                primaryButton: .default(Text(L(\.cart_confirm))) {
                    Task {
                        // Order and cart state propagate reactively from the
                        // shared flows — no notifications or delays needed.
                        // placeOrder empties the cart in the same transaction
                        // as the order insert, so there is no clearCart here.
                        let success = await orderViewModel.placeOrder(cartItems: cartViewModel.state.cartItems)
                        if success {
                            router.selectedTab = .orders
                        }
                    }
                },
                secondaryButton: .cancel()
            )
        }
        // "Removed … from cart" with Undo, after a removal or a decrement to
        // zero here: the cart's events are this screen's own (add-to-cart
        // returns its outcome to Home and Favorites instead)
        .sharedEventToast(cartViewModel.event, clear: { cartViewModel.clearEvent() })
        .sharedErrorAlert(cartViewModel.error, clear: { cartViewModel.clearError() })
        .sharedErrorAlert(orderViewModel.error, clear: { orderViewModel.clearError() })
    }

    private var scrollCheckoutWithItems: Bool {
        dynamicTypeSize.isAccessibilitySize
    }

    private var checkoutSection: some View {
        VStack(spacing: AppTheme.Spacing.lg) {
            // Order Summary
            CartSummaryCard(
                subtotal: cartViewModel.state.totalPrice,
                deliveryFee: cartViewModel.state.deliveryFee,
                total: cartViewModel.state.finalTotal
            )

            // Place Order Button
            CheckoutButton(
                title: L(\.cart_place_order),
                isEnabled: !cartViewModel.state.cartItems.isEmpty,
                isLoading: orderViewModel.state.isLoading,
                action: {
                    showCheckoutConfirmation = true
                }
            )
        }
    }
}

