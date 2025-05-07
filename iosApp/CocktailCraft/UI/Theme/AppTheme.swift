import SwiftUI

struct AppTheme {
    static let shared = AppTheme()
    
    // Colors
    struct Colors {
        let primary = Color("Primary", bundle: nil)
        let secondary = Color("Secondary", bundle: nil)
        let background = Color("Background", bundle: nil)
        let surface = Color("Surface", bundle: nil)
        let error = Color("Error", bundle: nil)
        
        // Text colors
        let onPrimary = Color.white
        let onSecondary = Color.white
        let onBackground = Color("OnBackground", bundle: nil)
        let onSurface = Color("OnSurface", bundle: nil)
        let onError = Color.white
    }
    
    // Typography
    struct Typography {
        let largeTitle = Font.largeTitle
        let title = Font.title
        let headline = Font.headline
        let body = Font.body
        let subheadline = Font.subheadline
        let footnote = Font.footnote
        let caption = Font.caption
    }
    
    // Spacing
    struct Spacing {
        let small: CGFloat = 8
        let medium: CGFloat = 16
        let large: CGFloat = 24
        let xlarge: CGFloat = 32
    }
    
    // Shapes
    struct Shapes {
        let smallRadius: CGFloat = 4
        let mediumRadius: CGFloat = 8
        let largeRadius: CGFloat = 12
    }
    
    let colors = Colors()
    let typography = Typography()
    let spacing = Spacing()
    let shapes = Shapes()
}

// Environment key for theme
struct ThemeKey: EnvironmentKey {
    static let defaultValue = AppTheme.shared
}

extension EnvironmentValues {
    var theme: AppTheme {
        get { self[ThemeKey.self] }
        set { self[ThemeKey.self] = newValue }
    }
}

// View extension for easy theme access
extension View {
    func withAppTheme() -> some View {
        environment(\.theme, AppTheme.shared)
    }
}

// Preview provider
struct AppTheme_Previews: PreviewProvider {
    static var previews: some View {
        VStack {
            Text("Heading")
                .font(AppTheme.shared.typography.headline)
            Text("Body text")
                .font(AppTheme.shared.typography.body)
            Text("Caption")
                .font(AppTheme.shared.typography.caption)
        }
        .padding(AppTheme.shared.spacing.medium)
        .withAppTheme()
    }
}