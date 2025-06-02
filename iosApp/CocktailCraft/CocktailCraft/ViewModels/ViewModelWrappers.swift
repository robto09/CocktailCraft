//
//  ViewModelWrappers.swift
//  CocktailCraft
//
//  Minimal wrappers for KMP ViewModels to work with SwiftUI
//  Using kmp-viewmodel library
//

import SwiftUI
import shared
import Combine

// MARK: - Home ViewModel
@MainActor
class ObservableHomeViewModel: ObservableObject {
    private let viewModel: HomeViewModel
    private var collectors: [Any] = []
    
    @Published var cocktails: [Cocktail] = []
    @Published var isLoading = false
    @Published var favorites: [Cocktail] = []
    @Published var errorString = ""
    @Published var isOfflineMode = false
    @Published var isNetworkAvailable = true
    @Published var searchFilters: SearchFilters?
    @Published var hasMoreData = true
    @Published var isLoadingMore = false
    
    init(_ viewModel: HomeViewModel) {
        self.viewModel = viewModel
        setupObservers()
    }
    
    private func setupObservers() {
        observe(viewModel.cocktails) { [weak self] in self?.cocktails = $0 }
        observe(viewModel.isLoading) { [weak self] in self?.isLoading = $0 }
        observe(viewModel.favorites) { [weak self] in self?.favorites = $0 }
        observe(viewModel.errorString) { [weak self] in self?.errorString = $0 }
        observe(viewModel.isOfflineMode) { [weak self] in self?.isOfflineMode = $0 }
        observe(viewModel.isNetworkAvailable) { [weak self] in self?.isNetworkAvailable = $0 }
        observe(viewModel.searchFilters) { [weak self] in self?.searchFilters = $0 }
        observe(viewModel.hasMoreData) { [weak self] in self?.hasMoreData = $0 }
        observe(viewModel.isLoadingMore) { [weak self] in self?.isLoadingMore = $0 }
    }
    
    private func observe<Value>(_ flow: any StateFlow, handler: @escaping (Value) -> Void) {
        let collector = StateFlowCollector<Value>()
        collector.collect(flow: flow) { value in
            Task { @MainActor in
                if let typedValue = value as? Value {
                    handler(typedValue)
                }
            }
        }
        collectors.append(collector)
    }
    
    // Delegate methods to ViewModel
    func loadCocktails() { viewModel.loadCocktails() }
    func searchCocktails(query: String) { viewModel.searchCocktails(query: query) }
    func loadMore() { viewModel.loadMoreCocktails() }
    func refresh() { viewModel.loadCocktails() }
    func loadCocktailsByCategory(category: String) { viewModel.loadCocktailsByCategory(category: category) }
}

// MARK: - Cart ViewModel
@MainActor
class ObservableCartViewModel: ObservableObject {
    private let viewModel: CartViewModel
    private var collectors: [Any] = []
    
    @Published var cartItems: [CocktailCartItem] = []
    @Published var totalPrice: Double = 0.0
    @Published var isLoading = false
    
    init(_ viewModel: CartViewModel) {
        self.viewModel = viewModel
        setupObservers()
    }
    
    private func setupObservers() {
        observe(viewModel.cartItems) { [weak self] in self?.cartItems = $0 }
        observe(viewModel.totalPrice) { [weak self] in self?.totalPrice = $0 }
        observe(viewModel.isLoading) { [weak self] in self?.isLoading = $0 }
    }
    
    private func observe<Value>(_ flow: any StateFlow, handler: @escaping (Value) -> Void) {
        let collector = StateFlowCollector<Value>()
        collector.collect(flow: flow) { value in
            Task { @MainActor in
                if let typedValue = value as? Value {
                    handler(typedValue)
                }
            }
        }
        collectors.append(collector)
    }
    
    func updateQuantity(cocktailId: String, quantity: Int32) {
        viewModel.updateQuantity(cocktailId: cocktailId, quantity: quantity)
    }
    
    func removeFromCart(cocktailId: String) {
        viewModel.removeFromCart(cocktailId: cocktailId)
    }
    
    func clearCart() { viewModel.clearCart() }
    func addToCart(cocktail: Cocktail, quantity: Int = 1) {
        viewModel.addToCart(cocktail: cocktail, quantity: Int32(quantity))
    }
}

// MARK: - Theme ViewModel
@MainActor
class ObservableThemeViewModel: ObservableObject {
    private let viewModel: ThemeViewModel
    private var collectors: [Any] = []
    
    @Published var isDarkMode = false
    
    init(_ viewModel: ThemeViewModel) {
        self.viewModel = viewModel
        setupObservers()
    }
    
    private func setupObservers() {
        observe(viewModel.isDarkMode) { [weak self] in self?.isDarkMode = $0 }
    }
    
    private func observe<Value>(_ flow: any StateFlow, handler: @escaping (Value) -> Void) {
        let collector = StateFlowCollector<Value>()
        collector.collect(flow: flow) { value in
            Task { @MainActor in
                if let typedValue = value as? Value {
                    handler(typedValue)
                }
            }
        }
        collectors.append(collector)
    }
    
    func setDarkMode(enabled: Bool) {
        viewModel.setDarkMode(enabled: enabled)
    }
}

// MARK: - Cocktail Detail ViewModel
@MainActor
class ObservableCocktailDetailViewModel: ObservableObject {
    private let viewModel: CocktailDetailViewModel
    private var collectors: [Any] = []
    
    @Published var cocktail: Cocktail?
    @Published var isLoading = false
    @Published var isFavorite = false
    @Published var recommendations: [Cocktail] = []
    @Published var isLoadingRecommendations = false
    
    init(_ viewModel: CocktailDetailViewModel) {
        self.viewModel = viewModel
        setupObservers()
    }
    
    private func setupObservers() {
        observe(viewModel.cocktail) { [weak self] in self?.cocktail = $0 }
        observe(viewModel.isLoading) { [weak self] in self?.isLoading = $0 }
        observe(viewModel.isFavorite) { [weak self] in self?.isFavorite = $0 }
        observe(viewModel.recommendations) { [weak self] in self?.recommendations = $0 }
        observe(viewModel.isLoadingRecommendations) { [weak self] in self?.isLoadingRecommendations = $0 }
    }
    
    private func observe<Value>(_ flow: any StateFlow, handler: @escaping (Value) -> Void) {
        let collector = StateFlowCollector<Value>()
        collector.collect(flow: flow) { value in
            Task { @MainActor in
                if let typedValue = value as? Value {
                    handler(typedValue)
                }
            }
        }
        collectors.append(collector)
    }
    
    func loadCocktail(id: String) { viewModel.loadCocktail(id: id) }
    func toggleFavorite() { viewModel.toggleFavorite() }
}

// MARK: - Favorites ViewModel
@MainActor
class ObservableFavoritesViewModel: ObservableObject {
    private let viewModel: FavoritesViewModel
    private var collectors: [Any] = []
    
    @Published var favorites: [Cocktail] = []
    @Published var isLoading = false
    
    init(_ viewModel: FavoritesViewModel) {
        self.viewModel = viewModel
        setupObservers()
    }
    
    private func setupObservers() {
        observe(viewModel.favorites) { [weak self] in self?.favorites = $0 }
        observe(viewModel.isLoading) { [weak self] in self?.isLoading = $0 }
    }
    
    private func observe<Value>(_ flow: any StateFlow, handler: @escaping (Value) -> Void) {
        let collector = StateFlowCollector<Value>()
        collector.collect(flow: flow) { value in
            Task { @MainActor in
                if let typedValue = value as? Value {
                    handler(typedValue)
                }
            }
        }
        collectors.append(collector)
    }
    
    func toggleFavorite(cocktail: Cocktail) { viewModel.toggleFavorite(cocktail: cocktail) }
    func refresh() { viewModel.loadFavorites() }
}

// MARK: - Profile ViewModel
@MainActor
class ObservableProfileViewModel: ObservableObject {
    private let viewModel: ProfileViewModel
    private var collectors: [Any] = []
    
    @Published var user: User?
    @Published var isLoading = false
    @Published var isSignedIn = false
    
    init(_ viewModel: ProfileViewModel) {
        self.viewModel = viewModel
        setupObservers()
    }
    
    private func setupObservers() {
        observe(viewModel.user) { [weak self] in self?.user = $0 }
        observe(viewModel.isLoading) { [weak self] in self?.isLoading = $0 }
        observe(viewModel.isSignedIn) { [weak self] in self?.isSignedIn = $0 }
    }
    
    private func observe<Value>(_ flow: any StateFlow, handler: @escaping (Value) -> Void) {
        let collector = StateFlowCollector<Value>()
        collector.collect(flow: flow) { value in
            Task { @MainActor in
                if let typedValue = value as? Value {
                    handler(typedValue)
                }
            }
        }
        collectors.append(collector)
    }
    
    func signIn(email: String, password: String) { viewModel.signIn(email: email, password: password) }
    func signOut() { viewModel.signOut() }
}

// MARK: - Order ViewModel
@MainActor
class ObservableOrderViewModel: ObservableObject {
    private let viewModel: OrderViewModel
    private var collectors: [Any] = []
    
    @Published var orders: [Order] = []
    @Published var isLoading = false
    
    init(_ viewModel: OrderViewModel) {
        self.viewModel = viewModel
        setupObservers()
    }
    
    private func setupObservers() {
        observe(viewModel.orders) { [weak self] in self?.orders = $0 }
        observe(viewModel.isLoading) { [weak self] in self?.isLoading = $0 }
    }
    
    private func observe<Value>(_ flow: any StateFlow, handler: @escaping (Value) -> Void) {
        let collector = StateFlowCollector<Value>()
        collector.collect(flow: flow) { value in
            Task { @MainActor in
                if let typedValue = value as? Value {
                    handler(typedValue)
                }
            }
        }
        collectors.append(collector)
    }
    
    func placeOrder(items: [CocktailCartItem], totalPrice: Double) {
        viewModel.placeOrder(cartItems: items, totalPrice: totalPrice)
    }
    func loadOrders() { viewModel.loadOrders() }
    func refresh() { viewModel.loadOrders() }
}

// MARK: - Review ViewModel
@MainActor
class ObservableReviewViewModel: ObservableObject {
    private let viewModel: ReviewViewModel
    private var collectors: [Any] = []
    
    @Published var reviews: [Review] = []
    @Published var isLoading = false
    
    init(_ viewModel: ReviewViewModel) {
        self.viewModel = viewModel
        // ReviewViewModel might not expose all these properties yet
        // We'll add observers as needed
    }
    
    func addReview(review: Review) { viewModel.addReview(review: review) }
    func getReviewsForCocktail(cocktailId: String) -> [Review] {
        return viewModel.getReviewsForCocktail(cocktailId: cocktailId)
    }
}

// MARK: - Offline Mode ViewModel
@MainActor
class ObservableOfflineModeViewModel: ObservableObject {
    private let viewModel: OfflineModeViewModel
    private var collectors: [Any] = []
    
    @Published var isOfflineModeEnabled = false
    @Published var isNetworkAvailable = true
    @Published var recentlyViewedCocktails: [Cocktail] = []
    
    var isOffline: Bool {
        return !isNetworkAvailable || isOfflineModeEnabled
    }
    
    init(_ viewModel: OfflineModeViewModel) {
        self.viewModel = viewModel
        setupObservers()
    }
    
    private func setupObservers() {
        observe(viewModel.isOfflineModeEnabled) { [weak self] in self?.isOfflineModeEnabled = $0 }
        observe(viewModel.isNetworkAvailable) { [weak self] in self?.isNetworkAvailable = $0 }
        observe(viewModel.recentlyViewedCocktails) { [weak self] in self?.recentlyViewedCocktails = $0 }
    }
    
    private func observe<Value>(_ flow: any StateFlow, handler: @escaping (Value) -> Void) {
        let collector = StateFlowCollector<Value>()
        collector.collect(flow: flow) { value in
            Task { @MainActor in
                if let typedValue = value as? Value {
                    handler(typedValue)
                }
            }
        }
        collectors.append(collector)
    }
    
    func toggleOfflineMode() { viewModel.toggleOfflineMode() }
    func setOfflineMode(enabled: Bool) { viewModel.setOfflineMode(enabled: enabled) }
    func loadRecentlyViewedCocktails() { viewModel.loadRecentlyViewedCocktails() }
    func clearCache() { viewModel.clearCache() }
    func getCachedCocktailCount() -> Int32 { return viewModel.getCachedCocktailCount() }
}