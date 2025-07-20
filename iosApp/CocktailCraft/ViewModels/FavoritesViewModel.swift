import SwiftUI
@preconcurrency import shared

@MainActor
class FavoritesViewModel: ObservableObject {
    @Published var favoriteCocktails: [Cocktail] = []
    @Published var isLoading = false
    @Published var error: ErrorHandler.UserFriendlyError? = nil

    private let repository: CocktailRepository?

    init() {
        self.repository = KoinInitializer.shared.getCocktailRepository()
        loadFavorites()
    }

    func loadFavorites() {
        guard let repository = repository else {
            // If no repository, keep empty list
            favoriteCocktails = []
            return
        }

        isLoading = true
        error = nil

        Task { @MainActor in
            do {
                let flow = try await repository.getFavoriteCocktails()

                // Create a collector to get the cocktails
                let collector = FlowValueCollector<NSArray>()
                collector.collect(from: flow)

                // Wait for the value to be collected
                var attempts = 0
                while collector.isLoading && attempts < 50 { // Wait up to 5 seconds
                    try await Task.sleep(nanoseconds: 100_000_000) // 0.1 seconds
                    attempts += 1
                }

                if let error = collector.error {
                    throw error
                }

                if let cocktailArray = collector.value as? [Cocktail] {
                    self.favoriteCocktails = cocktailArray
                }
                self.isLoading = false
            } catch {
                self.isLoading = false
                self.error = ErrorHandler.shared.createUserFriendlyError(
                    title: "Loading Error",
                    message: "Failed to load favorites: \(error.localizedDescription)",
                    category: ErrorHandler.ErrorCategory.unknown,
                    recoveryAction: nil,
                    originalException: nil,
                    errorCode: ErrorCode.unknown
                )
            }
        }
    }

    func addFavorite(cocktail: Cocktail) {
        guard let repository = repository else {
            // Fallback to local storage if repository is not available
            if !favoriteCocktails.contains(where: { $0.id == cocktail.id }) {
                favoriteCocktails.append(cocktail)
            }
            return
        }

        Task { @MainActor in
            do {
                try await repository.addToFavorites(cocktail: cocktail)
                // Reload favorites to get updated list
                self.loadFavorites()
            } catch {
                self.error = ErrorHandler.shared.createUserFriendlyError(
                    title: "Add Favorite Error",
                    message: "Failed to add to favorites: \(error.localizedDescription)",
                    category: ErrorHandler.ErrorCategory.unknown,
                    recoveryAction: nil,
                    originalException: nil,
                    errorCode: ErrorCode.unknown
                )
            }
        }
    }

    func removeFavorite(cocktailId: String) {
        guard let repository = repository else {
            // Fallback to local storage if repository is not available
            favoriteCocktails.removeAll { $0.id == cocktailId }
            return
        }

        // Find the cocktail to remove
        guard let cocktail = favoriteCocktails.first(where: { $0.id == cocktailId }) else {
            return
        }

        Task { @MainActor in
            do {
                try await repository.removeFromFavorites(cocktail: cocktail)
                // Reload favorites to get updated list
                self.loadFavorites()
            } catch {
                self.error = ErrorHandler.shared.createUserFriendlyError(
                    title: "Remove Favorite Error",
                    message: "Failed to remove from favorites: \(error.localizedDescription)",
                    category: ErrorHandler.ErrorCategory.unknown,
                    recoveryAction: nil,
                    originalException: nil,
                    errorCode: ErrorCode.unknown
                )
            }
        }
    }

    func isFavorite(cocktailId: String) -> Bool {
        favoriteCocktails.contains { $0.id == cocktailId }
    }

    func retryLoadFavorites() {
        loadFavorites()
    }


}