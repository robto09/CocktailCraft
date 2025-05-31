import SwiftUI
import shared

struct ProfileScreen: View {
    @ObservedObject private var profileViewModel = ViewModelProvider.shared.profileViewModel
    @ObservedObject private var themeViewModel = ViewModelProvider.shared.themeViewModel
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
            ZStack {
                ScrollView {
                    VStack(spacing: 16) {
                        // Profile header
                        ProfileCard(
                            userName: userName,
                            userEmail: userEmail,
                            isSignedIn: profileViewModel.isSignedIn,
                            onSignIn: { showSignInDialog = true },
                            onSignUp: { showSignUpDialog = true }
                        )
                        .padding(.horizontal)
                        
                        // Account settings (only if signed in)
                        if profileViewModel.isSignedIn {
                            SettingsSection(title: "Account Settings") {
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
                            .padding(.horizontal)
                        }
                        
                        // App settings
                        SettingsSection(title: "App Settings") {
                            VStack(spacing: 0) {
                                SettingsRow(
                                    icon: "calendar",
                                    title: "Order History",
                                    action: {
                                        navigationCoordinator.selectedTab = .orders
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
                                
                                SettingsToggleRow(
                                    icon: "gear",
                                    title: "Follow System Theme",
                                    subtitle: themeViewModel.followSystemTheme ? "On" : "Off",
                                    isOn: Binding(
                                        get: { themeViewModel.followSystemTheme },
                                        set: { _ in themeViewModel.toggleFollowSystemTheme() }
                                    )
                                )
                                
                                Divider()
                                
                                SettingsToggleRow(
                                    icon: themeViewModel.isDarkMode ? "moon.fill" : "sun.max",
                                    title: "Dark Mode",
                                    subtitle: themeViewModel.followSystemTheme ? "Controlled by system" : (themeViewModel.isDarkMode ? "On" : "Off"),
                                    isOn: Binding(
                                        get: { themeViewModel.isDarkMode },
                                        set: { _ in 
                                            if !themeViewModel.followSystemTheme {
                                                themeViewModel.toggleDarkMode()
                                            }
                                        }
                                    ),
                                    isEnabled: !themeViewModel.followSystemTheme
                                )
                                
                                // Logout (only if signed in)
                                if profileViewModel.isSignedIn {
                                    Divider()
                                    
                                    SettingsRow(
                                        icon: "rectangle.portrait.and.arrow.right",
                                        title: "Logout",
                                        textColor: .red,
                                        action: { showLogoutAlert = true }
                                    )
                                }
                            }
                        }
                        .padding(.horizontal)
                        
                        // About section
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
                        
                        Spacer(minLength: 20)
                    }
                    .padding(.vertical)
                }
                
                // Loading overlay
                if profileViewModel.isLoading {
                    Color.black.opacity(0.5)
                        .ignoresSafeArea()
                    
                    ProgressView()
                        .scaleEffect(1.5)
                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                }
            }
            .navigationTitle("Profile")
            .navigationBarTitleDisplayMode(.large)
            .alert("Logout", isPresented: $showLogoutAlert) {
                Button("Cancel", role: .cancel) { }
                Button("Logout", role: .destructive) {
                    profileViewModel.signOut()
                    navigationCoordinator.selectedTab = .home
                }
            } message: {
                Text("Are you sure you want to logout?")
            }
            .alert("Error", isPresented: $showErrorAlert) {
                Button("OK") {
                    profileViewModel.clearError()
                }
            } message: {
                Text(profileViewModel.error ?? "An unknown error occurred")
            }
            .onChange(of: profileViewModel.error) { error in
                showErrorAlert = error != nil
            }
            .sheet(isPresented: $showSignInDialog) {
                SignInDialog(
                    isPresented: $showSignInDialog,
                    onSignIn: { email, password in
                        profileViewModel.signIn(email: email, password: password)
                    }
                )
            }
            .sheet(isPresented: $showSignUpDialog) {
                SignUpDialog(
                    isPresented: $showSignUpDialog,
                    onSignUp: { name, email, password in
                        profileViewModel.signUp(name: name, email: email, password: password)
                    }
                )
            }
        }
    }
}