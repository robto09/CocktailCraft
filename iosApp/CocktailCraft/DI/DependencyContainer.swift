import Foundation
import shared

class DependencyContainer {
    static let shared = DependencyContainer()
    private let koin = KoinKt.doInitKoin().koin
    
    // Repositories
    lazy var cocktailRepository: CocktailRepository = {
        koin.get(objCClass: CocktailRepository.self) as! CocktailRepository
    }()
    
    lazy var cartRepository: CartRepository = {
        koin.get(objCClass: CartRepository.self) as! CartRepository
    }()
    
    lazy var favoritesRepository: FavoritesRepository = {
        koin.get(objCClass: FavoritesRepository.self) as! FavoritesRepository
    }()
    
    lazy var orderRepository: OrderRepository = {
        koin.get(objCClass: OrderRepository.self) as! OrderRepository
    }()
    
    lazy var authRepository: AuthRepository = {
        koin.get(objCClass: AuthRepository.self) as! AuthRepository
    }()
    
    // Use Cases
    lazy var getCocktailsUseCase: GetCocktailsUseCase = {
        koin.get(objCClass: GetCocktailsUseCase.self) as! GetCocktailsUseCase
    }()
    
    lazy var searchCocktailsUseCase: SearchCocktailsUseCase = {
        koin.get(objCClass: SearchCocktailsUseCase.self) as! SearchCocktailsUseCase
    }()
    
    lazy var manageFavoritesUseCase: ManageFavoritesUseCase = {
        koin.get(objCClass: ManageFavoritesUseCase.self) as! ManageFavoritesUseCase
    }()
    
    lazy var manageCartUseCase: ManageCartUseCase = {
        koin.get(objCClass: ManageCartUseCase.self) as! ManageCartUseCase
    }()
    
    lazy var placeOrderUseCase: PlaceOrderUseCase = {
        koin.get(objCClass: PlaceOrderUseCase.self) as! PlaceOrderUseCase
    }()
    
    // ViewModels factory methods
    func makeMainViewModel() -> MainViewModel {
        MainViewModel()
    }
    
    // Private init to ensure singleton
    private init() {}
}

// View extension for dependency injection
extension View {
    func inject<T>(_ keyPath: KeyPath<DependencyContainer, T>) -> T {
        DependencyContainer.shared[keyPath: keyPath]
    }
}