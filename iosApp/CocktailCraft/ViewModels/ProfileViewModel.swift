import SwiftUI
@preconcurrency import shared

// Simple collector for repository flows (temporary workaround until SKIE fully supports all flows)
class SimpleFlowCollector<T>: NSObject, Kotlinx_coroutines_coreFlowCollector {
    private let onValue: (T?) -> Void

    init(onValue: @escaping (T?) -> Void) {
        self.onValue = onValue
        super.init()
    }

    func __emit(value: Any?) async throws {
        onValue(value as? T)
    }
}

@MainActor
class ProfileViewModel: ObservableObject {
    @Published var isAuthenticated = false
    @Published var userName = ""
    @Published var userEmail = ""
    @Published var isLoading = false
    @Published var error: ErrorHandler.UserFriendlyError? = nil

    private let authRepository: AuthRepository?

    init() {
        self.authRepository = KoinInitializer.shared.getAuthRepository()
        checkAuthStatus()
    }

    func checkAuthStatus() {
        guard let authRepository = authRepository else {
            // If no repository, use mock data for testing
            isAuthenticated = true
            userName = "Test User"
            userEmail = "test@example.com"
            return
        }

        isLoading = true
        error = nil

        Task { @MainActor in
            do {
                // Check if user is signed in
                let signedInFlow = try await authRepository.isUserSignedIn()

                // Use simple collector for AuthRepository flows (temporary workaround)
                let collector = SimpleFlowCollector<KotlinBoolean> { [weak self] value in
                    DispatchQueue.main.async {
                        if let isSignedIn = value?.boolValue {
                            self?.isAuthenticated = isSignedIn
                            if isSignedIn {
                                self?.loadUserDetails()
                            }
                        }
                        self?.isLoading = false
                    }
                }

                try await signedInFlow.collect(collector: collector)
            } catch {
                await MainActor.run {
                    self.isLoading = false
                    self.error = ErrorHandler.shared.createUserFriendlyError(
                        title: "Authentication Error",
                        message: "Failed to check authentication status: \(error.localizedDescription)",
                        category: ErrorHandler.ErrorCategory.unknown,
                        recoveryAction: nil,
                        originalException: nil,
                        errorCode: .unknown
                    )
                }
            }
        }
    }

    private func loadUserDetails() {
        guard let authRepository = authRepository else { return }

        Task { @MainActor in
            do {
                let userFlow = try await authRepository.getCurrentUser()

                // Use simple collector for AuthRepository flows (temporary workaround)
                let collector = SimpleFlowCollector<User> { [weak self] user in
                    DispatchQueue.main.async {
                        if let user = user {
                            self?.userName = user.name
                            self?.userEmail = user.email
                        }
                        self?.isLoading = false
                    }
                }

                try await userFlow.collect(collector: collector)
            } catch {
                self.isLoading = false
                self.error = ErrorHandler.shared.createUserFriendlyError(
                    title: "User Data Error",
                    message: "Failed to load user details: \(error.localizedDescription)",
                    category: ErrorHandler.ErrorCategory.unknown,
                    recoveryAction: nil,
                    originalException: nil,
                    errorCode: .unknown
                )
            }
        }
    }

    func signIn(email: String, password: String) {
        guard let authRepository = authRepository else {
            // Fallback to mock data if repository is not available
            isAuthenticated = true
            userName = "John Doe"
            userEmail = email
            return
        }

        isLoading = true
        error = nil

        Task { @MainActor in
            do {
                let signInFlow = try await authRepository.signIn(email: email, password: password)

                // Use simple collector for AuthRepository flows (temporary workaround)
                let collector = SimpleFlowCollector<KotlinBoolean> { [weak self] success in
                    DispatchQueue.main.async {
                        if let success = success?.boolValue, success {
                            self?.isAuthenticated = true
                            self?.userEmail = email
                            self?.loadUserDetails()
                        } else {
                            self?.error = ErrorHandler.shared.createUserFriendlyError(
                                title: "Sign In Failed",
                                message: "Invalid email or password",
                                category: ErrorHandler.ErrorCategory.authentication,
                                recoveryAction: nil,
                                originalException: nil,
                                errorCode: .unauthorized
                            )
                        }
                        self?.isLoading = false
                    }
                }

                try await signInFlow.collect(collector: collector)
            } catch {
                await MainActor.run {
                    self.isLoading = false
                    self.error = ErrorHandler.shared.createUserFriendlyError(
                        title: "Sign In Error",
                        message: "Failed to sign in: \(error.localizedDescription)",
                        category: ErrorHandler.ErrorCategory.unknown,
                        recoveryAction: nil,
                        originalException: nil,
                        errorCode: .unknown
                    )
                }
            }
        }
    }
    
    func signOut() {
        guard let authRepository = authRepository else {
            // Fallback to local state if repository is not available
            isAuthenticated = false
            userName = ""
            userEmail = ""
            return
        }

        isLoading = true
        error = nil

        Task {
            do {
                let signOutFlow = try await authRepository.signOut()

                // Use simple collector for AuthRepository flows (temporary workaround)
                let collector = SimpleFlowCollector<KotlinBoolean> { [weak self] success in
                    DispatchQueue.main.async {
                        if let success = success?.boolValue, success {
                            self?.isAuthenticated = false
                            self?.userName = ""
                            self?.userEmail = ""
                        }
                        self?.isLoading = false
                    }
                }

                try await signOutFlow.collect(collector: collector)
            } catch {
                await MainActor.run {
                    self.isLoading = false
                    self.error = ErrorHandler.shared.createUserFriendlyError(
                        title: "Sign Out Error",
                        message: "Failed to sign out: \(error.localizedDescription)",
                        category: ErrorHandler.ErrorCategory.unknown,
                        recoveryAction: nil,
                        originalException: nil,
                        errorCode: .unknown
                    )
                }
            }
        }
    }

    func retryCheckAuthStatus() {
        checkAuthStatus()
    }
}