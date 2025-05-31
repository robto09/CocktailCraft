import SwiftUI
import shared

struct ProfileScreen: View {
    @StateObject private var profileViewModel = ViewModelProvider.shared.profileViewModel
    @StateObject private var themeViewModel = ViewModelProvider.shared.themeViewModel
    @EnvironmentObject var navigationCoordinator: NavigationCoordinator
    
    @State private var showLogoutAlert = false
    @State private var showSignInDialog = false
    @State private var showSignUpDialog = false
    @State private var showErrorAlert = false
    
    private var userName: String {
        profileViewModel.user?.name ?? "Guest User"
    }
    
    private var userEmail: String {
        profileViewModel.user?.email ?? "guest@example.com"
    }
    
    var body: some View {
        NavigationView {
            contentView
                .navigationTitle("Profile")
                .navigationBarTitleDisplayMode(.large)
                .alert("Logout", isPresented: $showLogoutAlert) {
                    Button("Cancel", role: .cancel) { }
                    Button("Logout", role: .destructive) {
                        profileViewModel.signOut()
                        navigationCoordinator.navigateToTab(.home)
                    }
                } message: {
                    Text("Are you sure you want to logout?")
                }
                .alert("Error", isPresented: $showErrorAlert) {
                    Button("OK") { }
                } message: {
                    Text("An error occurred")
                }
                .sheet(isPresented: $showSignInDialog) {
                    signInSheet
                }
                .sheet(isPresented: $showSignUpDialog) {
                    signUpSheet
                }
        }
    }
    
    @ViewBuilder
    private var contentView: some View {
        ZStack {
            mainScrollView
            if profileViewModel.isLoading {
                loadingOverlay
            }
        }
    }
    
    private var mainScrollView: some View {
        ScrollView {
            VStack(spacing: 16) {
                profileHeader
                if profileViewModel.isSignedIn {
                    accountSettingsSection
                }
                appSettingsSection
                aboutSection
                Spacer(minLength: 20)
            }
            .padding(.vertical)
        }
    }
    
    private var profileHeader: some View {
        ProfileCard(
            userName: userName,
            userEmail: userEmail,
            isSignedIn: profileViewModel.isSignedIn,
            onSignIn: { showSignInDialog = true },
            onSignUp: { showSignUpDialog = true }
        )
        .padding(.horizontal)
    }
    
    private var accountSettingsSection: some View {
        SettingsSection(title: "Account Settings") {
            VStack(spacing: 0) {
                accountSettingsRows
            }
        }
        .padding(.horizontal)
    }
    
    private var accountSettingsRows: some View {
        Group {
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
    
    private var appSettingsSection: some View {
        SettingsSection(title: "App Settings") {
            VStack(spacing: 0) {
                appSettingsRows
                if profileViewModel.isSignedIn {
                    Divider()
                    logoutRow
                }
            }
        }
        .padding(.horizontal)
    }
    
    private var appSettingsRows: some View {
        Group {
            SettingsRow(
                icon: "calendar",
                title: "Order History",
                action: {
                    navigationCoordinator.navigateToTab(.orders)
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
                icon: "icloud.slash",
                title: "Offline Mode",
                action: {
                    navigationCoordinator.navigateToOfflineMode()
                }
            )
            
            Divider()
            
            themeSettingsRows
        }
    }
    
    private var themeSettingsRows: some View {
        Group {
            SettingsToggleRow(
                icon: "gear",
                title: "Follow System Theme",
                subtitle: "Automatic",
                isOn: Binding(
                    get: { false }, // Simplified for now
                    set: { _ in }
                )
            )
            
            Divider()
            
            SettingsToggleRow(
                icon: themeViewModel.isDarkMode ? "moon.fill" : "sun.max",
                title: "Dark Mode",
                subtitle: themeViewModel.isDarkMode ? "On" : "Off",
                isOn: Binding(
                    get: { themeViewModel.isDarkMode },
                    set: { _ in 
                        themeViewModel.setDarkMode(enabled: !themeViewModel.isDarkMode)
                    }
                ),
                isEnabled: true
            )
        }
    }
    
    private var logoutRow: some View {
        SettingsRow(
            icon: "rectangle.portrait.and.arrow.right",
            title: "Logout",
            textColor: .red,
            action: { showLogoutAlert = true }
        )
    }
    
    private var aboutSection: some View {
        SettingsSection(title: "About") {
            VStack(alignment: .leading, spacing: 8) {
                Text("CocktailCraft")
                    .font(.system(size: 16, weight: .medium))
                
                Text("Version 1.0.0")
                    .font(.system(size: 14))
                    .foregroundColor(.secondary)
                
                Text("© 2024 CocktailCraft. All rights reserved.")
                    .font(.system(size: 12))
                    .foregroundColor(.secondary)
                    .padding(.top, 8)
            }
        }
        .padding(.horizontal)
    }
    
    private var loadingOverlay: some View {
        Color.black.opacity(0.5)
            .ignoresSafeArea()
            .overlay(
                ProgressView()
                    .scaleEffect(1.5)
                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
            )
    }
    
    private var signInSheet: some View {
        SignInDialog(
            isPresented: $showSignInDialog,
            onSignIn: { email, password in
                profileViewModel.signIn(email: email, password: password)
            }
        )
    }
    
    private var signUpSheet: some View {
        SignUpDialog(
            isPresented: $showSignUpDialog,
            onSignUp: { name, email, password in
                // Note: signUp method might not exist yet in ProfileViewModel
                // For now, just close the dialog
                showSignUpDialog = false
            }
        )
    }
}