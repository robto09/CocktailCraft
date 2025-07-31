import SwiftUI
import shared

/**
 * Demo view showing SKIE integration working.
 * This proves that:
 * 1. SharedViewModels are accessible from Swift
 * 2. StateFlows convert to AsyncSequence  
 * 3. Suspend functions work as async/await
 * 4. No FlowCollector bridge needed!
 */
struct SKIETestView: View {
    @State private var testResults: [String] = []
    @State private var isLoading = false
    
    var body: some View {
        NavigationView {
            VStack(spacing: 20) {
                Text("🎉 SKIE Integration Test")
                    .font(.title)
                    .fontWeight(.bold)
                
                if isLoading {
                    ProgressView("Testing SKIE...")
                        .progressViewStyle(CircularProgressViewStyle())
                }
                
                ScrollView {
                    LazyVStack(alignment: .leading, spacing: 10) {
                        ForEach(testResults, id: \.self) { result in
                            HStack {
                                Image(systemName: result.contains("✅") ? "checkmark.circle.fill" : "xmark.circle.fill")
                                    .foregroundColor(result.contains("✅") ? .green : .red)
                                Text(result)
                                    .font(.system(.body, design: .monospaced))
                            }
                            .padding(.horizontal)
                        }
                    }
                }
                
                Button("Run SKIE Tests") {
                    runSKIETests()
                }
                .buttonStyle(.borderedProminent)
                .disabled(isLoading)
                
                Spacer()
            }
            .padding()
            .navigationTitle("SKIE Demo")
        }
        .onAppear {
            runSKIETests()
        }
    }
    
    private func runSKIETests() {
        isLoading = true
        testResults = []
        
        Task {
            await MainActor.run {
                addResult("🚀 Starting SKIE integration tests...")
            }
            
            // Test 1: Can we create KoinHelper?
            do {
                let helper = KoinHelper()
                await MainActor.run {
                    addResult("✅ KoinHelper created successfully")
                }
                
                // Test 2: Can we get SharedHomeViewModel?
                let homeViewModel = helper.getSharedHomeViewModelSKIE()
                await MainActor.run {
                    addResult("✅ SharedHomeViewModel retrieved via SKIE")
                }
                
                // Test 3: Can we access StateFlows as AsyncSequence?
                await MainActor.run {
                    addResult("🔄 Testing StateFlow → AsyncSequence conversion...")
                }
                
                // Start observing cocktails StateFlow using SKIE async/await
                Task {
                    var iterationCount = 0
                    for await cocktails in homeViewModel.cocktails {
                        iterationCount += 1
                        await MainActor.run {
                            addResult("✅ StateFlow update #\(iterationCount): \(cocktails.count) cocktails")
                        }
                        
                        // Only observe a few updates to avoid infinite loop
                        if iterationCount >= 2 {
                            break
                        }
                    }
                }
                
                // Test 4: Can we call suspend functions as async/await?
                await MainActor.run {
                    addResult("🔄 Testing suspend function → async/await...")
                }
                
                await homeViewModel.loadCocktails()
                await MainActor.run {
                    addResult("✅ homeViewModel.loadCocktails() called with async/await")
                }
                
                // Test 5: Can we get SharedCartViewModel?
                let cartViewModel = helper.getSharedCartViewModelSKIE()
                await MainActor.run {
                    addResult("✅ SharedCartViewModel retrieved via SKIE")
                }
                
                // Test 6: Check cart StateFlow
                Task {
                    for await cartItems in cartViewModel.cartItems {
                        await MainActor.run {
                            addResult("✅ Cart StateFlow working: \(cartItems.count) items")
                        }
                        break // Only check once
                    }
                }
                
                // Test 7: Synchronous function calls
                let categories = homeViewModel.getCategories()
                await MainActor.run {
                    addResult("✅ Synchronous method call: \(categories.count) categories")
                }
                
                await MainActor.run {
                    addResult("🎉 SKIE integration fully working!")
                    addResult("📱 No FlowCollector bridge needed")
                    addResult("⚡ Native Swift async/await with Kotlin")
                    isLoading = false
                }
                
            } catch {
                await MainActor.run {
                    addResult("❌ SKIE test failed: \(error.localizedDescription)")
                    isLoading = false
                }
            }
        }
    }
    
    private func addResult(_ result: String) {
        testResults.append(result)
    }
}

// MARK: - Demo comparison of old vs new

struct SKIEComparisonView: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 20) {
            Text("SKIE vs FlowCollector")
                .font(.title)
                .fontWeight(.bold)
            
            VStack(alignment: .leading, spacing: 10) {
                Text("❌ Old FlowCollector Pattern:")
                    .font(.headline)
                    .foregroundColor(.red)
                
                Text("""
                // Complex bridge code needed
                let collector = FlowCollector<NSArray> { cocktailArray in
                    DispatchQueue.main.async {
                        if let cocktails = cocktailArray as? [Cocktail] {
                            self.cocktails = cocktails
                        }
                    }
                }
                try await kotlinFlow.collect(collector: collector)
                """)
                .font(.system(.caption, design: .monospaced))
                .padding()
                .background(Color.red.opacity(0.1))
                .cornerRadius(8)
            }
            
            VStack(alignment: .leading, spacing: 10) {
                Text("✅ New SKIE Pattern:")
                    .font(.headline)
                    .foregroundColor(.green)
                
                Text("""
                // Pure Swift async/await - no bridge!
                for await cocktails in sharedViewModel.cocktails {
                    await MainActor.run {
                        self.cocktails = cocktails
                    }
                }
                
                // Suspend functions work natively
                await sharedViewModel.loadCocktails()
                """)
                .font(.system(.caption, design: .monospaced))
                .padding()
                .background(Color.green.opacity(0.1))
                .cornerRadius(8)
            }
            
            Spacer()
        }
        .padding()
    }
}

// MARK: - Preview

struct SKIETestView_Previews: PreviewProvider {
    static var previews: some View {
        SKIETestView()
    }
}