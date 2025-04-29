import Foundation
import Combine
import shared

@MainActor
class MainViewModel: ObservableObject {
    private let homeViewModel: HomeViewModel
    
    // Published properties for UI state
    @Published var cocktails: [Cocktail] = []
    @Published var isLoading: Bool = false
    @Published var error: String? = nil
    @Published var searchQuery: String = ""
    
    private var cancellables = Set<AnyCancellable>()
    
    init(homeViewModel: HomeViewModel) {
        self.homeViewModel = homeViewModel
        setupObservers()
    }
    
    private func setupObservers() {
        // Observe KMP ViewModel state changes
        observeState()
        observeEffects()
    }
    
    private func observeState() {
        // Convert KMP StateFlow to Combine Publisher
        homeViewModel.stateFlow
            .watch { [weak self] state in
                guard let self = self, let state = state else { return }
                
                Task { @MainActor in
                    self.isLoading = state.isLoading
                    self.cocktails = state.cocktails
                    if let error = state.error {
                        self.error = error.message
                    }
                }
            }
    }
    
    private func observeEffects() {
        // Handle side effects from KMP ViewModel
        homeViewModel.effectFlow
            .watch { effect in
                // Handle effects when needed
            }
    }
    
    // MARK: - User Actions
    
    func onSearchQueryChanged(_ query: String) {
        searchQuery = query
        homeViewModel.onSearchQueryChanged(query: query)
    }
    
    func loadCocktails() {
        homeViewModel.loadCocktails()
    }
    
    func refreshCocktails() {
        homeViewModel.refreshCocktails()
    }
    
    func toggleFavorite(cocktailId: String) {
        homeViewModel.toggleFavorite(cocktailId: cocktailId)
    }
    
    // MARK: - Error Handling
    
    func clearError() {
        error = nil
    }
    
    // MARK: - Cleanup
    
    func cleanup() {
        cancellables.removeAll()
    }
}