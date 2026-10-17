import SwiftUI
import shared

struct SettingsView: View {
    @Environment(\.dismiss) private var dismiss
    private let themeViewModel = ThemeViewModelSKIE.shared
    @AppStorage("notificationsEnabled") private var notificationsEnabled = true

    var body: some View {
        NavigationStack {
            Form {
                Section(header: Text(L(\.profile_appearance))) {
                    Toggle(L(\.dark_mode), isOn: Binding(
                        get: { themeViewModel.state.isDarkMode },
                        set: { enabled in
                            Task {
                                await themeViewModel.setDarkMode(enabled)
                            }
                        }
                    ))
                    
                    Toggle(L(\.follow_system_theme), isOn: Binding(
                        get: { themeViewModel.state.isSystemTheme },
                        set: { enabled in
                            Task {
                                await themeViewModel.setFollowSystemTheme(enabled)
                            }
                        }
                    ))
                    
                    if !themeViewModel.availableAccentColors.isEmpty {
                        Picker(L(\.accent_color), selection: Binding(
                            get: { themeViewModel.state.accentColor },
                            set: { newColor in
                                Task {
                                    await themeViewModel.setAccentColor(newColor)
                                }
                            }
                        )) {
                            ForEach(themeViewModel.availableAccentColors, id: \.self) { color in
                                Text(themeViewModel.getAccentColorDisplayName(color))
                                    .tag(color)
                            }
                        }
                    }
                    
                    if !themeViewModel.availableFontSizes.isEmpty {
                        Picker(L(\.font_size), selection: Binding(
                            get: { themeViewModel.state.fontSize },
                            set: { newSize in
                                Task {
                                    await themeViewModel.setFontSize(newSize)
                                }
                            }
                        )) {
                            ForEach(themeViewModel.availableFontSizes, id: \.self) { size in
                                Text(themeViewModel.getFontSizeDisplayName(size))
                                    .tag(size)
                            }
                        }
                    }
                    
                    Toggle(L(\.high_contrast), isOn: Binding(
                        get: { themeViewModel.state.isHighContrast },
                        set: { enabled in
                            Task {
                                await themeViewModel.setHighContrast(enabled)
                            }
                        }
                    ))
                    
                    Toggle(L(\.reduced_motion), isOn: Binding(
                        get: { themeViewModel.state.isReducedMotion },
                        set: { enabled in
                            Task {
                                await themeViewModel.setReducedMotion(enabled)
                            }
                        }
                    ))
                }
                
                Section(header: Text(L(\.notifications))) {
                    Toggle(L(\.settings_enable_notifications), isOn: $notificationsEnabled)
                }
                
                Section(header: Text(L(\.settings_theme_actions))) {
                    Button(L(\.settings_reset_to_defaults)) {
                        Task {
                            await themeViewModel.resetToDefaults()
                        }
                    }
                    .foregroundColor(.red)
                }
                
                Section(header: Text(L(\.profile_about))) {
                    HStack {
                        Text(L(\.settings_version))
                        Spacer()
                        Text("1.0.0")
                            .foregroundColor(.secondary)
                    }
                    
                    HStack {
                        Text(L(\.settings_current_theme))
                        Spacer()
                        Text(themeViewModel.state.themeModeDisplayName.localized())
                            .foregroundColor(.secondary)
                    }
                }
            }
            .brandedNavigationBar(title: L(\.settings))
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(L(\.common_done)) {
                        dismiss()
                    }
                    .tint(.white)
                }
            }
        }
    }
}