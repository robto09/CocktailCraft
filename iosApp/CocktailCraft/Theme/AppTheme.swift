import SwiftUI
import shared

/// Environment key for theme state
struct ThemeEnvironmentKey: EnvironmentKey {
    static let defaultValue = false // Default to light mode
}

extension EnvironmentValues {
    var isDarkMode: Bool {
        get { self[ThemeEnvironmentKey.self] }
        set { self[ThemeEnvironmentKey.self] = newValue }
    }
}

/// App typography and spacing constants matching Android design
struct AppTheme {
    
    // MARK: - Typography
    struct Typography {
        static let largeTitle = Font.largeTitle.weight(.bold)
        static let title = Font.title.weight(.semibold)
        static let title2 = Font.title2.weight(.semibold)
        static let headline = Font.headline.weight(.semibold)
        static let subheadline = Font.subheadline.weight(.medium)
        static let body = Font.body
        static let callout = Font.callout
        static let caption = Font.caption
        static let caption2 = Font.caption2
        
        // Custom sizes matching Android, each anchored to a text
        // style so they scale with the user's Dynamic Type setting
        static let cardTitle = Font.callout.weight(.bold)
        static let cardSubtitle = Font.subheadline
        static let cardCaption = Font.caption
        static let price = Font.callout.weight(.bold)
    }
    
    // MARK: - Spacing
    // Mapped from the cross-platform SpacingTokens
    // (shared/designsystem/DesignTokens.kt) — identical to Android's Spacing.
    struct Spacing {
        static let xs = CGFloat(SpacingTokens.shared.xs)
        static let sm = CGFloat(SpacingTokens.shared.sm)
        static let md = CGFloat(SpacingTokens.shared.md)
        static let lg = CGFloat(SpacingTokens.shared.lg)
        static let xl = CGFloat(SpacingTokens.shared.xl)
        static let xxl = CGFloat(SpacingTokens.shared.xxl)
        static let xxxl = CGFloat(SpacingTokens.shared.xxxl)

        // Card specific spacing
        static let cardPadding: CGFloat = 12
        static let cardSpacing: CGFloat = 16
        static let cardImageHeight: CGFloat = 150
    }

    // MARK: - Corner Radius
    // Mapped from the cross-platform RadiusTokens
    // (shared/designsystem/DesignTokens.kt) — identical to Android's shapes.
    struct CornerRadius {
        static let sm = CGFloat(RadiusTokens.shared.sm)
        static let md = CGFloat(RadiusTokens.shared.md)
        static let lg = CGFloat(RadiusTokens.shared.lg)
        static let xl = CGFloat(RadiusTokens.shared.xl)

        // Component specific
        static let card = CGFloat(RadiusTokens.shared.card)
        static let button = CGFloat(RadiusTokens.shared.button)
        static let chip = CGFloat(RadiusTokens.shared.chip)
        static let searchBar = CGFloat(RadiusTokens.shared.searchBar)
    }
    
    // MARK: - Shadows
    struct Shadow {
        static let card = (color: Color.black.opacity(0.1), radius: CGFloat(4), x: CGFloat(0), y: CGFloat(2))
        static let button = (color: Color.black.opacity(0.15), radius: CGFloat(2), x: CGFloat(0), y: CGFloat(1))
        static let searchBar = (color: Color.black.opacity(0.05), radius: CGFloat(1), x: CGFloat(0), y: CGFloat(1))
    }
    
    // MARK: - Animation
    struct Animation {
        static let standard = SwiftUI.Animation.easeInOut(duration: 0.3)
        static let quick = SwiftUI.Animation.easeInOut(duration: 0.2)
        static let slow = SwiftUI.Animation.easeInOut(duration: 0.5)
    }
}

// MARK: - View Modifiers
struct CardStyle: ViewModifier {
    @Environment(\.isDarkMode) var isDarkMode

    func body(content: Content) -> some View {
        content
            .background(AppColors.surface(isDarkMode: isDarkMode))
            .cornerRadius(AppTheme.CornerRadius.card)
            .shadow(
                color: AppTheme.Shadow.card.color,
                radius: AppTheme.Shadow.card.radius,
                x: AppTheme.Shadow.card.x,
                y: AppTheme.Shadow.card.y
            )
    }
}

struct ChipStyle: ViewModifier {
    let isSelected: Bool
    @Environment(\.isDarkMode) var isDarkMode

    func body(content: Content) -> some View {
        content
            .padding(.horizontal, AppTheme.Spacing.lg)
            .padding(.vertical, AppTheme.Spacing.xs)
            .background(isSelected ? AppColors.primary(isDarkMode: isDarkMode) : AppColors.surface(isDarkMode: isDarkMode))
            .foregroundColor(isSelected ? .white : AppColors.primary(isDarkMode: isDarkMode))
            .cornerRadius(AppTheme.CornerRadius.chip)
            .shadow(
                color: isSelected ? AppTheme.Shadow.button.color : Color.clear,
                radius: AppTheme.Shadow.button.radius,
                x: AppTheme.Shadow.button.x,
                y: AppTheme.Shadow.button.y
            )
    }
}

// MARK: - Branded Navigation Chrome

/// Branded navigation bar: coral background, white title and bar content,
/// inline (fixed-height) layout — matching Android's AppTopBar so both
/// platforms share the same chrome. Apply to the root content of every
/// NavigationStack (tab roots, pushed screens, and sheets).
struct BrandedNavigationBarStyle: ViewModifier {
    @Environment(\.isDarkMode) var isDarkMode

    func body(content: Content) -> some View {
        content
            .navigationBarTitleDisplayMode(.inline)
            .toolbarBackground(AppColors.primary(isDarkMode: isDarkMode), for: .navigationBar)
            .toolbarBackground(.visible, for: .navigationBar)
            // Dark scheme = white title/status bar over the coral background
            .toolbarColorScheme(.dark, for: .navigationBar)
    }
}

/// Branded tab bar: opaque surface-colored background matching Android's
/// bottom navigation (the coral selected tint comes from ContentView's .tint).
struct BrandedTabBarStyle: ViewModifier {
    @Environment(\.isDarkMode) var isDarkMode

    func body(content: Content) -> some View {
        content
            .toolbarBackground(AppColors.surface(isDarkMode: isDarkMode), for: .tabBar)
            .toolbarBackground(.visible, for: .tabBar)
    }
}

// MARK: - View Extensions
extension View {
    func cardStyle() -> some View {
        modifier(CardStyle())
    }

    /// Coral navigation bar with white title — the app's shared chrome.
    func brandedNavigationBar() -> some View {
        modifier(BrandedNavigationBarStyle())
    }

    /// Opaque surface tab bar matching Android's bottom navigation.
    func brandedTabBar() -> some View {
        modifier(BrandedTabBarStyle())
    }

    func chipStyle(isSelected: Bool = false) -> some View {
        modifier(ChipStyle(isSelected: isSelected))
    }

    /// Expands an icon-only control's tappable area to Apple's 44 pt HIG
    /// minimum without growing the icon's visual size.
    func minimumHitTarget() -> some View {
        frame(minWidth: 44, minHeight: 44)
            .contentShape(Rectangle())
    }
}

// Double.asPrice moved to Utils/WidgetBridge.swift so the widget extension
// (which also compiles that file) shares the exact same price formatting.
