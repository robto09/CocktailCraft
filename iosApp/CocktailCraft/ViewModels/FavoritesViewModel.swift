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

        Task {
            await loadFavoritesAsync()
        }
    }

    @MainActor
    private func loadFavoritesAsync() async {
        guard let repository = repository else {
            self.isLoading = false
            return
        }

        do {
            let flow = try await repository.getFavoriteCocktails()

            // Use simple collector for repository flows
            let collector = SimpleFlowCollector<NSArray> { [weak self] cocktailArray in
                DispatchQueue.main.async {
                    if let cocktails = cocktailArray as? [Cocktail] {
                        self?.favoriteCocktails = cocktails
                    }
                    self?.isLoading = false
                }
            }

            try await flow.collect(collector: collector)
        } catch {
            await handleLoadingError(error)
        }
    }



    @MainActor
    private func handleLoadingError(_ error: Error) async {
        self.isLoading = false
        self.error = ErrorHandler.shared.createUserFriendlyError(
            title: "Loading Error",
            message: "Failed to load favorites: \(error.localizedDescription)",
            category: ErrorHandler.ErrorCategory.unknown,
            recoveryAction: nil,
            originalException: nil,
            errorCode: .unknown
        )
    }

    func addFavorite(cocktail: Cocktail) {
        guard repository != nil else {
            // Fallback to local storage if repository is not available
            if !favoriteCocktails.contains(where: { $0.id == cocktail.id }) {
                favoriteCocktails.append(cocktail)
            }
            return
        }

        Task {
            await addFavoriteAsync(cocktail: cocktail)
        }
    }

    func removeFavorite(cocktailId: String) {
        guard repository != nil else {
            // Fallback to local storage if repository is not available
            favoriteCocktails.removeAll { $0.id == cocktailId }
            return
        }

        // Find the cocktail to remove
        guard let cocktail = favoriteCocktails.first(where: { $0.id == cocktailId }) else {
            return
        }

        Task {
            await removeFavoriteAsync(cocktail: cocktail)
        }
    }

    func isFavorite(cocktailId: String) -> Bool {
        favoriteCocktails.contains { $0.id == cocktailId }
    }

    func retryLoadFavorites() {
        loadFavorites()
    }

    @MainActor
    func refreshFavorites() async {
        // Simulate refresh
        try? await Task.sleep(nanoseconds: 500_000_000) // 0.5 seconds
        loadFavorites()
    }

    @MainActor
    private func addFavoriteAsync(cocktail: Cocktail) async {
        guard let repository = repository else { return }

        do {
            try await repository.addToFavorites(cocktail: cocktail)
            // Reload favorites to get updated list
            self.loadFavorites()
        } catch {
            await handleFavoriteError(error, action: "add")
        }
    }

    @MainActor
    private func removeFavoriteAsync(cocktail: Cocktail) async {
        guard let repository = repository else { return }

        do {
            try await repository.removeFromFavorites(cocktail: cocktail)
            // Reload favorites to get updated list
            self.loadFavorites()
        } catch {
            await handleFavoriteError(error, action: "remove")
        }
    }

    @MainActor
    private func handleFavoriteError(_ error: Error, action: String) async {
        self.error = ErrorHandler.shared.createUserFriendlyError(
            title: "\(action.capitalized) Favorite Error",
            message: "Failed to \(action) favorite: \(error.localizedDescription)",
            category: ErrorHandler.ErrorCategory.unknown,
            recoveryAction: nil,
            originalException: nil,
            errorCode: .unknown
        )
    }
}

