import SwiftUI

struct LoadingMoreIndicator: View {
    let isLoading: Bool
    var indicatorSize: CGFloat = 32
    var animationDuration: Double = 0.3
    var indicatorColor: Color = .orange
    
    var body: some View {
        if isLoading {
            HStack {
                Spacer()
                
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: indicatorColor))
                    .scaleEffect(indicatorSize / 20) // Default size is ~20
                    .padding(16)
                
                Spacer()
            }
            .transition(.opacity.animation(.easeInOut(duration: animationDuration)))
        }
    }
}

struct InlineLoadingIndicator: View {
    let isLoading: Bool
    let text: String
    var indicatorColor: Color = .orange
    
    var body: some View {
        if isLoading {
            HStack(spacing: 12) {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: indicatorColor))
                    .scaleEffect(0.8)
                
                Text(text)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
            .padding(.vertical, 16)
            .frame(maxWidth: .infinity)
            .transition(.opacity.animation(.easeInOut(duration: 0.3)))
        }
    }
}

#Preview {
    VStack(spacing: 20) {
        // List example
        List {
            ForEach(0..<5) { index in
                Text("Item \(index + 1)")
                    .padding(.vertical, 8)
            }
            
            LoadingMoreIndicator(isLoading: true)
        }
        .frame(height: 300)
        
        // Inline example
        InlineLoadingIndicator(isLoading: true, text: "Loading more cocktails...")
    }
}