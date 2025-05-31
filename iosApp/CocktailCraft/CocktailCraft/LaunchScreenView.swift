import SwiftUI

struct LaunchScreenView: View {
    @State private var isAnimating = false
    @State private var showWave = false
    
    var body: some View {
        ZStack {
            // Background color
            Color(red: 0.922, green: 0.416, blue: 0.263) // Primary color
                .ignoresSafeArea()
            
            // Wave pattern at bottom
            VStack {
                Spacer()
                
                WaveShape()
                    .fill(
                        LinearGradient(
                            gradient: Gradient(colors: [
                                Color(red: 1.0, green: 0.784, blue: 0.302), // Secondary color
                                Color(red: 1.0, green: 0.784, blue: 0.302).opacity(0.8)
                            ]),
                            startPoint: .top,
                            endPoint: .bottom
                        )
                    )
                    .frame(height: 200)
                    .offset(y: showWave ? 0 : 300)
                    .animation(.easeOut(duration: 1.0).delay(0.5), value: showWave)
            }
            .ignoresSafeArea()
            
            // Center content
            VStack(spacing: 20) {
                // Animated cocktail glass
                ZStack {
                    Circle()
                        .fill(Color.white.opacity(0.2))
                        .frame(width: 150, height: 150)
                        .scaleEffect(isAnimating ? 1.1 : 0.9)
                        .animation(
                            Animation.easeInOut(duration: 2.0)
                                .repeatForever(autoreverses: true),
                            value: isAnimating
                        )
                    
                    CocktailGlassShape()
                        .fill(Color.white)
                        .frame(width: 80, height: 80)
                        .rotationEffect(.degrees(isAnimating ? 5 : -5))
                        .animation(
                            Animation.easeInOut(duration: 2.0)
                                .repeatForever(autoreverses: true),
                            value: isAnimating
                        )
                }
                
                // App name
                VStack(spacing: 4) {
                    Text("CocktailCraft")
                        .font(.system(size: 36, weight: .bold, design: .rounded))
                        .foregroundColor(.white)
                    
                    Text("Discover Amazing Cocktails")
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(.white.opacity(0.9))
                }
                .opacity(isAnimating ? 1 : 0)
                .animation(.easeIn(duration: 0.8).delay(0.3), value: isAnimating)
            }
        }
        .onAppear {
            isAnimating = true
            showWave = true
        }
    }
}

struct WaveShape: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        let width = rect.width
        let height = rect.height
        
        path.move(to: CGPoint(x: 0, y: height * 0.3))
        
        // Create wave using cubic bezier curves
        path.addCurve(
            to: CGPoint(x: width * 0.5, y: height * 0.3),
            control1: CGPoint(x: width * 0.25, y: 0),
            control2: CGPoint(x: width * 0.25, y: height * 0.6)
        )
        
        path.addCurve(
            to: CGPoint(x: width, y: height * 0.3),
            control1: CGPoint(x: width * 0.75, y: 0),
            control2: CGPoint(x: width * 0.75, y: height * 0.6)
        )
        
        // Complete the shape
        path.addLine(to: CGPoint(x: width, y: height))
        path.addLine(to: CGPoint(x: 0, y: height))
        path.closeSubpath()
        
        return path
    }
}

struct LaunchScreenView_Previews: PreviewProvider {
    static var previews: some View {
        LaunchScreenView()
    }
}