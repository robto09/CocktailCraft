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
                } else if !homeViewModel.errorString.isEmpty {
                    Text("Error: \(homeViewModel.errorString)")
                        .foregroundColor(.red)
                        .padding()
                } else {
                    List(homeViewModel.cocktails, id: \.id) { cocktail in
                        VStack(alignment: .leading) {
                            Text(cocktail.name)
                                .font(.headline)
                            if let category = cocktail.category {
                                Text(category)
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                            }
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
                        themeViewModel.setDarkMode(enabled: !themeViewModel.isDarkMode)
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
