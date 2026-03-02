import SwiftUI
import shared
import Combine

/**
 * iOS ViewModel wrapper for SharedThemeViewModel using pure SKIE integration.
 * No FlowCollector bridge needed - uses native Swift async/await.
 */
@MainActor
class ThemeViewModelSKIE: ObservableObject {
    // Singleton instance
    static let shared = ThemeViewModelSKIE()

    // Published properties for SwiftUI
    @Published var isDarkMode = false
    @Published var themeMode = "system"
    @Published var isSystemTheme = true
    @Published var accentColor = "blue"
    @Published var fontSize = "medium"
    @Published var isHighContrast = false
    @Published var isReducedMotion = false
    @Published var isLoading = false
    @Published var error: ErrorHandler.UserFriendlyError? = nil
    
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
    
    // Tasks for async observation
    private var observationTasks: [Task<Void, Never>] = []
    
    private init() {
        // Get shared ViewModel from Koin
        self.sharedViewModel = getSharedKoinHelper().getSharedThemeViewModel()
        
        // Start observing StateFlows using SKIE async/await
        startObserving()
    }
    
    deinit {
        // Cancel all observation tasks
        observationTasks.forEach { $0.cancel() }
        // Note: Do NOT call onCleared() — this is a Koin singleton whose
        // coroutine scope must survive the lifetime of any single wrapper.
    }
    
    // MARK: - SKIE StateFlow Observation
    
    private func startObserving() {
        // Single consolidated observation of uiState
        observationTasks.append(Task {
            for await state in sharedViewModel.uiState {
                await MainActor.run {
                    self.isDarkMode = state.isDarkMode
                    self.themeMode = state.themeMode
                    self.isSystemTheme = state.isSystemTheme
                    self.accentColor = state.accentColor
                    self.fontSize = state.fontSize
                    self.isHighContrast = state.isHighContrast
                    self.isReducedMotion = state.isReducedMotion
                    self.isLoading = state.isLoading
                }
            }
        })

        // Observe error from base class
        observationTasks.append(Task {
            for await errorValue in sharedViewModel.error {
                await MainActor.run {
                    self.error = errorValue
                }
            }
        })
    }
    
    // MARK: - Public Methods (using SKIE async/await)
    
    func setThemeMode(_ mode: String) async {
        do {
            try await sharedViewModel.setThemeMode(mode: mode)
        } catch {
            // Handle error silently
        }
    }
    
    func toggleDarkMode() async {
        do {
            try await sharedViewModel.toggleDarkMode()
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
    
    func applySystemTheme() async {
        do {
            try await sharedViewModel.applySystemTheme()
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
        switch fontSize.lowercased() {
        case "small": return 14.0
        case "medium": return 16.0
        case "large": return 18.0
        case "xlarge": return 20.0
        default: return 16.0
        }
    }
    
    func getThemeModeIcon() -> String {
        switch themeMode.lowercased() {
        case "light": return "sun.max"
        case "dark": return "moon"
        case "system": return "gear"
        default: return "gear"
        }
    }
    
    func shouldUseDarkMode() -> Bool {
        if isSystemTheme {
            return UITraitCollection.current.userInterfaceStyle == .dark
        }
        return isDarkMode
    }
}