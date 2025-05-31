import SwiftUI
import shared

struct OrderListScreen: View {
    @ObservedObject private var orderViewModel = ViewModelProvider.shared.orderViewModel
    @EnvironmentObject var navigationCoordinator: NavigationCoordinator
    
    @State private var animatedIndices = Set<Int>()
    
    var body: some View {
        NavigationView {
            ZStack {
                if orderViewModel.isLoading {
                    ProgressView("Loading orders...")
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if let error = orderViewModel.error, !error.isEmpty {
                    VStack(spacing: 16) {
                        Text(error)
                            .font(.system(size: 16))
                            .foregroundColor(.red)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 32)
                        
                        Button(action: {
                            orderViewModel.refresh()
                        }) {
                            Text("Try Again")
                                .font(.system(size: 16, weight: .medium))
                                .foregroundColor(.white)
                                .padding(.horizontal, 24)
                                .padding(.vertical, 12)
                                .background(Color.blue)
                                .cornerRadius(8)
                        }
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if orderViewModel.orders.isEmpty {
                    EmptyStateView(
                        title: "No orders yet",
                        message: "Your order history will appear here",
                        systemImage: "list.bullet.rectangle",
                        actionButtonText: "Browse Cocktails",
                        onActionButtonClick: {
                            navigationCoordinator.selectedTab = .home
                        }
                    )
                } else {
                    ScrollView {
                        LazyVStack(spacing: 16) {
                            // Section Header
                            HStack {
                                Text("Your Orders")
                                    .font(.system(size: 20, weight: .bold))
                                Spacer()
                            }
                            .padding(.horizontal)
                            .padding(.top, 8)
                            
                            // Orders List
                            ForEach(Array(orderViewModel.orders.enumerated()), id: \.element.id) { index, order in
                                OrderItemView(order: order)
                                    .padding(.horizontal)
                                    .scaleEffect(animatedIndices.contains(index) ? 1 : 0.8)
                                    .opacity(animatedIndices.contains(index) ? 1 : 0)
                                    .onAppear {
                                        withAnimation(.easeOut(duration: 0.3).delay(Double(index) * 0.05)) {
                                            animatedIndices.insert(index)
                                        }
                                    }
                                    .onDisappear {
                                        animatedIndices.remove(index)
                                    }
                            }
                        }
                        .padding(.vertical)
                    }
                }
            }
            .navigationTitle("Order History")
            .navigationBarTitleDisplayMode(.large)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {
                        orderViewModel.refresh()
                    }) {
                        Image(systemName: "arrow.clockwise")
                            .font(.system(size: 16))
                    }
                }
            }
        }
    }
}