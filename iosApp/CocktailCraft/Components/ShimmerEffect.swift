import SwiftUI

struct ShimmerEffect: View {
    @State private var isAnimating = false
    
    var body: some View {
        GeometryReader { geometry in
            LinearGradient(
                gradient: Gradient(colors: [
                    Color.gray.opacity(0.3),
                    Color.gray.opacity(0.1),
                    Color.gray.opacity(0.3)
                ]),
                startPoint: .leading,
                endPoint: .trailing
            )
            .frame(width: geometry.size.width * 2)
            .offset(x: isAnimating ? geometry.size.width : -geometry.size.width)
            .animation(
                .linear(duration: 1.5)
                    .repeatForever(autoreverses: false),
                value: isAnimating
            )
        }
        .onAppear {
            isAnimating = true
        }
    }
}

// Placeholder cocktail card with shimmer
struct CocktailCardPlaceholder: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            // Image placeholder
            RoundedRectangle(cornerRadius: 12)
                .fill(Color.gray.opacity(0.2))
                .frame(height: 150)
                .overlay(ShimmerEffect())
                .clipped()
            
            // Title placeholder
            RoundedRectangle(cornerRadius: 4)
                .fill(Color.gray.opacity(0.2))
                .frame(height: 20)
                .overlay(ShimmerEffect())
                .clipped()
            
            // Subtitle placeholder
            HStack {
                RoundedRectangle(cornerRadius: 4)
                    .fill(Color.gray.opacity(0.2))
                    .frame(width: 80, height: 16)
                    .overlay(ShimmerEffect())
                    .clipped()
                
                Spacer()
                
                RoundedRectangle(cornerRadius: 4)
                    .fill(Color.gray.opacity(0.2))
                    .frame(width: 50, height: 16)
                    .overlay(ShimmerEffect())
                    .clipped()
            }
        }
        .padding(4)
        .background(Color(UIColor.systemBackground))
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.1), radius: 4, x: 0, y: 2)
    }
}