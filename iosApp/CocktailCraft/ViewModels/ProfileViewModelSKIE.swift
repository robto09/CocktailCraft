import SwiftUI
import shared
import Combine

/**
 * iOS ViewModel wrapper for SharedProfileViewModel using pure SKIE integration.
 * No FlowCollector bridge needed - uses native Swift async/await.
 */
@MainActor
class ProfileViewModelSKIE: ObservableObject {
    // Published properties for SwiftUI
    @Published var user: User? = nil
    @Published var isLoggedIn = false
    @Published var authStatus = "Unknown"
    @Published var isAuthenticating = false
    @Published var authError: String? = nil
    @Published var userPreferences = UserPreferences()
    @Published var error: ErrorHandler.UserFriendlyError? = nil
    
    // Computed properties
    var isGuest: Bool {
        !isLoggedIn
    }
    
    var userName: String {
        user?.name ?? "Guest"
    }
    
    var userEmail: String {
        user?.email ?? ""
    }
    
    var userInitials: String {
        sharedViewModel.getUserInitials()
    }
    
    var hasCompleteProfile: Bool {
        sharedViewModel.hasCompleteProfile()
    }
    
    // Shared ViewModel instance
    private let sharedViewModel: ProfileViewModel
    
    // Tasks for async observation
    private var observationTasks: [Task<Void, Never>] = []
    
    init() {
        // Get shared ViewModel from Koin
        self.sharedViewModel = getSharedKoinHelper().getSharedProfileViewModel()
        
        // Start observing StateFlows using SKIE async/await
        startObserving()
    }
    
    deinit {
        // Cancel all observation tasks
        observationTasks.forEach { $0.cancel() }
        sharedViewModel.onCleared()
    }
    
    // MARK: - SKIE StateFlow Observation
    
    private func startObserving() {
        // Observe user using SKIE async sequence
        observationTasks.append(Task {
            for await userValue in sharedViewModel.user {
                await MainActor.run {
                    self.user = userValue
                }
            }
        })
        
        // Observe login status
        observationTasks.append(Task {
            for await loggedIn in sharedViewModel.isLoggedIn {
                await MainActor.run {
                    self.isLoggedIn = loggedIn.boolValue
                }
            }
        })
        
        // Observe auth status
        observationTasks.append(Task {
            for await status in sharedViewModel.authStatus {
                await MainActor.run {
                    self.authStatus = status
                }
            }
        })
        
        // Observe authenticating state
        observationTasks.append(Task {
            for await authenticating in sharedViewModel.isAuthenticating {
                await MainActor.run {
                    self.isAuthenticating = authenticating.boolValue
                }
            }
        })
        
        // Observe auth error
        observationTasks.append(Task {
            for await authErrorValue in sharedViewModel.authError {
                await MainActor.run {
                    self.authError = authErrorValue
                }
            }
        })
        
        // Observe user preferences
        observationTasks.append(Task {
            for await preferences in sharedViewModel.userPreferences {
                await MainActor.run {
                    self.userPreferences = preferences
                }
            }
        })
        
        // Observe error state
        observationTasks.append(Task {
            for await errorValue in sharedViewModel.error {
                await MainActor.run {
                    self.error = errorValue
                }
            }
        })
    }
    
    // MARK: - Public Methods (using SKIE async/await)
    
    func signIn(email: String, password: String) async -> Bool {
        do {
            return try await sharedViewModel.signIn(email: email, password: password).boolValue
        } catch {
            return false
        }
    }
    
    func signUp(name: String, email: String, password: String) async -> Bool {
        do {
            return try await sharedViewModel.signUp(name: name, email: email, password: password).boolValue
        } catch {
            return false
        }
    }
    
    func signOut() async -> Bool {
        do {
            return try await sharedViewModel.signOut().boolValue
        } catch {
            return false
        }
    }
    
    func updateProfile(name: String, email: String) async -> Bool {
        do {
            return try await sharedViewModel.updateProfile(name: name, email: email).boolValue
        } catch {
            return false
        }
    }
    
    func resetPassword(email: String) async -> Bool {
        do {
            return try await sharedViewModel.resetPassword(email: email).boolValue
        } catch {
            return false
        }
    }
    
    func changePassword(oldPassword: String, newPassword: String) async -> Bool {
        do {
            return try await sharedViewModel.changePassword(oldPassword: oldPassword, newPassword: newPassword).boolValue
        } catch {
            return false
        }
    }
    
    func updateAddress(address: Address) async -> Bool {
        do {
            return try await sharedViewModel.updateAddress(address: address).boolValue
        } catch {
            return false
        }
    }
    
    // MARK: - Synchronous Methods
    
    func isValidEmail(_ email: String) -> Bool {
        return sharedViewModel.isValidEmail(email: email)
    }
    
    func isValidPassword(_ password: String) -> Bool {
        return sharedViewModel.isValidPassword(password: password)
    }
    
    func getPasswordStrength(_ password: String) -> Int {
        return Int(sharedViewModel.getPasswordStrength(password: password))
    }
    
    func checkAuthStatus() async {
        do {
            try await sharedViewModel.checkAuthStatus()
        } catch {
            // Handle error silently
        }
    }
    
    func clearError() {
        sharedViewModel.clearError()
    }
    
    func refresh() {
        sharedViewModel.refresh()
    }
    
    func getDisplayName() -> String {
        return sharedViewModel.getDisplayName()
    }
    
    func getUserEmail() -> String {
        return sharedViewModel.getUserEmail()
    }
    
    func isEmailVerified() -> Bool {
        return sharedViewModel.isEmailVerified()
    }
    
    // MARK: - Helper Methods for SwiftUI
    
    func getPasswordStrengthText(_ password: String) -> String {
        let strength = getPasswordStrength(password)
        switch strength {
        case 0...1: return "Weak"
        case 2: return "Fair"
        case 3: return "Good"
        case 4: return "Strong"
        default: return "Unknown"
        }
    }
    
    func getPasswordStrengthColor(_ password: String) -> Color {
        let strength = getPasswordStrength(password)
        switch strength {
        case 0...1: return .red
        case 2: return .orange
        case 3: return .yellow
        case 4: return .green
        default: return .gray
        }
    }
    
    func formatUserName() -> String {
        if let name = user?.name, !name.isEmpty {
            return name
        }
        return "Guest User"
    }
    
    func getProfileCompletionPercentage() -> Double {
        guard let user = user else { return 0.0 }
        
        var completedFields = 0
        let totalFields = 2 // name and email
        
        if !user.name.isEmpty { completedFields += 1 }
        if !user.email.isEmpty { completedFields += 1 }
        
        return Double(completedFields) / Double(totalFields)
    }
    
    func getMissingProfileFields() -> [String] {
        guard let user = user else { return ["Name", "Email"] }
        
        var missing: [String] = []
        if user.name.isEmpty { missing.append("Name") }
        if user.email.isEmpty { missing.append("Email") }
        
        return missing
    }
}