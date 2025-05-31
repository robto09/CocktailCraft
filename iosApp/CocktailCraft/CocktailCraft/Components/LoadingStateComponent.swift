import SwiftUI

struct LoadingStateComponent: View {
    let isLoading: Bool
    var contentAlignment: Alignment = .center
    var indicatorSize: CGFloat = 40
    var indicatorColor: Color = .orange
    var padding: EdgeInsets? = nil
    
    var body: some View {
        if isLoading {
            GeometryReader { geometry in
                ZStack(alignment: contentAlignment) {
                    Color.clear
                    
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: indicatorColor))
                        .scaleEffect(indicatorSize / 20) // Default size is ~20
                        .padding(padding ?? EdgeInsets())
                }
                .frame(width: geometry.size.width, height: geometry.size.height)
            }
            .transition(.opacity)
        }
    }
}

struct FullScreenLoadingView: View {
    let isLoading: Bool
    var message: String? = nil
    var backgroundColor: Color = Color.black.opacity(0.3)
    var cardBackgroundColor: Color = .white
    var indicatorSize: CGFloat = 50
    
    var body: some View {
        if isLoading {
            ZStack {
                backgroundColor
                    .edgesIgnoringSafeArea(.all)
                
                VStack(spacing: 20) {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .orange))
                        .scaleEffect(indicatorSize / 20)
                    
                    if let message = message {
                        Text(message)
                            .font(.body)
                            .foregroundColor(.secondary)
                    }
                }
                .padding(30)
                .background(cardBackgroundColor)
                .cornerRadius(16)
                .shadow(radius: 10)
            }
            .transition(.opacity)
        }
    }
}

#Preview {
    ZStack {
        VStack(spacing: 20) {
            Text("Content underneath")
            Button("Action") {}
        }
        
        LoadingStateComponent(isLoading: true)
        
        FullScreenLoadingView(isLoading: true, message: "Loading cocktails...")
    }
}