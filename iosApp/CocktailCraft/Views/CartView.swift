import SwiftUI
import shared

struct CartView: View {
    @StateObject private var viewModel = CartViewModel()
    
    var body: some View {
        NavigationView {
            if viewModel.cartItems.isEmpty {
                EmptyStateView(
                    icon: "cart",
                    title: "Your cart is empty",
                    message: "Add some cocktails to get started"
                )
            } else {
                VStack {
                    List {
                        ForEach(viewModel.cartItems, id: \.cocktail.idDrink) { item in
                            CartItemRow(item: item, viewModel: viewModel)
                        }
                        .onDelete { indexSet in
                            indexSet.forEach { index in
                                let item = viewModel.cartItems[index]
                                viewModel.removeFromCart(cocktailId: item.cocktail.idDrink)
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
                            viewModel.checkout()
                        }) {
                            Text("Checkout")
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
            }
            .navigationTitle("Cart")
        }
    }
}

struct CartItemRow: View {
    let item: CartItem
    @ObservedObject var viewModel: CartViewModel
    
    var body: some View {
        HStack {
            // Cocktail Image
            AsyncImage(url: URL(string: item.cocktail.strDrinkThumb ?? "")) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                ProgressView()
            }
            .frame(width: 60, height: 60)
            .clipShape(RoundedRectangle(cornerRadius: 8))
            
            VStack(alignment: .leading, spacing: 4) {
                Text(item.cocktail.strDrink)
                    .font(.headline)
                    .lineLimit(1)
                
                Text("$\(item.price, specifier: "%.2f")")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
            
            Spacer()
            
            // Quantity Controls
            HStack(spacing: 12) {
                Button(action: {
                    viewModel.updateQuantity(
                        cocktailId: item.cocktail.idDrink,
                        quantity: max(1, item.quantity - 1)
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
                        cocktailId: item.cocktail.idDrink,
                        quantity: item.quantity + 1
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