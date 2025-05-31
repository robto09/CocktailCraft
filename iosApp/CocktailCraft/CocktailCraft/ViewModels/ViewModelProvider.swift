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
    
    private let koinHelper = KoinIOS.helper
    
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
    
    func favoritesViewModel() -> ObservableViewModel<FavoritesViewModel> {
        return ObservableViewModel(koinHelper.getFavoritesViewModel())
    }
    
    func profileViewModel() -> ObservableViewModel<ProfileViewModel> {
        return ObservableViewModel(koinHelper.getProfileViewModel())
    }
    
    func orderViewModel() -> ObservableViewModel<OrderViewModel> {
        return ObservableViewModel(koinHelper.getOrderViewModel())
    }
    
    func reviewViewModel() -> ObservableViewModel<ReviewViewModel> {
        return ObservableViewModel(koinHelper.getReviewViewModel())
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