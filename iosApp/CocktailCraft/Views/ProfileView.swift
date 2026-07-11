import SwiftUI
import shared

struct ProfileView: View {
    @State private var viewModel = ProfileViewModelSKIE()
    private let themeViewModel = ThemeViewModelSKIE.shared
    @State private var showingSettings = false
    @State private var showingEditProfile = false
    @State private var showingChangePassword = false
    @State private var showingSignIn = false
    @State private var showingSignUp = false
    @State private var showingLogoutAlert = false
    @State private var showingOfflineMode = false
    @Environment(\.dismiss) private var dismiss
    @Environment(\.isDarkMode) var isDarkMode

    // Optional binding for tab selection to enable navigation to Orders tab
    @Environment(AppRouter.self) private var router


    var body: some View {
        // No nav container here: ContentView already wraps this tab in a
        // NavigationStack (the old inner NavigationView double-nested it).
        VStack(spacing: 0) {
            // Brand-color header matching the Android TopAppBar
            HStack {
                Text("Profile")
                    .font(.title2.weight(.bold))
                    .foregroundColor(.white)
                Spacer()
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(AppColors.primary(isDarkMode: isDarkMode).ignoresSafeArea(edges: .top))

            ScrollView {
                VStack(spacing: 16) {
                    // Profile Header Card
                    profileHeaderCard

                    // Account Settings Card (only if logged in)
                    if viewModel.state.isLoggedIn {
                        accountSettingsCard
                    }

                    // App Settings Card
                    appSettingsCard

                    // About Card
                    aboutCard

                    // Bottom spacer to ensure content doesn't overlap with tab bar
                    Color.clear.frame(height: 80)
                }
                .padding(.horizontal)
                .padding(.top, 8)
            }
            .background(AppColors.background(isDarkMode: isDarkMode))
        }
        .toolbar(.hidden, for: .navigationBar)
        .onAppear {
            // Observation tracking keeps the UI current; the old
            // .id(UUID())-on-appear force-rebuild hack is gone.
            viewModel.refresh()
        }
        .sheet(isPresented: $showingSettings) {
            SettingsView()
        }
        .sheet(isPresented: $showingEditProfile) {
            EditProfileView(
                currentName: viewModel.userName,
                currentEmail: viewModel.userEmail,
                onDismiss: { showingEditProfile = false },
                onSave: { name, email in
                    let success = await viewModel.updateProfile(name: name, email: email)
                    if success {
                        viewModel.refresh()
                        showingEditProfile = false
                    }
                },
                isValidEmail: viewModel.isValidEmail
            )
        }
        .sheet(isPresented: $showingChangePassword) {
            ChangePasswordView(
                onDismiss: { showingChangePassword = false },
                onChangePassword: { current, new in
                    let success = await viewModel.changePassword(oldPassword: current, newPassword: new)
                    if success {
                        showingChangePassword = false
                    }
                },
                getPasswordStrength: viewModel.getPasswordStrength,
                meetsPasswordPolicy: viewModel.meetsPasswordPolicy
            )
        }
        .sheet(isPresented: $showingSignIn) {
            SignInView(
                onDismiss: { showingSignIn = false },
                onSignIn: { email, password in
                    let success = await viewModel.signIn(email: email, password: password)
                    if success {
                        viewModel.refresh()
                        showingSignIn = false
                    }
                }
            )
        }
        .sheet(isPresented: $showingSignUp) {
            SignUpView(
                onDismiss: { showingSignUp = false },
                onSignUp: { name, email, password in
                    let success = await viewModel.signUp(name: name, email: email, password: password)
                    if success {
                        viewModel.refresh()
                        showingSignUp = false
                    }
                },
                getPasswordStrength: viewModel.getPasswordStrength,
                meetsPasswordPolicy: viewModel.meetsPasswordPolicy
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
        .sharedErrorAlert(viewModel.error, clear: { viewModel.clearError() })
    }

    // MARK: - Profile Header Card
    private var profileHeaderCard: some View {
        VStack(spacing: 16) {
            // Profile Picture
            ZStack {
                Circle()
                    .fill(AppColors.primary(isDarkMode: isDarkMode).opacity(0.2))
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
            if !viewModel.state.isLoggedIn {
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
                // Inset like Android's 80%-width buttons
                .padding(.horizontal, 32)
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

            // Email Preferences / Notification Settings / Help & Support rows
            // were removed: they had no backing feature and tapped into
            // nothing. Add rows back only alongside a real implementation.
            VStack(spacing: 0) {
                SettingsRow(
                    icon: "person",
                    title: "Edit Profile",
                    action: { showingEditProfile = true }
                )

                Divider()

                SettingsRow(
                    icon: "lock",
                    title: "Change Password",
                    action: { showingChangePassword = true }
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
                    icon: "gearshape",
                    title: "Settings",
                    action: { showingSettings = true }
                )

                Divider()

                SettingsRow(
                    icon: "calendar",
                    title: "Order History",
                    action: {
                        // Navigate to Orders tab
                        router.selectedTab = .orders
                    }
                )

                Divider()

                SettingsRow(
                    icon: "icloud.slash",
                    title: "Offline Mode",
                    action: { showingOfflineMode = true }
                )

                Divider()

                // Follow System Theme Toggle (calendar icon matches Android)
                ThemeToggleRow(
                    title: "Follow System Theme",
                    subtitle: themeViewModel.state.isSystemTheme ? "On" : "Off",
                    icon: "calendar",
                    isChecked: themeViewModel.state.isSystemTheme,
                    onToggle: { target in
                        Task {
                            await themeViewModel.setFollowSystemTheme(target)
                        }
                    }
                )

                Divider()

                // Dark Mode Toggle
                ThemeToggleRow(
                    title: "Dark Mode",
                    subtitle: themeViewModel.state.isSystemTheme ? "Controlled by system" : (themeViewModel.state.isDarkMode ? "On" : "Off"),
                    icon: themeViewModel.state.isDarkMode ? "moon.fill" : "sun.max.fill",
                    isChecked: themeViewModel.state.isDarkMode,
                    onToggle: { target in
                        if !themeViewModel.state.isSystemTheme {
                            Task {
                                await themeViewModel.setDarkMode(target)
                            }
                        }
                    },
                    enabled: !themeViewModel.state.isSystemTheme
                )

                // Logout (only if logged in)
                if viewModel.state.isLoggedIn {
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
        // Fill the column like the other cards — text-only content otherwise
        // hugs and renders as a narrow centered card.
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .cardStyle()
    }
}
