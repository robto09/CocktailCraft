import SwiftUI

enum AppTheme {
    static let primaryColor = Color("PrimaryColor")
    static let secondaryColor = Color("SecondaryColor")
    static let backgroundColor = Color("BackgroundColor")
    static let textColor = Color("TextColor")
    
    static let padding: CGFloat = 16
    static let cornerRadius: CGFloat = 8
    
    enum Typography {
        static let titleFont = Font.system(size: 24, weight: .bold)
        static let headlineFont = Font.system(size: 20, weight: .semibold)
        static let bodyFont = Font.system(size: 16, weight: .regular)
        static let captionFont = Font.system(size: 14, weight: .regular)
    }
    
    enum Animation {
        static let standard = SwiftUI.Animation.easeInOut(duration: 0.3)
        static let quick = SwiftUI.Animation.easeInOut(duration: 0.15)
    }
}

struct ThemeModifier: ViewModifier {
    @Environment(\.colorScheme) var colorScheme
    
    func body(content: Content) -> some View {
        content
            .environment(\.colorScheme, colorScheme)
            .preferredColorScheme(colorScheme)
    }
}

extension View {
    func withAppTheme() -> some View {
        modifier(ThemeModifier())
    }
}