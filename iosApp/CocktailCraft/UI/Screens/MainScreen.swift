import SwiftUI

struct MainScreen: View {
    var body: some View {
        ZStack {
            Color(.systemBackground)
                .ignoresSafeArea()
            
            VStack(spacing: 20) {
                Text("Hello, CocktailCraft!")
                    .font(.system(size: 32, weight: .bold))
                    .multilineTextAlignment(.center)
                
                Text("iOS + KMP")
                    .font(.system(size: 24))
                    .foregroundColor(.secondary)
            }
            .padding()
        }
    }
}

#Preview {
    MainScreen()
}