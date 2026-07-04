import SwiftUI
import shared

/**
 * iOS ViewModel wrapper for SharedProfileViewModel using pure SKIE integration.
 * State/error mirroring and observation-task lifecycle live in
 * SharedViewModelWrapper.
 */
final class ProfileViewModelSKIE: SharedViewModelWrapper<ProfileUiState> {

    // Computed properties
    var isGuest: Bool {
        !state.isLoggedIn
    }

    var userName: String {
        state.user?.name ?? "Guest"
    }

    var userEmail: String {
        state.user?.email ?? ""
    }

    var userInitials: String {
        sharedViewModel.getUserInitials()
    }

    var hasCompleteProfile: Bool {
        sharedViewModel.hasCompleteProfile()
    }

    // Shared ViewModel instance
    private let sharedViewModel: SharedProfileViewModel

    init() {
        let viewModel = getSharedKoinHelper().getSharedProfileViewModel()
        self.sharedViewModel = viewModel
        super.init(uiState: viewModel.uiState, errorFlow: viewModel.error)
    }

    // No deinit: the base class cancels observation. Wraps a Koin `single`
    // whose coroutine scope must survive any one wrapper — never onCleared().

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
        if let name = state.user?.name, !name.isEmpty {
            return name
        }
        return "Guest User"
    }

    func getProfileCompletionPercentage() -> Double {
        guard let user = state.user else { return 0.0 }

        var completedFields = 0
        let totalFields = 2 // name and email

        if !user.name.isEmpty { completedFields += 1 }
        if !user.email.isEmpty { completedFields += 1 }

        return Double(completedFields) / Double(totalFields)
    }

    func getMissingProfileFields() -> [String] {
        guard let user = state.user else { return ["Name", "Email"] }

        var missing: [String] = []
        if user.name.isEmpty { missing.append("Name") }
        if user.email.isEmpty { missing.append("Email") }

        return missing
    }
}
