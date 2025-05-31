//
//  ObservableViewModel.swift
//  CocktailCraft
//
//  Helper class to make KMP ViewModels observable in SwiftUI
//

import SwiftUI
import shared
import Combine

/// Base class for wrapping KMP ViewModels to work with SwiftUI
class ObservableViewModel<T: BaseViewModel>: ObservableObject {
    let viewModel: T
    private var cancellables = Set<AnyCancellable>()
    
    init(_ viewModel: T) {
        self.viewModel = viewModel
    }
    
    deinit {
        viewModel.clear()
    }
    
    /// Helper to observe a StateFlow and publish changes to SwiftUI
    func observe<Value>(_ flow: any StateFlow, keyPath: ReferenceWritableKeyPath<ObservableViewModel<T>, Value>) {
        FlowCollector<Value>(flow: flow) { [weak self] value in
            DispatchQueue.main.async {
                self?[keyPath: keyPath] = value
            }
        }
    }
}

/// Collector for Kotlin StateFlow to Swift
class FlowCollector<T>: Kotlinx_coroutines_coreFlowCollector {
    let callback: (T) -> Void
    
    init(flow: any StateFlow, callback: @escaping (T) -> Void) {
        self.callback = callback
        flow.collect(collector: self, completionHandler: { error in
            print("Flow collection completed with error: \(String(describing: error))")
        })
    }
    
    func emit(value: Any?, completionHandler: @escaping (Error?) -> Void) {
        if let value = value as? T {
            callback(value)
        }
        completionHandler(nil)
    }
}

// MARK: - Specific ViewModel Wrappers

class ObservableHomeViewModel: ObservableViewModel<HomeViewModel> {
    @Published var cocktails: [Cocktail] = []
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var searchQuery = ""
    @Published var selectedSortOption: SortOption = .nameAsc
    @Published var isSearching = false
    @Published var searchFilters = SearchFilters()
    @Published var isAdvancedSearchActive = false
    @Published var hasMoreData = true
    @Published var isLoadingMore = false
    @Published var favorites: [Cocktail] = []
    
    override init(_ viewModel: HomeViewModel) {
        super.init(viewModel)
        
        // Observe StateFlows
        observe(viewModel.cocktails, keyPath: \.cocktails)
        observe(viewModel.isLoading, keyPath: \.isLoading)
        observe(viewModel.errorMessage, keyPath: \.errorMessage)
        observe(viewModel.searchQuery, keyPath: \.searchQuery)
        observe(viewModel.selectedSortOption, keyPath: \.selectedSortOption)
        observe(viewModel.isSearching, keyPath: \.isSearching)
        observe(viewModel.searchFilters, keyPath: \.searchFilters)
        observe(viewModel.isAdvancedSearchActive, keyPath: \.isAdvancedSearchActive)
        observe(viewModel.hasMoreData, keyPath: \.hasMoreData)
        observe(viewModel.isLoadingMore, keyPath: \.isLoadingMore)
        observe(viewModel.favorites, keyPath: \.favorites)
    }
    
    func loadCocktails() {
        viewModel.loadCocktails()
    }
    
    func searchCocktails(query: String) {
        viewModel.searchCocktails(query: query)
    }
    
    func changeSortOption(option: SortOption) {
        viewModel.changeSortOption(option: option)
    }
    
    func loadMore() {
        viewModel.loadMore()
    }
}

class ObservableCocktailDetailViewModel: ObservableViewModel<CocktailDetailViewModel> {
    @Published var cocktail: Cocktail?
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var isFavorite = false
    @Published var reviews: [Review] = []
    @Published var recommendations: [Cocktail] = []
    @Published var isLoadingRecommendations = false
    
    override init(_ viewModel: CocktailDetailViewModel) {
        super.init(viewModel)
        
        observe(viewModel.cocktail, keyPath: \.cocktail)
        observe(viewModel.isLoading, keyPath: \.isLoading)
        observe(viewModel.errorMessage, keyPath: \.errorMessage)
        observe(viewModel.isFavorite, keyPath: \.isFavorite)
        observe(viewModel.reviews, keyPath: \.reviews)
        observe(viewModel.recommendations, keyPath: \.recommendations)
        observe(viewModel.isLoadingRecommendations, keyPath: \.isLoadingRecommendations)
    }
    
    func loadCocktail(id: String) {
        viewModel.loadCocktail(id: id)
    }
    
    func toggleFavorite() {
        viewModel.toggleFavorite()
    }
    
    func addToCart(quantity: Int32) {
        viewModel.addToCart(quantity: quantity)
    }
    
    func setCurrentCocktail(_ cocktail: Cocktail) {
        viewModel.setCurrentCocktail(cocktail: cocktail)
    }
}

class ObservableCartViewModel: ObservableViewModel<CartViewModel> {
    @Published var cartItems: [CocktailCartItem] = []
    @Published var totalPrice: Double = 0.0
    @Published var isLoading = false
    
    override init(_ viewModel: CartViewModel) {
        super.init(viewModel)
        
        observe(viewModel.cartItems, keyPath: \.cartItems)
        observe(viewModel.totalPrice, keyPath: \.totalPrice)
        observe(viewModel.isLoading, keyPath: \.isLoading)
    }
    
    func updateQuantity(itemId: String, quantity: Int32) {
        viewModel.updateQuantity(itemId: itemId, quantity: quantity)
    }
    
    func removeItem(itemId: String) {
        viewModel.removeItem(itemId: itemId)
    }
    
    func clearCart() {
        viewModel.clearCart()
    }
}

class ObservableThemeViewModel: ObservableViewModel<ThemeViewModel> {
    @Published var isDarkMode = false
    
    override init(_ viewModel: ThemeViewModel) {
        super.init(viewModel)
        observe(viewModel.isDarkMode, keyPath: \.isDarkMode)
    }
    
    func toggleTheme() {
        viewModel.toggleTheme()
    }
}

class ObservableOfflineModeViewModel: ObservableViewModel<OfflineModeViewModel> {
    @Published var isOfflineModeEnabled = false
    @Published var isNetworkAvailable = true
    @Published var recentlyViewedCocktails: [Cocktail] = []
    
    var isOffline: Bool {
        return !isNetworkAvailable || isOfflineModeEnabled
    }
    
    override init(_ viewModel: OfflineModeViewModel) {
        super.init(viewModel)
        
        observe(viewModel.isOfflineModeEnabled, keyPath: \.isOfflineModeEnabled)
        observe(viewModel.isNetworkAvailable, keyPath: \.isNetworkAvailable)
        observe(viewModel.recentlyViewedCocktails, keyPath: \.recentlyViewedCocktails)
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