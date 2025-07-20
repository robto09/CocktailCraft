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

            // Create a collector to get the cocktails
            let collector = FlowValueCollector<NSArray>()
            collector.collect(from: flow)

            // Wait for the value to be collected
            await waitForCollectorCompletion(collector)

            if let error = collector.error {
                throw error
            }

            if let cocktailArray = collector.value as? [Cocktail] {
                self.favoriteCocktails = cocktailArray
            }
            self.isLoading = false
        } catch {
            await handleLoadingError(error)
        }
    }

    @MainActor
    private func waitForCollectorCompletion(_ collector: FlowValueCollector<NSArray>) async {
        var attempts = 0
        while collector.isLoading && attempts < 50 { // Wait up to 5 seconds
            try? await Task.sleep(nanoseconds: 100_000_000) // 0.1 seconds
            attempts += 1
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
            errorCode: ErrorCode.unknown
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
            errorCode: ErrorCode.unknown
        )
    }


}