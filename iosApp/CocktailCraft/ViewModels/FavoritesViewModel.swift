import Foundation
import shared
import Combine

@MainActor
class FavoritesViewModel: ObservableObject, IFavoritesViewModel {
    private let favoritesRepository: FavoritesRepository
    private let manageFavoritesUseCase: ManageFavoritesUseCase
    
    // MARK: - Published Properties
    @Published private(set) var favorites: [Cocktail] = []
    @Published private(set) var isLoading: Bool = false
    
    // MARK: - StateFlow Properties
    var favoritesFlow: any StateFlow<[Cocktail]> {
        StateFlowWrapper(publisher: $favorites.eraseToAnyPublisher())
    }
    
    var isLoadingFlow: any StateFlow<Bool> {
        StateFlowWrapper(publisher: $isLoading.eraseToAnyPublisher())
    }
    
    init() {
        let container = DependencyContainer.shared
        self.favoritesRepository = container.favoritesRepository
        self.manageFavoritesUseCase = container.manageFavoritesUseCase
        
        // Initial load
        Task {
            await loadFavorites()
        }
    }
    
    // MARK: - IFavoritesViewModel Implementation
    
    func loadFavorites() {
        Task {
            isLoading = true
            do {
                let result = try await favoritesRepository.getFavorites()
                if let favoritesResult = result as? Result.Success<[Cocktail]> {
                    favorites = favoritesResult.data
                }
            } catch {
                print("Error loading favorites: \(error)")
            }
            isLoading = false
        }
    }
    
    func addToFavorites(cocktail: Cocktail) {
        Task {
            do {
                try await manageFavoritesUseCase.addToFavorites(cocktail: cocktail)
                await loadFavorites()
            } catch {
                print("Error adding to favorites: \(error)")
            }
        }
    }
    
    func removeFromFavorites(cocktail: Cocktail) {
        Task {
            do {
                try await manageFavoritesUseCase.removeFromFavorites(cocktail: cocktail)
                await loadFavorites()
            } catch {
                print("Error removing from favorites: \(error)")
            }
        }
    }
    
    func toggleFavorite(cocktail: Cocktail) {
        Task {
            do {
                if isFavorite(id: cocktail.id) {
                    try await removeFromFavorites(cocktail: cocktail)
                } else {
                    try await addToFavorites(cocktail: cocktail)
                }
            } catch {
                print("Error toggling favorite: \(error)")
            }
        }
    }
    
    func isFavorite(id: String) -> Bool {
        favorites.contains { $0.id == id }
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