import SwiftUI

import shared

struct HomeView: View {
    @StateObject private var viewModel = HomeViewModel()
    @State private var searchText = ""
    @State private var showingFilters = false

    var body: some View {
            VStack {
                // Modern Search Bar
                HStack {
                    HStack {
                        Image(systemName: "magnifyingglass")
                            .foregroundStyle(.secondary)

                        TextField("Search cocktails...", text: $searchText)
                            .textFieldStyle(.plain)
                            .onSubmit {
                                viewModel.searchCocktails(query: searchText)
                            }

                        if !searchText.isEmpty {
                            Button(action: {
                                searchText = ""
                            }) {
                                Image(systemName: "xmark.circle.fill")
                                    .foregroundStyle(.secondary)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(.horizontal, 12)
                    .padding(.vertical, 8)
                    .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 10))
                }
                .padding(.horizontal)
                
                // Cocktail List
                // Debug info
                Text("Debug: Loading=\(viewModel.isLoading), Cocktails=\(viewModel.cocktails.count), Error=\(viewModel.error?.title ?? "none")")
                    .font(.caption)
                    .foregroundColor(.gray)
                    .padding(.horizontal)
                
                if viewModel.isLoading && viewModel.cocktails.isEmpty {
                    ScrollView {
                        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 16) {
                            ForEach(0..<6, id: \.self) { _ in
                                CocktailCardPlaceholder()
                            }
                        }
                        .padding()
                    }
                    .transition(.opacity)
                } else if let error = viewModel.error {
                    ErrorView(error: error, onRetry: {
                        viewModel.retryLoadCocktails()
                    })
                } else {
                    ScrollView {
                        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 16) {
                            ForEach(viewModel.filteredCocktails, id: \.id) { cocktail in
                                NavigationLink(destination: CocktailDetailView(cocktailId: cocktail.id)) {
                                    CocktailCard(cocktail: cocktail)
                                }
                                .buttonStyle(PlainButtonStyle())
                            }
                        }
                        .padding()
                    }
                    .refreshable {
                        await viewModel.refreshCocktails()
                    }
                }
            }
            .navigationTitle("CocktailCraft")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: { showingFilters.toggle() }) {
                        Image(systemName: "slider.horizontal.3")
                    }
                }
            }
            .sheet(isPresented: $showingFilters) {
                FilterView(viewModel: viewModel)
            }
        .onAppear {
            viewModel.loadCocktails()
        }
        .onChange(of: searchText) {
            viewModel.searchCocktails(query: searchText)
        }
    }
}

