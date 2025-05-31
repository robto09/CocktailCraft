//
//  CocktailCraftApp.swift
//  CocktailCraft
//
//  Created by Robert Torres on 29/5/25.
//

import SwiftUI
import shared

@main
struct CocktailCraftApp: App {
    init() {
        // Initialize Koin DI
        DIHelperKt.doInitKoin()
    }
    
    var body: some Scene {
        WindowGroup {
            MainView()
        }
    }
}
