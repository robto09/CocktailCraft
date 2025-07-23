import SwiftUI
import shared
import Kingfisher

struct CartView: View {
    @ObservedObject private var viewModel = CartViewModel.instance
    @Binding var selectedTab: Int
    @State private var showCheckoutConfirmation = false
    
    var body: some View {
        NavigationView {
            if viewModel.cartItems.isEmpty {
                EmptyStateView(
                    icon: "cart",
                    title: "Your cart is empty",
                    message: "Add some cocktails to get started"
                )
                .navigationTitle("Cart")
            } else {
                VStack {
                    List {
                        ForEach(viewModel.cartItems, id: \.cocktail.id) { item in
                            CartItemRow(item: item, viewModel: viewModel)
                        }
                        .onDelete { indexSet in
                            indexSet.forEach { index in
                                let item = viewModel.cartItems[index]
                                viewModel.removeFromCart(cocktailId: item.cocktail.id)
                            }
                        }
                    }
                    
                    // Total and Checkout
                    VStack(spacing: 16) {
                        HStack {
                            Text("Total:")
                                .font(.headline)
                            Spacer()
                            Text("$\(viewModel.totalPrice, specifier: "%.2f")")
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
                message: Text("Are you sure you want to place this order for $\(viewModel.totalPrice + 5.99, specifier: "%.2f")?"),
                primaryButton: .default(Text("Confirm")) {
                    viewModel.checkout { success in
                        if success {
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
    @ObservedObject var viewModel: CartViewModel
    
    var body: some View {
        HStack {
            // Cocktail Image
            KFImage(URL(string: item.cocktail.imageUrl ?? ""))
                .placeholder {
                    ProgressView()
                }
                .resizable()
                .aspectRatio(contentMode: ContentMode.fill)
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
            HStack(spacing: 12) {
                Button(action: {
                    viewModel.updateQuantity(
                        cocktailId: item.cocktail.id,
                        quantity: max(1, Int(item.quantity) - 1)
                    )
                }) {
                    Image(systemName: "minus.circle.fill")
                        .foregroundColor(.blue)
                }
                
                Text("\(item.quantity)")
                    .font(.headline)
                    .frame(minWidth: 30)
                
                Button(action: {
                    viewModel.updateQuantity(
                        cocktailId: item.cocktail.id,
                        quantity: Int(item.quantity) + 1
                    )
                }) {
                    Image(systemName: "plus.circle.fill")
                        .foregroundColor(.blue)
                }
            }
        }
        .padding(.vertical, 4)
    }
}