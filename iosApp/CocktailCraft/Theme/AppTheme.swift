import SwiftUI

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
    struct Spacing {
        static let xs: CGFloat = 4
        static let sm: CGFloat = 8
        static let md: CGFloat = 12
        static let lg: CGFloat = 16
        static let xl: CGFloat = 20
        static let xxl: CGFloat = 24
        static let xxxl: CGFloat = 32
        
        // Card specific spacing
        static let cardPadding: CGFloat = 12
        static let cardSpacing: CGFloat = 16
        static let cardImageHeight: CGFloat = 150
    }
    
    // MARK: - Corner Radius
    struct CornerRadius {
        static let sm: CGFloat = 8
        static let md: CGFloat = 12
        static let lg: CGFloat = 16
        static let xl: CGFloat = 20
        
        // Component specific
        static let card: CGFloat = 12
        static let button: CGFloat = 8
        static let chip: CGFloat = 16
        static let searchBar: CGFloat = 10
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

// MARK: - View Extensions
extension View {
    func cardStyle() -> some View {
        modifier(CardStyle())
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

extension Double {
    /// Canonical price string. Prices are demo USD, so formatting is pinned
    /// to en_US instead of the device locale (SwiftUI's `specifier:`
    /// interpolation is locale-aware and rendered "$12,78" on comma-decimal
    /// locales).
    var asPrice: String {
        Double.priceFormatter.string(from: NSNumber(value: self)) ?? "$" + String(format: "%.2f", self)
    }

    private static let priceFormatter: NumberFormatter = {
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.locale = Locale(identifier: "en_US")
        return formatter
    }()
}
