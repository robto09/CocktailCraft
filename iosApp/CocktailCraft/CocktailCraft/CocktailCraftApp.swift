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
    @State private var showLaunchScreen = true
    
    init() {
        // Initialize Koin DI
        DIHelperKt.doInitKoin()
    }
    
    var body: some Scene {
        WindowGroup {
            ZStack {
                if showLaunchScreen {
                    LaunchScreenView()
                        .transition(.opacity)
                } else {
                    MainView()
                        .transition(.opacity)
                }
            }
            .animation(.easeInOut(duration: 0.5), value: showLaunchScreen)
            .onAppear {
                // Show launch screen for 2 seconds
                DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
                    showLaunchScreen = false
                }
            }
        }
    }
}
