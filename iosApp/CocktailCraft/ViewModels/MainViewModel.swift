import Foundation
import shared
import Combine

@MainActor
class MainViewModel: ObservableObject, IHomeViewModel {
    private let cocktailRepository: CocktailRepository
    private let searchUseCase: SearchCocktailsUseCase
    private let networkUseCase: NetworkStatusUseCase
    
    // MARK: - Published Properties (SwiftUI State)
    @Published private(set) var cocktails: [Cocktail] = []
    @Published private(set) var hasMoreData: Bool = true
    @Published private(set) var isLoadingMore: Bool = false
    @Published private(set) var searchQuery: String = ""
    @Published private(set) var isSearchActive: Bool = false
    @Published private(set) var searchFilters = SearchFilters()
    @Published private(set) var isAdvancedSearchActive: Bool = false
    @Published private(set) var isOfflineMode: Bool = false
    @Published private(set) var isNetworkAvailable: Bool = true
    @Published private(set) var isLoading: Bool = false
    
    // MARK: - StateFlow Properties (Shared KMP Interface)
    var cocktailsFlow: any StateFlow<[Cocktail]> {
        StateFlowWrapper(publisher: $cocktails.eraseToAnyPublisher())
    }
    
    var hasMoreDataFlow: any StateFlow<Bool> {
        StateFlowWrapper(publisher: $hasMoreData.eraseToAnyPublisher())
    }
    
    var isLoadingMoreFlow: any StateFlow<Bool> {
        StateFlowWrapper(publisher: $isLoadingMore.eraseToAnyPublisher())
    }
    
    var searchQueryFlow: any StateFlow<String> {
        StateFlowWrapper(publisher: $searchQuery.eraseToAnyPublisher())
    }
    
    var isSearchActiveFlow: any StateFlow<Bool> {
        StateFlowWrapper(publisher: $isSearchActive.eraseToAnyPublisher())
    }
    
    var searchFiltersFlow: any StateFlow<SearchFilters> {
        StateFlowWrapper(publisher: $searchFilters.eraseToAnyPublisher())
    }
    
    var isAdvancedSearchActiveFlow: any StateFlow<Bool> {
        StateFlowWrapper(publisher: $isAdvancedSearchActive.eraseToAnyPublisher())
    }
    
    var isOfflineModeFlow: any StateFlow<Bool> {
        StateFlowWrapper(publisher: $isOfflineMode.eraseToAnyPublisher())
    }
    
    var isNetworkAvailableFlow: any StateFlow<Bool> {
        StateFlowWrapper(publisher: $isNetworkAvailable.eraseToAnyPublisher())
    }
    
    var isLoadingFlow: any StateFlow<Bool> {
        StateFlowWrapper(publisher: $isLoading.eraseToAnyPublisher())
    }
    
    init() {
        let container = DependencyContainer.shared
        self.cocktailRepository = container.cocktailRepository
        self.searchUseCase = container.searchCocktailsUseCase
        self.networkUseCase = container.networkStatusUseCase
        
        // Start monitoring network status
        Task {
            for await status in networkUseCase.networkStatus {
                isNetworkAvailable = status
            }
        }
        
        // Initial load
        Task {
            await loadCocktails()
        }
    }
    
    // MARK: - IHomeViewModel Implementation
    
    func loadCocktails() {
        Task {
            isLoading = true
            do {
                let result = try await cocktailRepository.getCocktails()
                if let cocktailArray = result as? Result.Success<[Cocktail]> {
                    cocktails = cocktailArray.data
                }
            } catch {
                print("Error loading cocktails: \(error)")
            }
            isLoading = false
        }
    }
    
    func loadMoreCocktails() {
        guard !isLoadingMore && hasMoreData else { return }
        
        Task {
            isLoadingMore = true
            do {
                let result = try await cocktailRepository.getMoreCocktails()
                if let cocktailArray = result as? Result.Success<[Cocktail]> {
                    cocktails.append(contentsOf: cocktailArray.data)
                    hasMoreData = cocktailArray.data.count > 0
                }
            } catch {
                print("Error loading more cocktails: \(error)")
            }
            isLoadingMore = false
        }
    }
    
    func searchCocktails(query: String) {
        searchQuery = query
        Task {
            isLoading = true
            do {
                let result = try await searchUseCase.search(query: query, filters: searchFilters)
                if let cocktailArray = result as? Result.Success<[Cocktail]> {
                    cocktails = cocktailArray.data
                }
            } catch {
                print("Error searching cocktails: \(error)")
            }
            isLoading = false
        }
    }
    
    func updateSearchFilters(filters: SearchFilters) {
        searchFilters = filters
        if isSearchActive {
            searchCocktails(query: searchQuery)
        }
    }
    
    func clearSearchFilters() {
        searchFilters = SearchFilters()
        if isSearchActive {
            searchCocktails(query: searchQuery)
        }
    }
    
    func toggleAdvancedSearchMode(active: Bool) {
        isAdvancedSearchActive = active
    }
    
    func toggleSearchMode(active: Bool) {
        isSearchActive = active
        if !active {
            searchQuery = ""
            clearSearchFilters()
            loadCocktails()
        }
    }
    
    func loadCocktailsByCategory(category: String?) {
        Task {
            isLoading = true
            do {
                let result = try await cocktailRepository.getCocktailsByCategory(category: category)
                if let cocktailArray = result as? Result.Success<[Cocktail]> {
                    cocktails = cocktailArray.data
                }
            } catch {
                print("Error loading cocktails by category: \(error)")
            }
            isLoading = false
        }
    }
    
    func sortByPrice(ascending: Bool) {
        cocktails.sort { first, second in
            ascending ? first.price < second.price : first.price > second.price
        }
    }
    
    func sortByPopularity() {
        cocktails.sort { $0.popularity > $1.popularity }
    }
    
    func setOfflineMode(enabled: Bool) {
        isOfflineMode = enabled
        if enabled {
            // Load cached data
            loadCocktails()
        }
    }
    
    func retry() {
        loadCocktails()
    }
    
    func getCocktailById(id: String) -> any Flow<Cocktail?> {
        cocktailRepository.getCocktailById(id: id)
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

extension Publisher where Failure == Never {
    var value: Output {
        var result: Output?
        let semaphore = DispatchSemaphore(value: 0)
        
        let cancellable = first()
            .sink { value in
                result = value
                semaphore.signal()
            }
        
        semaphore.wait()
        cancellable.cancel()
        
        return result!
    }
}