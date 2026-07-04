import SwiftUI
import shared

/**
 * iOS ViewModel wrapper for SharedThemeViewModel using pure SKIE integration.
 * State/error mirroring and observation-task lifecycle live in
 * SharedViewModelWrapper.
 */
final class ThemeViewModelSKIE: SharedViewModelWrapper<ThemeUiState> {
    // Singleton instance
    static let shared = ThemeViewModelSKIE()

    // Computed properties
    var currentThemeName: String {
        sharedViewModel.currentThemeName
    }

    var availableThemes: [String] {
        sharedViewModel.availableThemes
    }

    var availableAccentColors: [String] {
        sharedViewModel.availableAccentColors
    }

    var availableFontSizes: [String] {
        sharedViewModel.availableFontSizes
    }

    // Shared ViewModel instance
    private let sharedViewModel: SharedThemeViewModel

    private init() {
        let viewModel = getSharedKoinHelper().getSharedThemeViewModel()
        self.sharedViewModel = viewModel
        super.init(uiState: viewModel.uiState, errorFlow: viewModel.error)
    }

    // No deinit: the base class cancels observation. Wraps a Koin `single`
    // whose coroutine scope must survive any one wrapper — never onCleared().

    // MARK: - Public Methods (using SKIE async/await)

    func setThemeMode(_ mode: String) async {
        do {
            try await sharedViewModel.setThemeMode(mode: mode)
        } catch {
            // Handle error silently
        }
    }

    func setDarkMode(_ enabled: Bool) async {
        do {
            try await sharedViewModel.setDarkMode(enabled: enabled)
        } catch {
            // Handle error silently
        }
    }

    func setAccentColor(_ color: String) async {
        do {
            try await sharedViewModel.setAccentColor(color: color)
        } catch {
            // Handle error silently
        }
    }

    func setFontSize(_ size: String) async {
        do {
            try await sharedViewModel.setFontSize(size: size)
        } catch {
            // Handle error silently
        }
    }

    func toggleHighContrast() async {
        do {
            try await sharedViewModel.toggleHighContrast()
        } catch {
            // Handle error silently
        }
    }

    func toggleReducedMotion() async {
        do {
            try await sharedViewModel.toggleReducedMotion()
        } catch {
            // Handle error silently
        }
    }

    func resetToDefaults() async {
        do {
            try await sharedViewModel.resetToDefaults()
        } catch {
            // Handle error silently
        }
    }

    func setFollowSystemTheme(_ enabled: Bool) async {
        do {
            try await sharedViewModel.setFollowSystemTheme(enabled: enabled)
        } catch {
            // Handle error silently
        }
    }

    // MARK: - Synchronous Methods

    func isValidThemeMode(_ mode: String) -> Bool {
        return sharedViewModel.isValidThemeMode(mode: mode)
    }

    func isValidAccentColor(_ color: String) -> Bool {
        return sharedViewModel.isValidAccentColor(color: color)
    }

    func isValidFontSize(_ size: String) -> Bool {
        return sharedViewModel.isValidFontSize(size: size)
    }

    func getThemeModeDisplayName(_ mode: String) -> String {
        return sharedViewModel.getThemeModeDisplayName(mode: mode)
    }

    func getAccentColorDisplayName(_ color: String) -> String {
        return sharedViewModel.getAccentColorDisplayName(color: color)
    }

    func getFontSizeDisplayName(_ size: String) -> String {
        return sharedViewModel.getFontSizeDisplayName(size: size)
    }

    func isCustomTheme() -> Bool {
        return sharedViewModel.isCustomTheme()
    }

    func getThemePreviewColors(_ mode: String) -> [String: String] {
        return sharedViewModel.getThemePreviewColors(mode: mode)
    }

    func systemSupportsDarkMode() -> Bool {
        return sharedViewModel.systemSupportsDarkMode()
    }

    func clearError() {
        sharedViewModel.clearError()
    }

    func refreshThemeSettings() {
        sharedViewModel.refreshThemeSettings()
    }

    func exportThemeSettings() -> [String: Any] {
        return sharedViewModel.exportThemeSettings()
    }

    func importThemeSettings(_ settings: [String: Any]) {
        sharedViewModel.importThemeSettings(settings: settings)
    }

    // MARK: - Helper Methods for SwiftUI

    func getSwiftUIColor(for colorName: String) -> Color {
        switch colorName.lowercased() {
        case "blue": return .blue
        case "green": return .green
        case "red": return .red
        case "orange": return .orange
        case "purple": return .purple
        case "pink": return .pink
        case "yellow": return .yellow
        default: return .blue
        }
    }

    func getFontSizeValue() -> CGFloat {
        switch state.fontSize.lowercased() {
        case "small": return 14.0
        case "medium": return 16.0
        case "large": return 18.0
        case "xlarge": return 20.0
        default: return 16.0
        }
    }

    func getThemeModeIcon() -> String {
        switch state.themeMode {
        case .light: return "sun.max"
        case .dark: return "moon"
        case .system: return "gear"
        default: return "gear"
        }
    }

    func shouldUseDarkMode() -> Bool {
        if state.isSystemTheme {
            return UITraitCollection.current.userInterfaceStyle == .dark
        }
        return state.isDarkMode
    }
}
