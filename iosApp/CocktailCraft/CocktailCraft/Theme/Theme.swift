import SwiftUI

// MARK: - App Colors
struct AppColors {
    // Light Theme Colors
    static let primaryLight = Color(red: 0.922, green: 0.416, blue: 0.263) // #EB6A43
    static let secondaryLight = Color(red: 1.0, green: 0.784, blue: 0.302) // #FFC84D
    static let backgroundLight = Color(red: 0.98, green: 0.98, blue: 0.98) // #FAFAFA
    static let surfaceLight = Color.white
    static let textPrimaryLight = Color.black
    static let textSecondaryLight = Color(red: 0.557, green: 0.557, blue: 0.576) // #8E8E93
    
    // Dark Theme Colors
    static let primaryDark = Color(red: 1.0, green: 0.541, blue: 0.396) // #FF8A65
    static let secondaryDark = Color(red: 1.0, green: 0.82, blue: 0.502) // #FFD180
    static let backgroundDark = Color(red: 0.071, green: 0.071, blue: 0.071) // #121212
    static let surfaceDark = Color(red: 0.118, green: 0.118, blue: 0.118) // #1E1E1E
    static let textPrimaryDark = Color.white
    static let textSecondaryDark = Color(red: 0.69, green: 0.69, blue: 0.69) // #B0B0B0
    
    // Common Colors (same in both themes)
    static let success = Color(red: 0.204, green: 0.78, blue: 0.349) // #34C759
    static let error = Color(red: 1.0, green: 0.231, blue: 0.188) // #FF3B30
    static let warning = Color(red: 1.0, green: 0.584, blue: 0) // #FF9500
    static let gray = Color(red: 0.557, green: 0.557, blue: 0.576) // #8E8E93
    static let darkGray = Color(red: 0.388, green: 0.388, blue: 0.4) // #636366
    static let lightGray = Color(red: 0.898, green: 0.898, blue: 0.918) // #E5E5EA
    static let chipBackground = Color(red: 0.612, green: 0.361, blue: 0.22) // #9C5C38
}

// MARK: - Color Scheme Environment Key
private struct ColorSchemeKey: EnvironmentKey {
    static let defaultValue = ColorScheme.light
}

extension EnvironmentValues {
    var appColorScheme: ColorScheme {
        get { self[ColorSchemeKey.self] }
        set { self[ColorSchemeKey.self] = newValue }
    }
}

// MARK: - Theme-aware Color Extensions
extension Color {
    // Dynamic colors that change based on theme
    static func primary(for colorScheme: ColorScheme) -> Color {
        colorScheme == .dark ? AppColors.primaryDark : AppColors.primaryLight
    }
    
    static func secondary(for colorScheme: ColorScheme) -> Color {
        colorScheme == .dark ? AppColors.secondaryDark : AppColors.secondaryLight
    }
    
    static func background(for colorScheme: ColorScheme) -> Color {
        colorScheme == .dark ? AppColors.backgroundDark : AppColors.backgroundLight
    }
    
    static func surface(for colorScheme: ColorScheme) -> Color {
        colorScheme == .dark ? AppColors.surfaceDark : AppColors.surfaceLight
    }
    
    static func textPrimary(for colorScheme: ColorScheme) -> Color {
        colorScheme == .dark ? AppColors.textPrimaryDark : AppColors.textPrimaryLight
    }
    
    static func textSecondary(for colorScheme: ColorScheme) -> Color {
        colorScheme == .dark ? AppColors.textSecondaryDark : AppColors.textSecondaryLight
    }
}

// MARK: - View Modifiers for Theme Support
struct ThemedBackground: ViewModifier {
    @Environment(\.colorScheme) var colorScheme
    
    func body(content: Content) -> some View {
        content
            .background(Color.background(for: colorScheme))
    }
}

struct ThemedCard: ViewModifier {
    @Environment(\.colorScheme) var colorScheme
    let cornerRadius: CGFloat
    
    func body(content: Content) -> some View {
        content
            .background(Color.surface(for: colorScheme))
            .cornerRadius(cornerRadius)
            .shadow(color: Color.black.opacity(colorScheme == .dark ? 0.3 : 0.1), radius: 2, x: 0, y: 1)
    }
}

// MARK: - Convenience View Extensions
extension View {
    func themedBackground() -> some View {
        modifier(ThemedBackground())
    }
    
    func themedCard(cornerRadius: CGFloat = 12) -> some View {
        modifier(ThemedCard(cornerRadius: cornerRadius))
    }
    
    func primaryButton(colorScheme: ColorScheme) -> some View {
        self
            .foregroundColor(.white)
            .background(Color.primary(for: colorScheme))
            .cornerRadius(8)
    }
    
    func secondaryButton(colorScheme: ColorScheme) -> some View {
        self
            .foregroundColor(Color.primary(for: colorScheme))
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(Color.primary(for: colorScheme), lineWidth: 1)
            )
    }
}

// MARK: - Theme Manager
class ThemeManager: ObservableObject {
    static let shared = ThemeManager()
    
    @Published var currentTheme: ColorScheme = .light
    
    private init() {}
    
    func updateTheme(isDarkMode: Bool) {
        currentTheme = isDarkMode ? .dark : .light
    }
}