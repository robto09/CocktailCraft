import Foundation
import shared
import Combine

@MainActor
class CocktailDetailViewModel: ObservableObject, ICocktailDetailViewModel {
    private let cocktailRepository: CocktailRepository
    private let favoritesRepository: FavoritesRepository
    private let recommendationEngine: CocktailRecommendationEngine
    
    // MARK: - Published Properties
    @Published private(set) var cocktail: Cocktail?
    @Published private(set) var isFavorite: Bool = false
    @Published private(set) var recommendations: [Cocktail] = []
    @Published private(set) var isLoading: Bool = false
    
    // MARK: - StateFlow Properties
    var cocktailFlow: any StateFlow<Cocktail?> {
        StateFlowWrapper(publisher: $cocktail.eraseToAnyPublisher())
    }
    
    var isFavoriteFlow: any StateFlow<Bool> {
        StateFlowWrapper(publisher: $isFavorite.eraseToAnyPublisher())
    }
    
    var recommendationsFlow: any StateFlow<[Cocktail]> {
        StateFlowWrapper(publisher: $recommendations.eraseToAnyPublisher())
    }
    
    var isLoadingFlow: any StateFlow<Bool> {
        StateFlowWrapper(publisher: $isLoading.eraseToAnyPublisher())
    }
    
    init() {
        let container = DependencyContainer.shared
        self.cocktailRepository = container.cocktailRepository
        self.favoritesRepository = container.favoritesRepository
        self.recommendationEngine = container.recommendationEngine
    }
    
    // MARK: - ICocktailDetailViewModel Implementation
    
    func loadCocktailDetails(id: String) {
        Task {
            isLoading = true
            do {
                let result = try await cocktailRepository.getCocktailById(id: id).first()
                
                if let cocktailResult = result as? Result.Success<Cocktail> {
                    cocktail = cocktailResult.data
                    // Check if it's a favorite
                    isFavorite = try await favoritesRepository.isFavorite(id: id)
                    // Load recommendations
                    loadRecommendations(for: cocktailResult.data)
                }
            } catch {
                print("Error loading cocktail details: \(error)")
            }
            isLoading = false
        }
    }
    
    func toggleFavorite() {
        guard let currentCocktail = cocktail else { return }
        
        Task {
            do {
                try await favoritesRepository.toggleFavorite(cocktail: currentCocktail)
                isFavorite.toggle()
            } catch {
                print("Error toggling favorite: \(error)")
            }
        }
    }
    
    func loadRandomCocktail() {
        Task {
            isLoading = true
            do {
                let result = try await cocktailRepository.getRandomCocktail()
                if let cocktailResult = result as? Result.Success<Cocktail> {
                    cocktail = cocktailResult.data
                    // Check if it's a favorite
                    isFavorite = try await favoritesRepository.isFavorite(id: cocktailResult.data.id)
                    // Load recommendations
                    loadRecommendations(for: cocktailResult.data)
                }
            } catch {
                print("Error loading random cocktail: \(error)")
            }
            isLoading = false
        }
    }
    
    // MARK: - Private Helper Methods
    
    private func loadRecommendations(for cocktail: Cocktail) {
        Task {
            do {
                let recommendedCocktails = try await recommendationEngine.getRecommendations(
                    forCocktail: cocktail,
                    limit: 5
                )
                recommendations = recommendedCocktails
            } catch {
                print("Error loading recommendations: \(error)")
                recommendations = []
            }
        }
    }
}

// MARK: - StateFlow Wrapper
private class StateFlowWrapper<T>: StateFlow {
    private let publisher: AnyPublisher<T, Never>
    private var currentValue: T
    
    init(publisher: AnyPublisher<T, Never>, initialValue: T? = nil) {
        self.publisher = publisher
        self.currentValue = initialValue ?? publisher.value
    }
    
    var value: T {
        get { currentValue }
        set { currentValue = newValue }
    }
}