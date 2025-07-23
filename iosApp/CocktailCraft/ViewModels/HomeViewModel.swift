import SwiftUI
@preconcurrency import shared
import Combine

@MainActor
class HomeViewModel: ObservableObject {
    @Published var cocktails: [Cocktail] = []
    @Published var filteredCocktails: [Cocktail] = []
    @Published var isLoading = false
    @Published var error: ErrorHandler.UserFriendlyError? = nil
    @Published var selectedCategory: String? = nil
    @Published var selectedIngredient: String? = nil
    @Published var sortOption: SortOption = SortOption.nameAsc
    
    private let repository: CocktailRepository?
    private var cancellables = Set<AnyCancellable>()

    init() {
        self.repository = KoinInitializer.shared.getCocktailRepository()
        print("HomeViewModel init - repository: \(repository != nil ? "available" : "nil")")
        // Load data from repository
        loadCocktails()
    }
    
    func loadCocktails() {
        print("HomeViewModel - loadCocktails() called")
        isLoading = true
        error = nil

        guard let repository = repository else {
            print("HomeViewModel - No repository, showing error")
            // Show error if repository is not available
            self.error = ErrorHandler.shared.createUserFriendlyError(
                title: "Service Unavailable",
                message: "Unable to connect to cocktail service.",
                category: ErrorHandler.ErrorCategory.unknown,
                recoveryAction: nil,
                originalException: nil,
                errorCode: .unknown
            )
            self.isLoading = false
            return
        }

        Task { @MainActor in
            do {
                print("HomeViewModel - Repository available, loading via SKIE AsyncSequence")
                let kotlinFlow = try await repository.getCocktailsSortedByNewest()
                print("HomeViewModel - Got kotlinFlow type: \(type(of: kotlinFlow))")

                // SKIE converts StateFlow to AsyncSequence - cast to proper type
                if let asyncFlow = kotlinFlow as? any AsyncSequence {
                    print("HomeViewModel - Successfully cast to AsyncSequence")
                    for try await cocktailArray in asyncFlow {
                        await MainActor.run {
                            if let cocktails = cocktailArray as? [Cocktail] {
                                self.cocktails = cocktails
                                self.filteredCocktails = cocktails
                                print("HomeViewModel - Loaded \(cocktails.count) cocktails via SKIE")
                            }
                            self.isLoading = false
                        }
                        break // Take first emission
                    }
                } else {
                    print("HomeViewModel - AsyncSequence cast failed, flow type: \(type(of: kotlinFlow))")
                    await MainActor.run {
                        self.isLoading = false
                        self.error = ErrorHandler.shared.createUserFriendlyError(
                            title: "Data Loading Error",
                            message: "Unable to load cocktails. AsyncSequence conversion failed.",
                            category: ErrorHandler.ErrorCategory.unknown,
                            recoveryAction: nil,
                            originalException: nil,
                            errorCode: .unknown
                        )
                    }
                }
            } catch {
                await handleLoadingError(error)
            }
        }
    }
    
    @MainActor
    private func handleLoadingError(_ error: Error) async {
        print("HomeViewModel - Error loading cocktails: \(error)")
        self.isLoading = false
        self.error = ErrorHandler.shared.createUserFriendlyError(
            title: "Loading Error",
            message: "Failed to load cocktails: \(error.localizedDescription)",
            category: ErrorHandler.ErrorCategory.unknown,
            recoveryAction: nil,
            originalException: nil,
            errorCode: .unknown
        )
    }


    
    private func getSampleCocktails() -> [Cocktail] {
        // Sample cocktails for testing
        return [
            Cocktail(
                id: "11007",
                name: "Margarita",
                alternateName: nil,
                tags: ["IBA", "Contemporary Classic"],
                category: "Ordinary Drink",
                iba: "Contemporary Classic",
                alcoholic: "Alcoholic",
                glass: "Cocktail glass",
                instructions: "Rub the rim of the glass with the lime slice to make the salt stick to it. Take care to moisten only the outer rim and sprinkle the salt on it. The salt should present to the lips of the imbiber and never mix into the cocktail. Shake the other ingredients with ice, then carefully pour into the glass.",
                imageUrl: "https://www.thecocktaildb.com/images/media/drink/5noda61589575158.jpg",
                ingredients: [
                    CocktailIngredient(name: "Tequila", measure: "1 1/2 oz"),
                    CocktailIngredient(name: "Triple sec", measure: "1/2 oz"),
                    CocktailIngredient(name: "Lime juice", measure: "1 oz"),
                    CocktailIngredient(name: "Salt", measure: "")
                ],
                imageSource: "https://commons.wikimedia.org/wiki/File:Klassiche_Margarita.jpg",
                imageAttribution: "Cocktailmarler",
                creativeCommonsConfirmed: true,
                dateModified: "2015-08-18 14:42:59",
                price: 12.99,
                inStock: true,
                stockCount: 50,
                rating: 4.5,
                popularity: 95,
                dateAdded: Int64(Date().timeIntervalSince1970 * 1000)
            ),
            Cocktail(
                id: "11118",
                name: "Blue Margarita",
                alternateName: nil,
                tags: nil,
                category: "Ordinary Drink",
                iba: nil,
                alcoholic: "Alcoholic",
                glass: "Cocktail glass",
                instructions: "Rub rim of cocktail glass with lime juice. Dip rim in coarse salt. Shake tequila, blue curacao, and lime juice with ice, strain into the salt-rimmed glass, and serve.",
                imageUrl: "https://www.thecocktaildb.com/images/media/drink/bry4qh1582751040.jpg",
                ingredients: [
                    CocktailIngredient(name: "Tequila", measure: "1 1/2 oz"),
                    CocktailIngredient(name: "Blue Curacao", measure: "1 oz"),
                    CocktailIngredient(name: "Lime juice", measure: "1 oz"),
                    CocktailIngredient(name: "Salt", measure: "Coarse")
                ],
                imageSource: nil,
                imageAttribution: nil,
                creativeCommonsConfirmed: true,
                dateModified: "2015-08-18 14:51:53",
                price: 13.99,
                inStock: true,
                stockCount: 45,
                rating: 4.2,
                popularity: 78,
                dateAdded: Int64(Date().timeIntervalSince1970 * 1000)
            )
        ]
    }
    
    func searchCocktails(query: String) {
        guard let repository = repository else {
            // Fallback to local filtering if repository is not available
            if query.isEmpty {
                filteredCocktails = cocktails
            } else {
                filteredCocktails = cocktails.filter { cocktail in
                    cocktail.name.lowercased().contains(query.lowercased())
                }
            }
            applySorting()
            return
        }

        if query.isEmpty {
            // If query is empty, reload all cocktails
            loadCocktails()
            return
        }

        isLoading = true
        error = nil

        Task { @MainActor in
            await searchCocktailsAsync(query: query)
        }
    }

    @MainActor
    private func searchCocktailsAsync(query: String) async {
        guard let repository = repository else {
            self.isLoading = false
            return
        }

        do {
            // Use SKIE AsyncSequence pattern
            let kotlinFlow = try await repository.searchCocktailsByName(name: query)
            print("HomeViewModel - Search got kotlinFlow type: \(type(of: kotlinFlow))")

            // SKIE converts StateFlow to AsyncSequence - cast to proper type
            if let asyncFlow = kotlinFlow as? any AsyncSequence {
                print("HomeViewModel - Successfully cast search to AsyncSequence")
                for try await cocktailArray in asyncFlow {
                    await MainActor.run {
                        if let cocktails = cocktailArray as? [Cocktail] {
                            self.filteredCocktails = cocktails
                            print("HomeViewModel - Found \(cocktails.count) cocktails via SKIE search")
                            self.applySorting()
                        }
                        self.isLoading = false
                    }
                    break // Take first emission
                }
            } else {
                print("HomeViewModel - AsyncSequence cast failed for search, flow type: \(type(of: kotlinFlow))")
                await MainActor.run {
                    self.isLoading = false
                    self.error = ErrorHandler.shared.createUserFriendlyError(
                        title: "Search Error",
                        message: "Unable to search cocktails. AsyncSequence conversion failed.",
                        category: ErrorHandler.ErrorCategory.unknown,
                        recoveryAction: nil,
                        originalException: nil,
                        errorCode: .unknown
                    )
                }
            }
        } catch {
            await handleSearchError(error)
        }
    }

    @MainActor
    private func handleSearchError(_ error: Error) async {
        self.isLoading = false
        self.error = ErrorHandler.shared.createUserFriendlyError(
            title: "Search Error",
            message: "Failed to search cocktails: \(error.localizedDescription)",
            category: ErrorHandler.ErrorCategory.unknown,
            recoveryAction: nil,
            originalException: nil,
            errorCode: .unknown
        )
    }
    
    func applyFilters() {
        var filtered = cocktails

        if let category = selectedCategory {
            filtered = filtered.filter { $0.category == category }
        }

        if let ingredient = selectedIngredient {
            filtered = filtered.filter { cocktail in
                // Check if any ingredient matches
                cocktail.ingredients.contains { $0.name == ingredient }
            }
        }

        filteredCocktails = filtered
        applySorting()
    }
    
    func applySorting() {
        switch sortOption {
        case SortOption.nameAsc:
            filteredCocktails.sort { $0.name < $1.name }
        case SortOption.nameDesc:
            filteredCocktails.sort { $0.name > $1.name }
        default:
            break
        }
    }
    
    func retryLoadCocktails() {
        loadCocktails()
    }
    
    @MainActor
    func refreshCocktails() async {
        // Simulate refresh
        try? await Task.sleep(nanoseconds: 1_000_000_000) // 1 second
        loadCocktails()
    }
}

