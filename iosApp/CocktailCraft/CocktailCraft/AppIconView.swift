import SwiftUI

// This view represents the app icon design
// To generate the actual app icon:
// 1. Run the app and take a screenshot of this view
// 2. Export it at 1024x1024 resolution
// 3. Add it to Assets.xcassets/AppIcon.appiconset

struct AppIconView: View {
    let size: CGFloat
    
    init(size: CGFloat = 1024) {
        self.size = size
    }
    
    var body: some View {
        ZStack {
            // Background gradient
            LinearGradient(
                gradient: Gradient(colors: [
                    Color(red: 0.922, green: 0.416, blue: 0.263), // Primary color
                    Color(red: 0.8, green: 0.3, blue: 0.15) // Darker shade
                ]),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            
            // Cocktail glass icon
            CocktailGlassShape()
                .fill(Color.white)
                .frame(width: size * 0.6, height: size * 0.6)
                .shadow(color: Color.black.opacity(0.2), radius: size * 0.02, x: 0, y: size * 0.01)
        }
        .frame(width: size, height: size)
        .cornerRadius(size * 0.2237) // iOS app icon corner radius
    }
}

struct CocktailGlassShape: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        let width = rect.width
        let height = rect.height
        
        // Glass bowl (martini glass style)
        path.move(to: CGPoint(x: width * 0.1, y: height * 0.2))
        path.addLine(to: CGPoint(x: width * 0.9, y: height * 0.2))
        path.addLine(to: CGPoint(x: width * 0.5, y: height * 0.6))
        path.closeSubpath()
        
        // Stem
        path.move(to: CGPoint(x: width * 0.5, y: height * 0.6))
        path.addLine(to: CGPoint(x: width * 0.5, y: height * 0.8))
        
        // Base
        path.move(to: CGPoint(x: width * 0.3, y: height * 0.8))
        path.addLine(to: CGPoint(x: width * 0.7, y: height * 0.8))
        
        // Olive or cherry garnish
        path.addEllipse(in: CGRect(
            x: width * 0.65,
            y: height * 0.15,
            width: width * 0.1,
            height: width * 0.1
        ))
        
        return path.strokedPath(StrokeStyle(lineWidth: rect.width * 0.04, lineCap: .round, lineJoin: .round))
    }
}

// Preview for development
struct AppIconView_Previews: PreviewProvider {
    static var previews: some View {
        VStack(spacing: 20) {
            AppIconView(size: 180)
                .previewDisplayName("180x180")
            
            AppIconView(size: 120)
                .previewDisplayName("120x120")
            
            AppIconView(size: 60)
                .previewDisplayName("60x60")
        }
        .padding()
        .background(Color.gray.opacity(0.2))
    }
}