import Foundation
import shared

class DependencyContainer {
    static let shared = DependencyContainer()
    
    private let koin: Koin
    
    private init() {
        // Initialize Koin
        koin = KoinKt.doInitKoin().koin
    }
    
    // MARK: - ViewModels
    
    func makeMainViewModel() -> MainViewModel {
        return koin.get(objCClass: MainViewModel.self) as! MainViewModel
    }
    
    func makeCocktailDetailViewModel() -> CocktailDetailViewModel {
        return koin.get(objCClass: CocktailDetailViewModel.self) as! CocktailDetailViewModel
    }
    
    func makeCartViewModel() -> CartViewModel {
        return koin.get(objCClass: CartViewModel.self) as! CartViewModel
    }
    
    func makeFavoritesViewModel() -> FavoritesViewModel {
        return koin.get(objCClass: FavoritesViewModel.self) as! FavoritesViewModel
    }
    
    func makeOrderViewModel() -> OrderViewModel {
        return koin.get(objCClass: OrderViewModel.self) as! OrderViewModel
    }
    
    func makeAuthViewModel() -> AuthViewModel {
        return koin.get(objCClass: AuthViewModel.self) as! AuthViewModel
    }
    
    func makeUserProfileViewModel() -> UserProfileViewModel {
        return koin.get(objCClass: UserProfileViewModel.self) as! UserProfileViewModel
    }
    
    // MARK: - Platform Components
    
    func makeNetworkMonitor() -> NetworkMonitor {
        return koin.get(objCClass: NetworkMonitor.self) as! NetworkMonitor
    }
    
    func makeSettingsWrapper() -> IosSettingsWrapper {
        return koin.get(objCClass: IosSettingsWrapper.self) as! IosSettingsWrapper
    }
    
    func makeNetworkClient() -> IosNetworkClient {
        return koin.get(objCClass: IosNetworkClient.self) as! IosNetworkClient
    }
}

// MARK: - View Model Extensions

extension MainViewModel: ObservableObject {}
extension CocktailDetailViewModel: ObservableObject {}
extension CartViewModel: ObservableObject {}
extension FavoritesViewModel: ObservableObject {}
extension OrderViewModel: ObservableObject {}
extension AuthViewModel: ObservableObject {}
extension UserProfileViewModel: ObservableObject {}