//
//  ContentView.swift
//  CocktailCraft
//
//  Created by Robert Torres on 29/5/25.
//

import SwiftUI
import shared

struct ContentView: View {
    @State private var greetingText = "Loading..."
    
    var body: some View {
        VStack {
            Image(systemName: "globe")
                .imageScale(.large)
                .foregroundStyle(.tint)
            Text(greetingText)
                .padding()
        }
        .onAppear {
            // Test calling a function from shared module
            let greeting = Greeting()
            greetingText = greeting.getGreeting()
        }
    }
}

#Preview {
    ContentView()
}
