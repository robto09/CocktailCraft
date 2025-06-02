//
//  ViewModelProvider.swift
//  CocktailCraft
//
//  Provides easy access to ViewModels throughout the iOS app
//  Updated to work with kmp-viewmodel
//

import SwiftUI
import shared

class ViewModelProvider {
    static let shared = ViewModelProvider()
    
    private let koinHelper = KoinHelper()
    
    private init() {}
    
    // Create observable wrappers for SwiftUI
    // These are now much smaller wrappers that just bridge StateFlow to SwiftUI
    lazy var homeViewModel = ObservableHomeViewModel(koinHelper.getHomeViewModel())
    lazy var cartViewModel = ObservableCartViewModel(koinHelper.getCartViewModel())
    lazy var themeViewModel = ObservableThemeViewModel(koinHelper.getThemeViewModel())
    lazy var offlineModeViewModel = ObservableOfflineModeViewModel(koinHelper.getOfflineModeViewModel())
    lazy var favoritesViewModel = ObservableFavoritesViewModel(koinHelper.getFavoritesViewModel())
    lazy var profileViewModel = ObservableProfileViewModel(koinHelper.getProfileViewModel())
    lazy var orderViewModel = ObservableOrderViewModel(koinHelper.getOrderViewModel())
    lazy var reviewViewModel = ObservableReviewViewModel(koinHelper.getReviewViewModel())
    
    // Factory method for ViewModels that need fresh instances
    func cocktailDetailViewModel() -> ObservableCocktailDetailViewModel {
        return ObservableCocktailDetailViewModel(koinHelper.getCocktailDetailViewModel())
    }
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