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
        // Load real data if repository is available, otherwise use mock data
        if repository != nil {
            loadCocktails()
        } else {
            print("HomeViewModel - No repository, loading mock data")
            loadMockData()
        }
    }
    
    func loadCocktails() {
        print("HomeViewModel - loadCocktails() called")
        isLoading = true
        error = nil

        guard let repository = repository else {
            print("HomeViewModel - No repository, loading mock data")
            // Fallback to mock data if repository is not available
            loadMockData()
            return
        }

        print("HomeViewModel - Repository available, loading real data")

        Task {
            await loadCocktailsAsync()
        }
    }

    @MainActor
    private func loadCocktailsAsync() async {
        guard let repository = repository else {
            self.isLoading = false
            return
        }

        do {
            print("HomeViewModel - Loading all cocktails with SKIE AsyncSequence")

            // Use simple FlowCollector approach
            let kotlinFlow = try await repository.getCocktailsSortedByNewest()

            // Create a simple collector
            let collector = FlowCollector<NSArray> { cocktailArray in
                DispatchQueue.main.async {
                    if let cocktails = cocktailArray as? [Cocktail] {
                        print("HomeViewModel - Got \(cocktails.count) cocktails")
                        self.cocktails = cocktails
                        self.filteredCocktails = cocktails
                        self.applySorting()
                    }
                    self.isLoading = false
                }
            }

            try await kotlinFlow.collect(collector: collector)

        } catch {
            await handleLoadingError(error)
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

        Task {
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
            // Use simple FlowCollector approach
            let kotlinFlow = try await repository.searchCocktailsByName(name: query)

            // Create a simple collector
            let collector = FlowCollector<NSArray> { cocktailArray in
                DispatchQueue.main.async {
                    if let cocktails = cocktailArray as? [Cocktail] {
                        self.filteredCocktails = cocktails
                        self.applySorting()
                    }
                    self.isLoading = false
                }
            }

            try await kotlinFlow.collect(collector: collector)
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

    // MARK: - Mock Data (Temporary)
    private func loadMockData() {
        // Create some mock cocktails for testing
        let mockIngredient1 = CocktailIngredient(name: "Vodka", measure: "2 oz")
        let mockIngredient2 = CocktailIngredient(name: "Orange Juice", measure: "4 oz")

        let mockCocktail = Cocktail(
            id: "1",
            name: "Screwdriver",
            alternateName: nil,
            tags: nil,
            category: "Ordinary Drink",
            iba: nil,
            alcoholic: "Alcoholic",
            glass: "Highball glass",
            instructions: "Mix vodka and orange juice in a glass with ice.",
            imageUrl: "https://www.thecocktaildb.com/images/media/drink/8xnyke1504352207.jpg",
            ingredients: [mockIngredient1, mockIngredient2],
            imageSource: nil,
            imageAttribution: nil,
            creativeCommonsConfirmed: nil,
            dateModified: nil,
            price: 8.50,
            inStock: true,
            stockCount: 10,
            rating: 4.5,
            popularity: 85,
            dateAdded: 1672531200000 // 2023-01-01 as timestamp
        )

        self.cocktails = [mockCocktail]
        self.filteredCocktails = [mockCocktail]
    }
}

