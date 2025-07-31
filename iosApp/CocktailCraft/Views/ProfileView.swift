import SwiftUI
import shared

struct ProfileView: View {
    @StateObject private var viewModel = ProfileViewModelSKIE()
    @ObservedObject private var themeViewModel = ThemeViewModelSKIE.shared
    @State private var showingSettings = false
    @State private var showingSignIn = false
    @State private var showingSignUp = false
    @State private var showingLogoutAlert = false
    @State private var showingOfflineMode = false
    @Environment(\.dismiss) private var dismiss
    @Environment(\.isDarkMode) var isDarkMode

    // Optional binding for tab selection to enable navigation to Orders tab
    var selectedTab: Binding<Int>?

    init(selectedTab: Binding<Int>? = nil) {
        self.selectedTab = selectedTab
    }

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 16) {
                    // Profile Header Card
                    profileHeaderCard

                    // Account Settings Card (only if logged in)
                    if viewModel.isLoggedIn {
                        accountSettingsCard
                    }

                    // App Settings Card
                    appSettingsCard

                    // About Card
                    aboutCard
                }
                .padding(.horizontal)
                .padding(.top, 8)
                .padding(.bottom)
            }
            .background(AppColors.background(isDarkMode: isDarkMode))
            .navigationTitle("Profile")
            .navigationBarTitleDisplayMode(.large)
            .onAppear {
                viewModel.refresh()
            }
        }
        .sheet(isPresented: $showingSettings) {
            SettingsView()
        }
        .sheet(isPresented: $showingSignIn) {
            SignInView(
                onDismiss: { showingSignIn = false },
                onSignIn: { email, password in
                    Task {
                        let success = await viewModel.signIn(email: email, password: password)
                        if success {
                            viewModel.refresh()
                            showingSignIn = false
                        }
                    }
                }
            )
        }
        .sheet(isPresented: $showingSignUp) {
            SignUpView(
                onDismiss: { showingSignUp = false },
                onSignUp: { name, email, password in
                    Task {
                        let success = await viewModel.signUp(name: name, email: email, password: password)
                        if success {
                            viewModel.refresh()
                            showingSignUp = false
                        }
                    }
                }
            )
        }
        .sheet(isPresented: $showingOfflineMode) {
            OfflineModeView()
        }
        .alert("Logout", isPresented: $showingLogoutAlert) {
            Button("Cancel", role: .cancel) { }
            Button("Logout", role: .destructive) {
                Task {
                    await viewModel.signOut()
                }
            }
        } message: {
            Text("Are you sure you want to logout?")
        }
    }

    // MARK: - Profile Header Card
    private var profileHeaderCard: some View {
        VStack(spacing: 16) {
            // Profile Picture
            ZStack {
                Circle()
                    .fill(AppColors.primary.opacity(0.2))
                    .frame(width: 80, height: 80)

                Text(viewModel.userName.prefix(1).uppercased())
                    .font(.largeTitle)
                    .fontWeight(.bold)
                    .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))
            }

            // User Info
            VStack(spacing: 4) {
                Text(viewModel.userName)
                    .font(.title2)
                    .fontWeight(.bold)
                    .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

                Text(viewModel.userEmail)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }

            // Sign In/Sign Up Buttons (if not logged in)
            if !viewModel.isLoggedIn {
                VStack(spacing: 12) {
                    Text("Sign in to access your profile")
                        .font(.body)
                        .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                        .padding(.top, 8)

                    Button("Sign In") {
                        showingSignIn = true
                    }
                    .buttonStyle(PrimaryButtonStyle())

                    Button("Create Account") {
                        showingSignUp = true
                    }
                    .buttonStyle(SecondaryButtonStyle())
                }
            }
        }
        .padding()
        .cardStyle()
    }

    // MARK: - Account Settings Card
    private var accountSettingsCard: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Account Settings")
                .font(.headline)
                .fontWeight(.bold)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

            VStack(spacing: 0) {
                SettingsRow(
                    icon: "person",
                    title: "Edit Profile",
                    action: { /* Handle edit profile */ }
                )

                Divider()

                SettingsRow(
                    icon: "lock",
                    title: "Change Password",
                    action: { /* Handle change password */ }
                )

                Divider()

                SettingsRow(
                    icon: "envelope",
                    title: "Email Preferences",
                    action: { /* Handle email preferences */ }
                )

                Divider()

                SettingsRow(
                    icon: "bell",
                    title: "Notification Settings",
                    action: { /* Handle notification settings */ }
                )
            }
        }
        .padding()
        .cardStyle()
    }

    // MARK: - App Settings Card
    private var appSettingsCard: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("App Settings")
                .font(.headline)
                .fontWeight(.bold)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

            VStack(spacing: 0) {
                SettingsRow(
                    icon: "list.bullet",
                    title: "Order History",
                    action: {
                        // Navigate to Orders tab
                        selectedTab?.wrappedValue = 3
                    }
                )

                Divider()

                SettingsRow(
                    icon: "questionmark.circle",
                    title: "Help & Support",
                    action: { /* Handle help & support */ }
                )

                Divider()

                SettingsRow(
                    icon: "wifi.slash",
                    title: "Offline Mode",
                    action: { showingOfflineMode = true }
                )

                Divider()

                // Follow System Theme Toggle
                ThemeToggleRow(
                    title: "Follow System Theme",
                    subtitle: themeViewModel.isSystemTheme ? "On" : "Off",
                    icon: "gear",
                    isChecked: themeViewModel.isSystemTheme,
                    onToggle: {
                        Task {
                            await themeViewModel.applySystemTheme()
                        }
                    }
                )

                Divider()

                // Dark Mode Toggle
                ThemeToggleRow(
                    title: "Dark Mode",
                    subtitle: themeViewModel.isSystemTheme ? "Controlled by system" : (themeViewModel.isDarkMode ? "On" : "Off"),
                    icon: themeViewModel.isDarkMode ? "moon.fill" : "sun.max.fill",
                    isChecked: themeViewModel.isDarkMode,
                    onToggle: {
                        if !themeViewModel.isSystemTheme {
                            Task {
                                await themeViewModel.toggleDarkMode()
                            }
                        }
                    },
                    enabled: !themeViewModel.isSystemTheme
                )

                // Logout (only if logged in)
                if viewModel.isLoggedIn {
                    Divider()

                    SettingsRow(
                        icon: "arrow.right.square",
                        title: "Logout",
                        action: { showingLogoutAlert = true },
                        textColor: .red
                    )
                }
            }
        }
        .padding()
        .cardStyle()
    }

    // MARK: - About Card
    private var aboutCard: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("About")
                .font(.headline)
                .fontWeight(.bold)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

            VStack(alignment: .leading, spacing: 12) {
                Text("Cocktail Bar App")
                    .font(.body)
                    .fontWeight(.medium)
                    .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

                Text("Version 1.0.0")
                    .font(.subheadline)
                    .foregroundColor(.secondary)

                Text("© 2023 Cocktail Bar. All rights reserved.")
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .padding(.top, 8)
            }
        }
        .padding()
        .cardStyle()
    }
}

// MARK: - Supporting Views

struct SettingsRow: View {
    let icon: String
    let title: String
    let action: () -> Void
    let textColor: Color

    init(icon: String, title: String, action: @escaping () -> Void, textColor: Color = .primary) {
        self.icon = icon
        self.title = title
        self.action = action
        self.textColor = textColor
    }

    var body: some View {
        Button(action: action) {
            HStack(spacing: 16) {
                Image(systemName: icon)
                    .foregroundColor(textColor)
                    .frame(width: 24, height: 24)

                Text(title)
                    .font(.body)
                    .foregroundColor(textColor)

                Spacer()

                Image(systemName: "chevron.right")
                    .foregroundColor(.gray)
                    .font(.caption)
            }
            .padding(.vertical, 12)
        }
        .buttonStyle(PlainButtonStyle())
    }
}

struct ThemeToggleRow: View {
    let title: String
    let subtitle: String
    let icon: String
    let isChecked: Bool
    let onToggle: () -> Void
    let enabled: Bool

    init(title: String, subtitle: String, icon: String, isChecked: Bool, onToggle: @escaping () -> Void, enabled: Bool = true) {
        self.title = title
        self.subtitle = subtitle
        self.icon = icon
        self.isChecked = isChecked
        self.onToggle = onToggle
        self.enabled = enabled
    }

    var body: some View {
        HStack(spacing: 16) {
            Image(systemName: icon)
                .foregroundColor(enabled ? .primary : .gray)
                .frame(width: 24, height: 24)
                .scaleEffect(enabled ? 1.0 : 0.8)
                .animation(AppTheme.Animation.quick, value: enabled)

            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.body)
                    .foregroundColor(enabled ? .primary : .gray)

                Text(subtitle)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            .opacity(enabled ? 1.0 : 0.6)
            .animation(AppTheme.Animation.standard, value: enabled)

            Spacer()

            Toggle("", isOn: Binding(
                get: { isChecked },
                set: { _ in onToggle() }
            ))
            .disabled(!enabled)
            .opacity(enabled ? 1.0 : 0.5)
        }
        .padding(.vertical, 12)
    }
}



struct PrimaryButtonStyle: ButtonStyle {
    @Environment(\.isDarkMode) var isDarkMode

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.headline)
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .padding()
            .background(AppColors.primary(isDarkMode: isDarkMode))
            .cornerRadius(8)
            .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
    }
}

struct SecondaryButtonStyle: ButtonStyle {
    @Environment(\.isDarkMode) var isDarkMode

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.headline)
            .foregroundColor(AppColors.primary(isDarkMode: isDarkMode))
            .frame(maxWidth: .infinity)
            .padding()
            .background(Color.clear)
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(AppColors.primary(isDarkMode: isDarkMode), lineWidth: 1)
            )
            .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
    }
}