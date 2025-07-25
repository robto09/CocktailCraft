import SwiftUI
import shared

struct SettingsView: View {
    @Environment(\.presentationMode) var presentationMode
    @StateObject private var themeViewModel = ThemeViewModelSKIE()
    @AppStorage("notificationsEnabled") private var notificationsEnabled = true
    
    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Appearance")) {
                    Toggle("Dark Mode", isOn: Binding(
                        get: { themeViewModel.isDarkMode },
                        set: { _ in
                            Task {
                                await themeViewModel.toggleDarkMode()
                            }
                        }
                    ))
                    
                    Toggle("Follow System Theme", isOn: Binding(
                        get: { themeViewModel.isSystemTheme },
                        set: { _ in
                            Task {
                                await themeViewModel.applySystemTheme()
                            }
                        }
                    ))
                    
                    if !themeViewModel.availableAccentColors.isEmpty {
                        Picker("Accent Color", selection: Binding(
                            get: { themeViewModel.accentColor },
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
                        Picker("Font Size", selection: Binding(
                            get: { themeViewModel.fontSize },
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
                    
                    Toggle("High Contrast", isOn: Binding(
                        get: { themeViewModel.isHighContrast },
                        set: { _ in
                            Task {
                                await themeViewModel.toggleHighContrast()
                            }
                        }
                    ))
                    
                    Toggle("Reduce Motion", isOn: Binding(
                        get: { themeViewModel.isReducedMotion },
                        set: { _ in
                            Task {
                                await themeViewModel.toggleReducedMotion()
                            }
                        }
                    ))
                }
                
                Section(header: Text("Notifications")) {
                    Toggle("Enable Notifications", isOn: $notificationsEnabled)
                }
                
                Section(header: Text("Theme Actions")) {
                    Button("Reset to Defaults") {
                        Task {
                            await themeViewModel.resetToDefaults()
                        }
                    }
                    .foregroundColor(.red)
                }
                
                Section(header: Text("About")) {
                    HStack {
                        Text("Version")
                        Spacer()
                        Text("1.0.0")
                            .foregroundColor(.secondary)
                    }
                    
                    HStack {
                        Text("Current Theme")
                        Spacer()
                        Text(themeViewModel.currentThemeName)
                            .foregroundColor(.secondary)
                    }
                }
            }
            .navigationTitle("Settings")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") {
                        presentationMode.wrappedValue.dismiss()
                    }
                }
            }
        }
    }
}