import SwiftUI

/// App color scheme matching Android design
struct AppColors {

    // MARK: - Primary Colors
    static let primaryLight = Color(hex: "EB6A43")  // Coral/Orange for main elements
    static let primaryDark = Color(hex: "FF8A65")   // Lighter coral for dark theme
    static let secondaryLight = Color(hex: "FFC84D") // Yellow/Gold for accents
    static let secondaryDark = Color(hex: "FFD180")  // Lighter gold for dark theme

    // MARK: - Background Colors
    static let backgroundLight = Color(hex: "FAFAFA") // Light gray background
    static let backgroundDark = Color(hex: "121212")  // Dark background
    static let surfaceLight = Color(hex: "FFFFFF")    // White surface/cards
    static let surfaceDark = Color(hex: "1E1E1E")     // Dark surface/cards

    // MARK: - Text Colors
    static let textPrimaryLight = Color(hex: "000000") // Black text
    static let textPrimaryDark = Color(hex: "FFFFFF")  // White text
    static let textSecondaryLight = Color(hex: "8E8E93") // Gray secondary text
    static let textSecondaryDark = Color(hex: "B0B0B0")  // Light gray secondary text

    // MARK: - Utility Colors
    static let success = Color(hex: "34C759")    // Green
    static let error = Color(hex: "FF3B30")      // Red
    static let warning = Color(hex: "FF9500")    // Orange for stars
    static let gray = Color(hex: "8E8E93")       // Secondary text
    static let lightGray = Color(hex: "E5E5EA")  // Backgrounds
    static let chipBackground = Color(hex: "9C5C38") // Brown for category chips

    // MARK: - Dynamic Colors (controlled by app theme preference)
    static func primary(isDarkMode: Bool) -> Color {
        isDarkMode ? primaryDark : primaryLight
    }

    static func secondary(isDarkMode: Bool) -> Color {
        isDarkMode ? secondaryDark : secondaryLight
    }

    static func background(isDarkMode: Bool) -> Color {
        isDarkMode ? backgroundDark : backgroundLight
    }

    static func surface(isDarkMode: Bool) -> Color {
        isDarkMode ? surfaceDark : surfaceLight
    }

    static func textPrimary(isDarkMode: Bool) -> Color {
        isDarkMode ? textPrimaryDark : textPrimaryLight
    }

    static func textSecondary(isDarkMode: Bool) -> Color {
        isDarkMode ? textSecondaryDark : textSecondaryLight
    }

    // MARK: - Legacy Dynamic Colors (for backward compatibility with system theme)
    static var primary: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark ? UIColor(primaryDark) : UIColor(primaryLight)
        })
    }

    static var secondary: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark ? UIColor(secondaryDark) : UIColor(secondaryLight)
        })
    }

    static var background: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark ? UIColor(backgroundDark) : UIColor(backgroundLight)
        })
    }

    static var surface: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark ? UIColor(surfaceDark) : UIColor(surfaceLight)
        })
    }

    static var textPrimary: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark ? UIColor(textPrimaryDark) : UIColor(textPrimaryLight)
        })
    }

    static var textSecondary: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark ? UIColor(textSecondaryDark) : UIColor(textSecondaryLight)
        })
    }
}

// MARK: - Color Extension for Hex Support
extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (1, 1, 1, 0)
        }

        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue:  Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}

