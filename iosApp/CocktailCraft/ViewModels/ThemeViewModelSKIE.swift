import SwiftUI
import shared

/**
 * iOS ViewModel wrapper for SharedThemeViewModel using pure SKIE integration.
 * State/error mirroring and observation-task lifecycle live in
 * SharedViewModelWrapper; views read `state.<field>` (including the derived
 * `state.isDarkMode` / `state.isSystemTheme` / `state.themeModeDisplayName`).
 */
final class ThemeViewModelSKIE: SharedViewModelWrapper<ThemeUiState> {
    // Singleton instance
    static let shared = ThemeViewModelSKIE()

    // Constant option lists (not state) from the shared ViewModel
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
        super.init(viewModel: viewModel, uiState: viewModel.uiState)
    }

    // No deinit: the base class cancels observation. Wraps a Koin `single`
    // whose coroutine scope must survive any one wrapper — never onCleared().

    // MARK: - Public Methods

    // Value-based setters only: a re-fired binding re-asserts, never flips.
    // Each applies at once and saves on the shared ViewModel's scope.

    func setDarkMode(_ enabled: Bool) {
        sharedViewModel.setDarkMode(enabled: enabled)
    }

    func setAccentColor(_ color: String) {
        sharedViewModel.setAccentColor(color: color)
    }

    func setFontSize(_ size: String) {
        sharedViewModel.setFontSize(size: size)
    }

    func setHighContrast(_ enabled: Bool) {
        sharedViewModel.setHighContrast(enabled: enabled)
    }

    func setReducedMotion(_ enabled: Bool) {
        sharedViewModel.setReducedMotion(enabled: enabled)
    }

    func resetToDefaults() {
        sharedViewModel.resetToDefaults()
    }

    func setFollowSystemTheme(_ enabled: Bool) {
        sharedViewModel.setFollowSystemTheme(enabled: enabled)
    }

    // MARK: - Synchronous Methods

    func getAccentColorDisplayName(_ color: String) -> String {
        return sharedViewModel.getAccentColorDisplayName(color: color).localized()
    }

    func getFontSizeDisplayName(_ size: String) -> String {
        return sharedViewModel.getFontSizeDisplayName(size: size).localized()
    }
}
