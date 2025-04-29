import Foundation
import Factory
import shared

// Main container holding all dependencies
extension Container {
    // ViewModels
    var mainViewModel: Factory<MainViewModel> {
        self { MainViewModel(homeViewModel: shared.HomeViewModel()) }
    }
    
    // Use Cases
    var getCocktailsUseCase: Factory<GetCocktailsUseCase> {
        self { shared.GetCocktailsUseCase() }
    }
    
    var searchCocktailsUseCase: Factory<SearchCocktailsUseCase> {
        self { shared.SearchCocktailsUseCase() }
    }
    
    var getFilterOptionsUseCase: Factory<GetFilterOptionsUseCase> {
        self { shared.GetFilterOptionsUseCase() }
    }
    
    // Repositories
    var cocktailRepository: Factory<CocktailRepository> {
        self { shared.CocktailRepositoryImpl() }
    }
}

// Helper for accessing dependencies
enum Dependencies {
    static let container = Container.shared
    
    static var mainViewModel: MainViewModel {
        container.mainViewModel()
    }
    
    static var getCocktailsUseCase: GetCocktailsUseCase {
        container.getCocktailsUseCase()
    }
    
    static var searchCocktailsUseCase: SearchCocktailsUseCase {
        container.searchCocktailsUseCase()
    }
    
    static var getFilterOptionsUseCase: GetFilterOptionsUseCase {
        container.getFilterOptionsUseCase()
    }
    
    static var cocktailRepository: CocktailRepository {
        container.cocktailRepository()
    }
}