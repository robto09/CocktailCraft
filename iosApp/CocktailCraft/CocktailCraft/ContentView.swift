//
//  ContentView.swift
//  CocktailCraft
//
//  Created by Robert Torres on 29/5/25.
//

import SwiftUI
import shared

struct ContentView: View {
    @StateObject private var homeViewModel = ViewModelProvider.shared.homeViewModel
    @StateObject private var themeViewModel = ViewModelProvider.shared.themeViewModel
    
    var body: some View {
        NavigationView {
            VStack {
                if homeViewModel.isLoading {
                    ProgressView("Loading cocktails...")
                        .padding()
                } else if let error = homeViewModel.errorMessage {
                    Text("Error: \(error)")
                        .foregroundColor(.red)
                        .padding()
                } else {
                    List(homeViewModel.cocktails, id: \.id) { cocktail in
                        VStack(alignment: .leading) {
                            Text(cocktail.name)
                                .font(.headline)
                            Text(cocktail.description_)
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                                .lineLimit(2)
                            Text("$\(String(format: "%.2f", cocktail.price))")
                                .font(.caption)
                                .foregroundColor(.blue)
                        }
                        .padding(.vertical, 4)
                    }
                }
            }
            .navigationTitle("CocktailCraft")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {
                        themeViewModel.toggleTheme()
                    }) {
                        Image(systemName: themeViewModel.isDarkMode ? "sun.max.fill" : "moon.fill")
                    }
                }
            }
        }
        .preferredColorScheme(themeViewModel.isDarkMode ? .dark : .light)
        .onAppear {
            homeViewModel.loadCocktails()
        }
    }
}

#Preview {
    ContentView()
}
