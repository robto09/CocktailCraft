import SwiftUI
import shared

struct CartView: View {
    @ObservedObject var cartViewModel: CartViewModelSKIE
    @StateObject private var orderViewModel = OrderViewModelSKIE()
    @Binding var selectedTab: Int
    @State private var showCheckoutConfirmation = false
    
    var body: some View {
        NavigationView {
            if cartViewModel.cartItems.isEmpty {
                EmptyStateView(
                    icon: "cart",
                    title: "Your cart is empty",
                    message: "Add some cocktails to get started"
                )
                .navigationTitle("Cart")
            } else {
                VStack {
                    List {
                        ForEach(cartViewModel.cartItems, id: \.cocktail.id) { item in
                            CartItemRow(item: item, cartViewModel: cartViewModel)
                        }
                        .onDelete { indexSet in
                            indexSet.forEach { index in
                                let item = cartViewModel.cartItems[index]
                                Task {
                                    await cartViewModel.removeFromCart(item.cocktail.id)
                                }
                            }
                        }
                    }
                    
                    // Total and Checkout
                    VStack(spacing: 16) {
                        HStack {
                            Text("Total:")
                                .font(.headline)
                            Spacer()
                            Text("$\(cartViewModel.totalPrice, specifier: "%.2f")")
                                .font(.title2)
                                .fontWeight(.bold)
                        }
                        .padding(.horizontal)
                        
                        Button(action: {
                            showCheckoutConfirmation = true
                        }) {
                            Text("Place Order")
                                .font(.headline)
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(Color.blue)
                                .cornerRadius(10)
                        }
                        .padding(.horizontal)
                    }
                    .padding(.vertical)
                    .background(Color(UIColor.systemBackground))
                    .shadow(color: Color.black.opacity(0.1), radius: 5, y: -2)
                }
                .navigationTitle("Cart")
            }
        }
        .alert(isPresented: $showCheckoutConfirmation) {
            Alert(
                title: Text("Confirm Order"),
                message: Text("Are you sure you want to place this order for $\(cartViewModel.totalPrice + 5.99, specifier: "%.2f")?"),
                primaryButton: .default(Text("Confirm")) {
                    Task {
                        let success = await orderViewModel.placeOrder(cartItems: cartViewModel.cartItems, totalPrice: cartViewModel.totalPrice)
                        if success {
                            // Clear cart after successful order
                            await cartViewModel.clearCart()
                            // Navigate to Orders tab (tag 3)
                            selectedTab = 3
                        }
                    }
                },
                secondaryButton: .cancel()
            )
        }
    }
}

struct CartItemRow: View {
    let item: CocktailCartItem
    @ObservedObject var cartViewModel: CartViewModelSKIE
    
    var body: some View {
        HStack {
            // Cocktail Image
            AsyncImage(url: URL(string: item.cocktail.imageUrl ?? "")) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                ProgressView()
            }
            .frame(width: 60, height: 60)
            .clipShape(RoundedRectangle(cornerRadius: 8))
            
            VStack(alignment: .leading, spacing: 4) {
                Text(item.cocktail.name)
                    .font(.headline)
                    .lineLimit(1)
                
                Text("$\(item.cocktail.price, specifier: "%.2f")")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
            
            Spacer()
            
            // Quantity Controls
            HStack(spacing: 16) {
                Button(action: {
                    Task {
                        await cartViewModel.decrementQuantity(item.cocktail.id)
                    }
                }) {
                    Image(systemName: "minus.circle.fill")
                        .font(.title2)
                        .foregroundColor(.blue)
                        .frame(width: 44, height: 44)
                        .contentShape(Rectangle())
                }
                .buttonStyle(PlainButtonStyle())

                Text("\(item.quantity)")
                    .font(.headline)
                    .frame(minWidth: 40)

                Button(action: {
                    Task {
                        await cartViewModel.incrementQuantity(item.cocktail.id)
                    }
                }) {
                    Image(systemName: "plus.circle.fill")
                        .font(.title2)
                        .foregroundColor(.blue)
                        .frame(width: 44, height: 44)
                        .contentShape(Rectangle())
                }
                .buttonStyle(PlainButtonStyle())
            }
        }
        .padding(.vertical, 4)
    }
}