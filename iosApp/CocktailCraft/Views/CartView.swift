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
                    VStack(spacing: 20) {
                        // Cart Items
                        VStack(spacing: 16) {
                            ForEach(cartViewModel.cartItems, id: \.cocktail.id) { item in
                                CartItemCard(item: item, cartViewModel: cartViewModel)
                                    .padding(.horizontal, 16)
                            }
                        }
                        .padding(.top, 8)

                        // Order Summary Section
                        VStack(alignment: .leading, spacing: 16) {
                            Text("Order Summary")
                                .font(.title2)
                                .fontWeight(.bold)
                                .padding(.horizontal, 16)

                            VStack(spacing: 0) {
                                // Subtotal
                                HStack {
                                    Text("Subtotal")
                                        .foregroundColor(.secondary)
                                    Spacer()
                                    Text("$\(cartViewModel.totalPrice, specifier: "%.2f")")
                                        .fontWeight(.medium)
                                }
                                .padding(.horizontal, 16)
                                .padding(.vertical, 12)

                                Divider()
                                    .padding(.horizontal, 16)

                                // Delivery Fee
                                HStack {
                                    Text("Delivery Fee")
                                        .foregroundColor(.secondary)
                                    Spacer()
                                    Text("$\(cartViewModel.deliveryFee, specifier: "%.2f")")
                                        .fontWeight(.medium)
                                }
                                .padding(.horizontal, 16)
                                .padding(.vertical, 12)

                                Divider()
                                    .padding(.horizontal, 16)

                                // Total
                                HStack {
                                    Text("Total")
                                        .font(.headline)
                                        .fontWeight(.bold)
                                    Spacer()
                                    Text("$\(cartViewModel.finalTotal, specifier: "%.2f")")
                                        .font(.headline)
                                        .fontWeight(.bold)
                                        .foregroundColor(.orange)
                                }
                                .padding(.horizontal, 16)
                                .padding(.vertical, 12)
                            }
                            .background(Color(UIColor.systemBackground))
                            .cornerRadius(12)
                            .shadow(color: Color.black.opacity(0.1), radius: 4, y: 2)
                            .padding(.horizontal, 16)
                        }

                        // Place Order Button
                        Button(action: {
                            showCheckoutConfirmation = true
                        }) {
                            Text("Place Order")
                                .font(.headline)
                                .fontWeight(.semibold)
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 16)
                                .background(Color.orange)
                                .cornerRadius(12)
                        }
                        .padding(.horizontal, 16)
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

struct CartItemCard: View {
    let item: CocktailCartItem
    @ObservedObject var cartViewModel: CartViewModelSKIE

    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: 12) {
                // Cocktail Image
                AsyncImage(url: URL(string: item.cocktail.imageUrl ?? "")) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                } placeholder: {
                    ProgressView()
                }
                .frame(width: 80, height: 80)
                .clipShape(RoundedRectangle(cornerRadius: 12))

                // Content
                VStack(alignment: .leading, spacing: 8) {
                    // Name and Favorite
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(item.cocktail.name)
                                .font(.headline)
                                .fontWeight(.semibold)
                                .lineLimit(2)

                            Text(item.cocktail.category ?? "Unknown")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }

                        Spacer()

                        Button(action: {
                            // TODO: Add favorite functionality
                        }) {
                            Image(systemName: "heart")
                                .font(.title3)
                                .foregroundColor(.gray)
                        }
                        .buttonStyle(PlainButtonStyle())
                    }

                    // Price
                    Text("$\(item.cocktail.price, specifier: "%.2f")")
                        .font(.headline)
                        .fontWeight(.bold)
                        .foregroundColor(.orange)

                    Spacer()

                    // Quantity Controls and Total
                    HStack {
                        // Quantity Controls
                        HStack(spacing: 12) {
                            Button(action: {
                                Task {
                                    await cartViewModel.decrementQuantity(item.cocktail.id)
                                }
                            }) {
                                Image(systemName: "minus")
                                    .font(.system(size: 16, weight: .bold))
                                    .foregroundColor(.white)
                                    .frame(width: 32, height: 32)
                                    .background(Color.gray.opacity(0.6))
                                    .clipShape(Circle())
                            }
                            .buttonStyle(PlainButtonStyle())

                            Text("\(item.quantity)")
                                .font(.headline)
                                .fontWeight(.bold)
                                .frame(minWidth: 30)

                            Button(action: {
                                Task {
                                    await cartViewModel.incrementQuantity(item.cocktail.id)
                                }
                            }) {
                                Image(systemName: "plus")
                                    .font(.system(size: 16, weight: .bold))
                                    .foregroundColor(.white)
                                    .frame(width: 32, height: 32)
                                    .background(Color.orange)
                                    .clipShape(Circle())
                            }
                            .buttonStyle(PlainButtonStyle())
                        }

                        Spacer()

                        // Item Total and Delete
                        HStack(spacing: 8) {
                            Text("$\(item.cocktail.price * Double(item.quantity), specifier: "%.2f")")
                                .font(.headline)
                                .fontWeight(.bold)

                            Button(action: {
                                Task {
                                    await cartViewModel.removeFromCart(item.cocktail.id)
                                }
                            }) {
                                Image(systemName: "trash")
                                    .font(.system(size: 16))
                                    .foregroundColor(.red)
                            }
                            .buttonStyle(PlainButtonStyle())
                        }
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding(16)
        }
        .background(Color(UIColor.systemBackground))
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.1), radius: 4, y: 2)
    }
}