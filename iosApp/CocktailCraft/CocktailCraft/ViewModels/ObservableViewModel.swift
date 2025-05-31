//
//  ObservableViewModel.swift
//  CocktailCraft
//
//  Helper class to make KMP ViewModels observable in SwiftUI
//

import SwiftUI
import shared
import Combine

typealias StateFlow = Kotlinx_coroutines_coreStateFlow

/// Base class for wrapping KMP ViewModels to work with SwiftUI
class ObservableViewModel<T: BaseViewModel>: ObservableObject {
    let viewModel: T
    private var cancellables = Set<AnyCancellable>()
    private var collectors: [Any] = []
    
    init(_ viewModel: T) {
        self.viewModel = viewModel
    }
    
    deinit {
        viewModel.onCleared()
    }
    
    /// Helper to observe a StateFlow and publish changes to SwiftUI
    func observe<Value>(_ flow: any StateFlow, keyPath: ReferenceWritableKeyPath<ObservableViewModel<T>, Value>) {
        let collector = StateFlowCollector<Value>()
        collector.collect(flow: flow) { [weak self] value in
            DispatchQueue.main.async {
                if let typedValue = value as? Value {
                    self?[keyPath: keyPath] = typedValue
                }
            }
        }
        // Store collector to keep it alive
        self.collectors.append(collector)
    }
}


// MARK: - Specific ViewModel Wrappers

class ObservableHomeViewModel: ObservableObject {
    @Published var cocktails: [Cocktail] = []
    @Published var isLoading = false
    @Published var favorites: [Cocktail] = []
    @Published var errorString = ""
    @Published var isOfflineMode = false
    @Published var isNetworkAvailable = true
    @Published var searchFilters: SearchFilters?
    @Published var hasMoreData = true
    @Published var isLoadingMore = false
    
    let viewModel: HomeViewModel
    private var cancellables = Set<AnyCancellable>()
    private var collectors: [Any] = []
    
    init(_ viewModel: HomeViewModel) {
        self.viewModel = viewModel
        
        // Observe StateFlows
        observe(viewModel.cocktails) { [weak self] value in
            self?.cocktails = value
        }
        observe(viewModel.isLoading) { [weak self] value in
            self?.isLoading = value
        }
        observe(viewModel.favorites) { [weak self] value in
            self?.favorites = value
        }
        observe(viewModel.errorString) { [weak self] value in
            self?.errorString = value
        }
        observe(viewModel.isOfflineMode) { [weak self] value in
            self?.isOfflineMode = value
        }
        observe(viewModel.isNetworkAvailable) { [weak self] value in
            self?.isNetworkAvailable = value
        }
        observe(viewModel.searchFilters) { [weak self] value in
            self?.searchFilters = value
        }
        observe(viewModel.hasMoreData) { [weak self] value in
            self?.hasMoreData = value
        }
        observe(viewModel.isLoadingMore) { [weak self] value in
            self?.isLoadingMore = value
        }
    }
    
    deinit {
        viewModel.onCleared()
    }
    
    private func observe<Value>(_ flow: any StateFlow, handler: @escaping (Value) -> Void) {
        let collector = StateFlowCollector<Value>()
        collector.collect(flow: flow) { value in
            DispatchQueue.main.async {
                if let typedValue = value as? Value {
                    handler(typedValue)
                }
            }
        }
        // Store collector to keep it alive
        self.collectors.append(collector)
    }
    
    // Computed properties
    var errorMessage: String? {
        errorString.isEmpty ? nil : errorString
    }
    
    func loadCocktails() {
        viewModel.loadCocktails()
    }
    
    func searchCocktails(query: String) {
        viewModel.searchCocktails(query: query)
    }
    
    func loadMore() {
        viewModel.loadMoreCocktails()
    }
    
    func refresh() {
        // Refresh by reloading cocktails
        viewModel.loadCocktails()
    }
    
    func loadCocktailsByCategory(category: String) {
        viewModel.loadCocktailsByCategory(category: category)
    }
}

class ObservableCocktailDetailViewModel: ObservableObject {
    @Published var cocktail: Cocktail?
    @Published var isLoading = false
    @Published var isFavorite = false
    @Published var recommendations: [Cocktail] = []
    @Published var isLoadingRecommendations = false
    
    let viewModel: CocktailDetailViewModel
    private var cancellables = Set<AnyCancellable>()
    private var collectors: [Any] = []
    
    init(_ viewModel: CocktailDetailViewModel) {
        self.viewModel = viewModel
        
        observe(viewModel.cocktail) { [weak self] value in
            self?.cocktail = value
        }
        observe(viewModel.isLoading) { [weak self] value in
            self?.isLoading = value
        }
        observe(viewModel.isFavorite) { [weak self] value in
            self?.isFavorite = value
        }
        observe(viewModel.recommendations) { [weak self] value in
            self?.recommendations = value
        }
        observe(viewModel.isLoadingRecommendations) { [weak self] value in
            self?.isLoadingRecommendations = value
        }
    }
    
    deinit {
        viewModel.onCleared()
    }
    
    private func observe<Value>(_ flow: any StateFlow, handler: @escaping (Value) -> Void) {
        let collector = StateFlowCollector<Value>()
        collector.collect(flow: flow) { value in
            DispatchQueue.main.async {
                if let typedValue = value as? Value {
                    handler(typedValue)
                }
            }
        }
        // Store collector to keep it alive
        self.collectors.append(collector)
    }
    
    var errorMessage: String? {
        return nil // Will be updated when error handling is exposed
    }
    
    func loadCocktail(id: String) {
        viewModel.loadCocktail(id: id)
    }
    
    func toggleFavorite() {
        viewModel.toggleFavorite()
    }
}

class ObservableCartViewModel: ObservableObject {
    @Published var cartItems: [CocktailCartItem] = []
    @Published var totalPrice: Double = 0.0
    @Published var isLoading = false
    
    let viewModel: CartViewModel
    private var cancellables = Set<AnyCancellable>()
    private var collectors: [Any] = []
    
    init(_ viewModel: CartViewModel) {
        self.viewModel = viewModel
        
        observe(viewModel.cartItems) { [weak self] value in
            self?.cartItems = value
        }
        observe(viewModel.totalPrice) { [weak self] value in
            self?.totalPrice = value
        }
        observe(viewModel.isLoading) { [weak self] value in
            self?.isLoading = value
        }
    }
    
    deinit {
        viewModel.onCleared()
    }
    
    private func observe<Value>(_ flow: any StateFlow, handler: @escaping (Value) -> Void) {
        let collector = StateFlowCollector<Value>()
        collector.collect(flow: flow) { value in
            DispatchQueue.main.async {
                if let typedValue = value as? Value {
                    handler(typedValue)
                }
            }
        }
        // Store collector to keep it alive
        self.collectors.append(collector)
    }
    
    var error: String? {
        return nil // Will be updated when error handling is exposed
    }
    
    func updateQuantity(cocktailId: String, quantity: Int32) {
        viewModel.updateQuantity(cocktailId: cocktailId, quantity: quantity)
    }
    
    func removeFromCart(cocktailId: String) {
        viewModel.removeFromCart(cocktailId: cocktailId)
    }
    
    func clearCart() {
        viewModel.clearCart()
    }
    
    func refresh() {
        // Might need to be added to KMP ViewModel
    }
    
    func addToCart(cocktail: Cocktail, quantity: Int = 1) {
        viewModel.addToCart(cocktail: cocktail, quantity: Int32(quantity))
    }
}

class ObservableThemeViewModel: ObservableObject {
    @Published var isDarkMode = false
    
    let viewModel: ThemeViewModel
    private var cancellables = Set<AnyCancellable>()
    private var collectors: [Any] = []
    
    init(_ viewModel: ThemeViewModel) {
        self.viewModel = viewModel
        observe(viewModel.isDarkMode) { [weak self] value in
            self?.isDarkMode = value
        }
    }
    
    deinit {
        viewModel.onCleared()
    }
    
    private func observe<Value>(_ flow: any StateFlow, handler: @escaping (Value) -> Void) {
        let collector = StateFlowCollector<Value>()
        collector.collect(flow: flow) { value in
            DispatchQueue.main.async {
                if let typedValue = value as? Value {
                    handler(typedValue)
                }
            }
        }
        // Store collector to keep it alive
        self.collectors.append(collector)
    }
    
    func setDarkMode(enabled: Bool) {
        viewModel.setDarkMode(enabled: enabled)
    }
}

class ObservableFavoritesViewModel: ObservableObject {
    @Published var favorites: [Cocktail] = []
    @Published var isLoading = false
    
    let viewModel: FavoritesViewModel
    private var cancellables = Set<AnyCancellable>()
    private var collectors: [Any] = []
    
    init(_ viewModel: FavoritesViewModel) {
        self.viewModel = viewModel
        
        observe(viewModel.favorites) { [weak self] value in
            self?.favorites = value
        }
        observe(viewModel.isLoading) { [weak self] value in
            self?.isLoading = value
        }
    }
    
    deinit {
        viewModel.onCleared()
    }
    
    private func observe<Value>(_ flow: any StateFlow, handler: @escaping (Value) -> Void) {
        let collector = StateFlowCollector<Value>()
        collector.collect(flow: flow) { value in
            DispatchQueue.main.async {
                if let typedValue = value as? Value {
                    handler(typedValue)
                }
            }
        }
        // Store collector to keep it alive
        self.collectors.append(collector)
    }
    
    var error: String? {
        return nil // Will be updated when error handling is exposed
    }
    
    func toggleFavorite(cocktail: Cocktail) {
        viewModel.toggleFavorite(cocktail: cocktail)
    }
    
    func refresh() {
        viewModel.loadFavorites()
    }
}

class ObservableProfileViewModel: ObservableObject {
    @Published var user: User?
    @Published var isLoading = false
    @Published var isSignedIn = false
    
    let viewModel: ProfileViewModel
    private var cancellables = Set<AnyCancellable>()
    private var collectors: [Any] = []
    
    init(_ viewModel: ProfileViewModel) {
        self.viewModel = viewModel
        
        observe(viewModel.user) { [weak self] value in
            self?.user = value
        }
        observe(viewModel.isLoading) { [weak self] value in
            self?.isLoading = value
        }
        observe(viewModel.isSignedIn) { [weak self] value in
            self?.isSignedIn = value
        }
    }
    
    deinit {
        viewModel.onCleared()
    }
    
    private func observe<Value>(_ flow: any StateFlow, handler: @escaping (Value) -> Void) {
        let collector = StateFlowCollector<Value>()
        collector.collect(flow: flow) { value in
            DispatchQueue.main.async {
                if let typedValue = value as? Value {
                    handler(typedValue)
                }
            }
        }
        // Store collector to keep it alive
        self.collectors.append(collector)
    }
    
    func signIn(email: String, password: String) {
        viewModel.signIn(email: email, password: password)
    }
    
    func signOut() {
        viewModel.signOut()
    }
}

class ObservableOrderViewModel: ObservableObject {
    @Published var orders: [Order] = []
    @Published var isLoading = false
    
    let viewModel: OrderViewModel
    private var cancellables = Set<AnyCancellable>()
    private var collectors: [Any] = []
    
    init(_ viewModel: OrderViewModel) {
        self.viewModel = viewModel
        
        observe(viewModel.orders) { [weak self] value in
            self?.orders = value
        }
        observe(viewModel.isLoading) { [weak self] value in
            self?.isLoading = value
        }
    }
    
    deinit {
        viewModel.onCleared()
    }
    
    private func observe<Value>(_ flow: any StateFlow, handler: @escaping (Value) -> Void) {
        let collector = StateFlowCollector<Value>()
        collector.collect(flow: flow) { value in
            DispatchQueue.main.async {
                if let typedValue = value as? Value {
                    handler(typedValue)
                }
            }
        }
        // Store collector to keep it alive
        self.collectors.append(collector)
    }
    
    func placeOrder(items: [CocktailCartItem], totalPrice: Double) {
        viewModel.placeOrder(cartItems: items, totalPrice: totalPrice)
    }
    
    func loadOrders() {
        viewModel.loadOrders()
    }
    
    func refresh() {
        viewModel.loadOrders()
    }
}

class ObservableReviewViewModel: ObservableObject {
    @Published var reviews: [Review] = []
    @Published var isLoading = false
    
    let viewModel: ReviewViewModel
    private var cancellables = Set<AnyCancellable>()
    private var collectors: [Any] = []
    
    init(_ viewModel: ReviewViewModel) {
        self.viewModel = viewModel
        
        // ReviewViewModel might not expose all these properties yet
        // We'll add them as needed
    }
    
    deinit {
        viewModel.onCleared()
    }
    
    private func observe<Value>(_ flow: any StateFlow, handler: @escaping (Value) -> Void) {
        let collector = StateFlowCollector<Value>()
        collector.collect(flow: flow) { value in
            DispatchQueue.main.async {
                if let typedValue = value as? Value {
                    handler(typedValue)
                }
            }
        }
        // Store collector to keep it alive
        self.collectors.append(collector)
    }
    
    func addReview(review: Review) {
        viewModel.addReview(review: review)
    }
    
    func getReviewsForCocktail(cocktailId: String) -> [Review] {
        return viewModel.getReviewsForCocktail(cocktailId: cocktailId)
    }
}

class ObservableOfflineModeViewModel: ObservableObject {
    @Published var isOfflineModeEnabled = false
    @Published var isNetworkAvailable = true
    @Published var recentlyViewedCocktails: [Cocktail] = []
    
    let viewModel: OfflineModeViewModel
    private var cancellables = Set<AnyCancellable>()
    private var collectors: [Any] = []
    
    var isOffline: Bool {
        return !isNetworkAvailable || isOfflineModeEnabled
    }
    
    init(_ viewModel: OfflineModeViewModel) {
        self.viewModel = viewModel
        
        observe(viewModel.isOfflineModeEnabled) { [weak self] value in
            self?.isOfflineModeEnabled = value
        }
        observe(viewModel.isNetworkAvailable) { [weak self] value in
            self?.isNetworkAvailable = value
        }
        observe(viewModel.recentlyViewedCocktails) { [weak self] value in
            self?.recentlyViewedCocktails = value
        }
    }
    
    deinit {
        viewModel.onCleared()
    }
    
    private func observe<Value>(_ flow: any StateFlow, handler: @escaping (Value) -> Void) {
        let collector = StateFlowCollector<Value>()
        collector.collect(flow: flow) { value in
            DispatchQueue.main.async {
                if let typedValue = value as? Value {
                    handler(typedValue)
                }
            }
        }
        // Store collector to keep it alive
        self.collectors.append(collector)
    }
    
    func toggleOfflineMode() {
        viewModel.toggleOfflineMode()
    }
    
    func setOfflineMode(enabled: Bool) {
        viewModel.setOfflineMode(enabled: enabled)
    }
    
    func loadRecentlyViewedCocktails() {
        viewModel.loadRecentlyViewedCocktails()
    }
    
    func clearCache() {
        viewModel.clearCache()
    }
    
    func getCachedCocktailCount() -> Int32 {
        return viewModel.getCachedCocktailCount()
    }
}