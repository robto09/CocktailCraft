import SwiftUI
import shared

struct AppTheme {
    // MARK: - Colors
    
    struct Colors {
        static let primary = Color.dynamicColor(
            light: Color(hex: 0x007AFF),
            dark: Color(hex: 0x0A84FF)
        )
        
        static let secondary = Color.dynamicColor(
            light: Color(hex: 0xFF9500),
            dark: Color(hex: 0xFF9F0A)
        )
        
        static let background = Color(.systemBackground)
        static let secondaryBackground = Color(.secondarySystemBackground)
        static let groupedBackground = Color(.systemGroupedBackground)
        
        static let text = Color(.label)
        static let secondaryText = Color(.secondaryLabel)
        
        static let success = Color.dynamicColor(
            light: Color(hex: 0x34C759),
            dark: Color(hex: 0x30D158)
        )
        
        static let error = Color.dynamicColor(
            light: Color(hex: 0xFF3B30),
            dark: Color(hex: 0xFF453A)
        )
        
        static let warning = Color.dynamicColor(
            light: Color(hex: 0xFF9500),
            dark: Color(hex: 0xFF9F0A)
        )
    }
    
    // MARK: - Typography
    
    struct Typography {
        static let largeTitle = Font.largeTitle
        static let title = Font.title
        static let title2 = Font.title2
        static let title3 = Font.title3
        static let headline = Font.headline
        static let subheadline = Font.subheadline
        static let body = Font.body
        static let callout = Font.callout
        static let caption = Font.caption
        static let caption2 = Font.caption2
    }
    
    // MARK: - Spacing
    
    struct Spacing {
        static let xxSmall: CGFloat = 2
        static let xSmall: CGFloat = 4
        static let small: CGFloat = 8
        static let medium: CGFloat = 16
        static let large: CGFloat = 24
        static let xLarge: CGFloat = 32
        static let xxLarge: CGFloat = 40
    }
    
    // MARK: - Layout
    
    struct Layout {
        static let cornerRadius: CGFloat = 12
        static let buttonHeight: CGFloat = 50
        static let iconSize: CGFloat = 24
        static let avatarSize: CGFloat = 40
        static let maxWidth: CGFloat = 414 // iPhone Pro Max width
    }
    
    // MARK: - Animations
    
    struct Animation {
        static let `default` = SwiftUI.Animation.easeInOut(duration: 0.3)
        static let fast = SwiftUI.Animation.easeInOut(duration: 0.2)
        static let slow = SwiftUI.Animation.easeInOut(duration: 0.4)
    }
}

// MARK: - View Modifiers

struct PrimaryButtonStyle: ViewModifier {
    func body(content: Content) -> some View {
        content
            .font(.headline)
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .frame(height: AppTheme.Layout.buttonHeight)
            .background(AppTheme.Colors.primary)
            .cornerRadius(AppTheme.Layout.cornerRadius)
    }
}

struct SecondaryButtonStyle: ViewModifier {
    func body(content: Content) -> some View {
        content
            .font(.headline)
            .foregroundColor(AppTheme.Colors.primary)
            .frame(maxWidth: .infinity)
            .frame(height: AppTheme.Layout.buttonHeight)
            .background(AppTheme.Colors.primary.opacity(0.1))
            .cornerRadius(AppTheme.Layout.cornerRadius)
    }
}

struct CardStyle: ViewModifier {
    func body(content: Content) -> some View {
        content
            .padding()
            .background(AppTheme.Colors.background)
            .cornerRadius(AppTheme.Layout.cornerRadius)
            .shadow(radius: 4)
    }
}

// MARK: - View Extensions

extension View {
    func primaryButtonStyle() -> some View {
        modifier(PrimaryButtonStyle())
    }
    
    func secondaryButtonStyle() -> some View {
        modifier(SecondaryButtonStyle())
    }
    
    func cardStyle() -> some View {
        modifier(CardStyle())
    }
}

// MARK: - Color Helpers

extension Color {
    init(hex: UInt, alpha: Double = 1.0) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xff) / 255,
            green: Double((hex >> 08) & 0xff) / 255,
            blue: Double((hex >> 00) & 0xff) / 255,
            opacity: alpha
        )
    }
    
    static func dynamicColor(light: Color, dark: Color) -> Color {
        return Color(UIColor { traitCollection in
            switch traitCollection.userInterfaceStyle {
            case .light:
                return UIColor(light)
            case .dark:
                return UIColor(dark)
            @unknown default:
                return UIColor(light)
            }
        })
    }
}

// MARK: - Preview Helpers

struct ThemePreview: PreviewProvider {
    static var previews: some View {
        Group {
            VStack(spacing: AppTheme.Spacing.medium) {
                Text("Heading")
                    .font(AppTheme.Typography.title)
                Text("Subheading")
                    .font(AppTheme.Typography.headline)
                Text("Body text")
                    .font(AppTheme.Typography.body)
                
                Button("Primary Button") {}
                    .primaryButtonStyle()
                
                Button("Secondary Button") {}
                    .secondaryButtonStyle()
                
                VStack {
                    Text("Card Content")
                }
                .cardStyle()
            }
            .padding()
            .previewDisplayName("Light Mode")
            
            VStack(spacing: AppTheme.Spacing.medium) {
                Text("Heading")
                    .font(AppTheme.Typography.title)
                Text("Subheading")
                    .font(AppTheme.Typography.headline)
                Text("Body text")
                    .font(AppTheme.Typography.body)
                
                Button("Primary Button") {}
                    .primaryButtonStyle()
                
                Button("Secondary Button") {}
                    .secondaryButtonStyle()
                
                VStack {
                    Text("Card Content")
                }
                .cardStyle()
            }
            .padding()
            .preferredColorScheme(.dark)
            .previewDisplayName("Dark Mode")
        }
    }
}