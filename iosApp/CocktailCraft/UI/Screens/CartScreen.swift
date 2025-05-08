import SwiftUI
import shared

struct CartScreen: View {
    @StateObject private var viewModel = DependencyContainer.shared.makeCartViewModel()
    @State private var showingCheckout = false
    @State private var showingError = false
    @State private var errorMessage = ""
    
    var body: some View {
        ZStack {
            if viewModel.cartItems.isEmpty {
                emptyCartView
            } else {
                cartListView
            }
            
            if viewModel.isLoading {
                ProgressView()
                    .scaleEffect(1.5)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(Color.black.opacity(0.2))
            }
        }
        .navigationTitle("Cart")
        .alert("Error", isPresented: $showingError) {
            Button("OK") {}
        } message: {
            Text(errorMessage)
        }
        .sheet(isPresented: $showingCheckout) {
            CheckoutView(
                items: viewModel.cartItems,
                totalAmount: viewModel.totalAmount,
                onComplete: handleCheckoutComplete
            )
        }
        .onAppear {
            observeViewModel()
        }
    }
    
    private var emptyCartView: some View {
        VStack(spacing: 16) {
            Image(systemName: "cart")
                .font(.system(size: 64))
                .foregroundColor(.secondary)
            
            Text("Your cart is empty")
                .font(.title2)
                .foregroundColor(.secondary)
            
            NavigationLink(destination: HomeTab()) {
                Text("Browse Cocktails")
                    .font(.headline)
                    .foregroundColor(.white)
                    .padding()
                    .background(Color.blue)
                    .cornerRadius(10)
            }
        }
    }
    
    private var cartListView: some View {
        VStack {
            List {
                ForEach(viewModel.cartItems, id: \.cocktailId) { item in
                    CartItemRow(
                        item: item,
                        onQuantityChange: { quantity in
                            viewModel.updateQuantity(cocktailId: item.cocktailId, quantity: quantity)
                        },
                        onRemove: {
                            withAnimation {
                                viewModel.removeFromCart(cocktailId: item.cocktailId)
                            }
                        }
                    )
                }
            }
            
            VStack(spacing: 16) {
                HStack {
                    Text("Total")
                        .font(.headline)
                    Spacer()
                    Text("$\(String(format: "%.2f", viewModel.totalAmount))")
                        .font(.headline)
                }
                .padding(.horizontal)
                
                Button(action: {
                    showingCheckout = true
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
                .disabled(viewModel.isOffline)
            }
            .padding(.vertical)
            .background(Color(.systemBackground))
            .shadow(radius: 2)
        }
    }
    
    private func observeViewModel() {
        // Observe error state
        Task {
            for await error in viewModel.error {
                if let errorText = error {
                    errorMessage = errorText
                    showingError = true
                }
            }
        }
    }
    
    private func handleCheckoutComplete(success: Bool) {
        if success {
            viewModel.clearCart()
        }
        showingCheckout = false
    }
}

struct CartItemRow: View {
    let item: CocktailCartItem
    let onQuantityChange: (Int) -> Void
    let onRemove: () -> Void
    
    var body: some View {
        HStack(spacing: 12) {
            AsyncImage(url: URL(string: item.imageUrl ?? "")) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                Color.gray.opacity(0.2)
            }
            .frame(width: 60, height: 60)
            .cornerRadius(8)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(item.name)
                    .font(.headline)
                
                Text("$\(String(format: "%.2f", item.price))")
                    .foregroundColor(.secondary)
            }
            
            Spacer()
            
            HStack {
                Button(action: {
                    onQuantityChange(item.quantity - 1)
                }) {
                    Image(systemName: "minus.circle.fill")
                        .foregroundColor(.blue)
                }
                
                Text("\(item.quantity)")
                    .frame(width: 30)
                
                Button(action: {
                    onQuantityChange(item.quantity + 1)
                }) {
                    Image(systemName: "plus.circle.fill")
                        .foregroundColor(.blue)
                }
            }
        }
        .padding(.vertical, 8)
        .swipeActions(edge: .trailing) {
            Button(role: .destructive, action: onRemove) {
                Label("Delete", systemImage: "trash")
            }
        }
    }
}

// MARK: - Preview
struct CartScreen_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            CartScreen()
        }
    }
}