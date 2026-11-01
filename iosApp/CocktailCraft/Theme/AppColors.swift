import SwiftUI
import shared

/// App color scheme, mapped 1:1 from the cross-platform design tokens in
/// core/designsystem/src/commonMain/kotlin/com/cocktailcraft/designsystem/DesignTokens.kt
/// (ColorTokens) — the single source of truth for BOTH iOS and Android, so the
/// palettes can never drift.
///
/// The widget extension cannot link the shared framework, so it keeps its own
/// mirror of the two brand colors in BrandColorComponents
/// (Utils/WidgetBridge.swift) — update that alongside any brand color change
/// in the shared tokens.
struct AppColors {

    // MARK: - Primary Colors
    static let primaryLight = Color(token: ColorTokens.shared.primaryLight)   // Coral/Orange for main elements
    static let primaryDark = Color(token: ColorTokens.shared.primaryDark)     // Lighter coral for dark theme
    static let secondaryLight = Color(token: ColorTokens.shared.secondaryLight) // Yellow/Gold for accents
    static let secondaryDark = Color(token: ColorTokens.shared.secondaryDark)   // Lighter gold for dark theme

    // MARK: - Background Colors
    static let backgroundLight = Color(token: ColorTokens.shared.backgroundLight) // Light gray background
    static let backgroundDark = Color(token: ColorTokens.shared.backgroundDark)   // Dark background
    static let surfaceLight = Color(token: ColorTokens.shared.surfaceLight)       // White surface/cards
    static let surfaceDark = Color(token: ColorTokens.shared.surfaceDark)         // Dark surface/cards

    // MARK: - Text Colors
    static let textPrimaryLight = Color(token: ColorTokens.shared.textPrimaryLight) // Black text
    static let textPrimaryDark = Color(token: ColorTokens.shared.textPrimaryDark)   // White text
    static let textSecondaryLight = Color(token: ColorTokens.shared.textSecondaryLight) // Gray secondary text
    static let textSecondaryDark = Color(token: ColorTokens.shared.textSecondaryDark)   // Light gray secondary text

    // MARK: - Utility Colors
    // Semantic statuses come from the shared tokens: one green/red/amber per
    // meaning on both platforms. (success was previously a coral that
    // duplicated primaryDark — that was a bug, fixed by the token migration.)
    static let success = Color(token: ColorTokens.shared.success)   // Green
    static let error = Color(token: ColorTokens.shared.error)       // Red
    static let warning = Color(token: ColorTokens.shared.warning)   // Orange for stars
    static let gray = Color(token: ColorTokens.shared.gray)         // Secondary text
    static let darkGray = Color(token: ColorTokens.shared.darkGray) // Stronger secondary text
    static let lightGray = Color(token: ColorTokens.shared.lightGray) // Backgrounds
    static let subtleDark = Color(token: ColorTokens.shared.subtleDark) // lightGray's role on dark
    static let chipBackground = Color(token: ColorTokens.shared.chipBackground) // Brown for category chips
    static let surfaceRaisedLight = Color(token: ColorTokens.shared.surfaceRaisedLight)
    static let surfaceRaisedDark = Color(token: ColorTokens.shared.surfaceRaisedDark)

    // MARK: - Dynamic Colors (controlled by app theme preference)
    // Colors that depend on the accent or high-contrast settings take the
    // whole AppAppearance (@Environment(\.appearance)); the rest only need
    // isDarkMode (@Environment(\.isDarkMode)).

    // Primary follows the user-selected accent ("coral" = brand default)
    static func primary(_ appearance: AppAppearance) -> Color {
        primary(isDarkMode: appearance.isDarkMode, accentName: appearance.accentName)
    }

    /// The accent's light or dark variant regardless of the theme — e.g. the
    /// theme switch's thumb, which previews the mode it toggles to.
    static func primary(isDarkMode: Bool, accentName: String) -> Color {
        Color(token: isDarkMode
            ? AccentColorTokens.shared.dark(name: accentName)
            : AccentColorTokens.shared.light(name: accentName))
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

    /// Content on a primary fill: white on the light coral, black on the
    /// lighter dark-theme coral (white there is ~2.3:1), matching Android's
    /// onPrimary.
    static func onPrimary(isDarkMode: Bool) -> Color {
        isDarkMode ? .black : .white
    }

    /// Navigation bar background: brand primary in light, surface in dark,
    /// matching Android's extendedColors.topBar.
    static func topBar(_ appearance: AppAppearance) -> Color {
        appearance.isDarkMode
            ? surfaceDark
            : primary(isDarkMode: false, accentName: appearance.accentName)
    }

    /// Controls raised on a surface (fields, unselected chips).
    static func surfaceRaised(isDarkMode: Bool) -> Color {
        isDarkMode ? surfaceRaisedDark : surfaceRaisedLight
    }

    /// Dividers, unfocused borders, empty segments: lightGray, or its dark
    /// counterpart so it doesn't glare on dark surfaces.
    static func neutralSubtle(isDarkMode: Bool) -> Color {
        isDarkMode ? subtleDark : lightGray
    }

    /// Fill of the detail-screen attribute chips: the shared chip color at
    /// 20%, as Android's tertiary.copy(alpha = 0.2f).
    static func chipFill(isDarkMode: Bool) -> Color {
        Color(token: isDarkMode ? ColorTokens.shared.chipBackgroundDark : ColorTokens.shared.chipBackground)
            .opacity(0.2)
    }

    static func textPrimary(isDarkMode: Bool) -> Color {
        isDarkMode ? textPrimaryDark : textPrimaryLight
    }

    // High contrast pushes secondary text further from the background,
    // matching Android's AppColors.TextSecondary behavior.
    static func textSecondary(_ appearance: AppAppearance) -> Color {
        switch (appearance.isHighContrast, appearance.isDarkMode) {
        case (true, true): return lightGray
        case (true, false): return darkGray
        case (false, true): return textSecondaryDark
        case (false, false): return textSecondaryLight
        }
    }

    // The old trait-collection-driven static vars (primary, background, ...)
    // are gone on purpose: they followed the SYSTEM appearance while the
    // rest of the app follows the app-controlled theme
    // (ThemeViewModelSKIE), so any call site could visually desync from
    // the chosen theme. Always use the functions above with
    // @Environment(\.isDarkMode) / @Environment(\.appearance). The accent
    // and high-contrast statics went the same way: written during
    // ContentView's body, they forced an .id() rebuild of the whole tree
    // (losing navigation state) on every change.
}

// MARK: - Color Extensions

extension Color {
    /// Builds a Color from a shared-framework design token (packed ARGB,
    /// 0xAARRGGBB, as a Kotlin Long / Swift Int64).
    init(token: Int64) {
        let argb = UInt64(bitPattern: token)
        self.init(
            .sRGB,
            red: Double((argb >> 16) & 0xFF) / 255,
            green: Double((argb >> 8) & 0xFF) / 255,
            blue: Double(argb & 0xFF) / 255,
            opacity: Double((argb >> 24) & 0xFF) / 255
        )
    }

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
