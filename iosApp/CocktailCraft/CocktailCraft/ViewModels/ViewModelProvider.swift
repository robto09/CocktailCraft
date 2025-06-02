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
    @MainActor
    lazy var homeViewModel = ObservableHomeViewModel(koinHelper.getHomeViewModel())
    @MainActor
    lazy var cartViewModel = ObservableCartViewModel(koinHelper.getCartViewModel())
    @MainActor
    lazy var themeViewModel = ObservableThemeViewModel(koinHelper.getThemeViewModel())
    @MainActor
    lazy var offlineModeViewModel = ObservableOfflineModeViewModel(koinHelper.getOfflineModeViewModel())
    @MainActor
    lazy var favoritesViewModel = ObservableFavoritesViewModel(koinHelper.getFavoritesViewModel())
    @MainActor
    lazy var profileViewModel = ObservableProfileViewModel(koinHelper.getProfileViewModel())
    @MainActor
    lazy var orderViewModel = ObservableOrderViewModel(koinHelper.getOrderViewModel())
    @MainActor
    lazy var reviewViewModel = ObservableReviewViewModel(koinHelper.getReviewViewModel())
    
    // Factory method for ViewModels that need fresh instances
    @MainActor
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