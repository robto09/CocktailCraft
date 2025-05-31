import SwiftUI

struct DetailHeaderImage: View {
    let imageUrl: String
    let contentDescription: String
    var height: CGFloat = 250
    var targetSize: CGFloat = 800
    var gradientColors: [Color] = [.clear, Color.black.opacity(0.5)]
    var gradientStartPoint: UnitPoint = .center
    
    @State private var isLoading = true
    @State private var hasError = false
    
    var body: some View {
        ZStack {
            if let url = URL(string: imageUrl) {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .empty:
                        shimmerBackground
                    case .success(let image):
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                            .frame(height: height)
                            .clipped()
                            .onAppear { isLoading = false }
                    case .failure(_):
                        placeholderImage
                            .onAppear { 
                                isLoading = false
                                hasError = true
                            }
                    @unknown default:
                        shimmerBackground
                    }
                }
            } else {
                placeholderImage
            }
            
            // Gradient overlay
            LinearGradient(
                gradient: Gradient(colors: gradientColors),
                startPoint: gradientStartPoint,
                endPoint: .bottom
            )
        }
        .frame(height: height)
        .frame(maxWidth: .infinity)
    }
    
    private var shimmerBackground: some View {
        Rectangle()
            .fill(Color.gray.opacity(0.3))
            .overlay(
                GeometryReader { geometry in
                    Rectangle()
                        .fill(
                            LinearGradient(
                                gradient: Gradient(colors: [
                                    Color.white.opacity(0.3),
                                    Color.white.opacity(0.5),
                                    Color.white.opacity(0.3)
                                ]),
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .offset(x: isLoading ? -geometry.size.width : geometry.size.width)
                        .animation(
                            Animation.linear(duration: 1.5)
                                .repeatForever(autoreverses: false),
                            value: isLoading
                        )
                }
            )
            .onAppear { isLoading = true }
    }
    
    private var placeholderImage: some View {
        ZStack {
            Rectangle()
                .fill(Color.gray.opacity(0.2))
            
            Image(systemName: "photo")
                .font(.system(size: 60))
                .foregroundColor(.gray.opacity(0.5))
        }
    }
}

#Preview {
    VStack(spacing: 20) {
        DetailHeaderImage(
            imageUrl: "https://www.thecocktaildb.com/images/media/drink/5noda61589575158.jpg",
            contentDescription: "Cocktail Image"
        )
        
        DetailHeaderImage(
            imageUrl: "invalid-url",
            contentDescription: "Invalid Image"
        )
    }
}