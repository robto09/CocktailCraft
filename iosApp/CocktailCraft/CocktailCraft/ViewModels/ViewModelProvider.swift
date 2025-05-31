//
//  ViewModelProvider.swift
//  CocktailCraft
//
//  Provides easy access to ViewModels throughout the iOS app
//

import SwiftUI
import shared

class ViewModelProvider {
    static let shared = ViewModelProvider()
    
    private let koinHelper = KoinHelper()
    
    private init() {}
    
    // Lazy initialization to ensure ViewModels are created only when needed
    lazy var homeViewModel = ObservableHomeViewModel(koinHelper.getHomeViewModel())
    lazy var cartViewModel = ObservableCartViewModel(koinHelper.getCartViewModel())
    lazy var themeViewModel = ObservableThemeViewModel(koinHelper.getThemeViewModel())
    lazy var offlineModeViewModel = ObservableOfflineModeViewModel(koinHelper.getOfflineModeViewModel())
    
    // Factory methods for ViewModels that need fresh instances
    func cocktailDetailViewModel() -> ObservableCocktailDetailViewModel {
        return ObservableCocktailDetailViewModel(koinHelper.getCocktailDetailViewModel())
    }
    
    lazy var favoritesViewModel = ObservableFavoritesViewModel(koinHelper.getFavoritesViewModel())
    lazy var profileViewModel = ObservableProfileViewModel(koinHelper.getProfileViewModel())
    lazy var orderViewModel = ObservableOrderViewModel(koinHelper.getOrderViewModel())
    lazy var reviewViewModel = ObservableReviewViewModel(koinHelper.getReviewViewModel())
}

// MARK: - Environment Values for easy access in SwiftUI

struct ViewModelProviderKey: EnvironmentKey {
    static let defaultValue = ViewModelProvider.shared
}

extension EnvironmentValues {
    var viewModelProvider: ViewModelProvider {
        get { self[ViewModelProviderKey.self] }
        set { self[ViewModelProviderKey.self] = newValue }
    }
}