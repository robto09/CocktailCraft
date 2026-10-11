import SwiftUI
import shared

/**
 * iOS ViewModel wrapper for SharedProfileViewModel using pure SKIE integration.
 * State/error mirroring and observation-task lifecycle live in
 * SharedViewModelWrapper.
 */
final class ProfileViewModelSKIE: SharedViewModelWrapper<ProfileUiState> {

    // Presentation fallbacks over the mirrored state (Observation-tracked)
    var userName: String {
        state.user?.name ?? "Guest"
    }

    var userEmail: String {
        state.user?.email ?? ""
    }

    // Shared ViewModel instance
    private let sharedViewModel: SharedProfileViewModel

    init() {
        let viewModel = getSharedKoinHelper().getSharedProfileViewModel()
        self.sharedViewModel = viewModel
        super.init(viewModel: viewModel, uiState: viewModel.uiState)
    }

    // No deinit: the base class cancels observation. Wraps a Koin `single`
    // whose coroutine scope must survive any one wrapper — never onCleared().

    // MARK: - Public Methods (using SKIE async/await)

    // Each returns whether it succeeded so the calling sheet can dismiss;
    // failures surface on the mirrored `error`.

    func signIn(email: String, password: String) async -> Bool {
        return await run(fallback: false) { try await sharedViewModel.signIn(email: email, password: password).boolValue }
    }

    func signUp(name: String, email: String, password: String) async -> Bool {
        return await run(fallback: false) { try await sharedViewModel.signUp(name: name, email: email, password: password).boolValue }
    }

    func signOut() async -> Bool {
        return await run(fallback: false) { try await sharedViewModel.signOut().boolValue }
    }

    func updateProfile(name: String, email: String) async -> Bool {
        return await run(fallback: false) { try await sharedViewModel.updateProfile(name: name, email: email).boolValue }
    }

    func changePassword(oldPassword: String, newPassword: String) async -> Bool {
        return await run(fallback: false) { try await sharedViewModel.changePassword(oldPassword: oldPassword, newPassword: newPassword).boolValue }
    }

    // MARK: - Synchronous Methods

    // Pure shared validators, so form gates match the rules the shared
    // ViewModel enforces.

    func isValidEmail(_ email: String) -> Bool {
        return sharedViewModel.isValidEmail(email: email)
    }

    func meetsPasswordPolicy(_ password: String) -> Bool {
        return sharedViewModel.meetsPasswordPolicy(password: password)
    }

    func getPasswordStrength(_ password: String) -> Int {
        return Int(sharedViewModel.getPasswordStrength(password: password))
    }

    func refresh() {
        sharedViewModel.refresh()
    }
}
