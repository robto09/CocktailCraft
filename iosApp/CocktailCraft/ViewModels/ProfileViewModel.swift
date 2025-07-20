import SwiftUI
@preconcurrency import shared

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
                let signedInCollector = FlowCollector<KotlinBoolean>(flow: signedInFlow)

                if let isSignedIn = signedInCollector.value?.boolValue {
                    self.isAuthenticated = isSignedIn

                    if isSignedIn {
                        // Load user details
                        self.loadUserDetails()
                    }
                }
                self.isLoading = signedInCollector.isLoading
                if let error = signedInCollector.error {
                    self.error = ErrorHandler.shared.createUserFriendlyError(
                        title: "Authentication Error",
                        message: "Failed to check authentication status: \(error.localizedDescription)",
                        category: ErrorHandler.ErrorCategory.unknown,
                        recoveryAction: nil,
                        originalException: nil,
                        errorCode: ErrorCode.unknown
                    )
                }
            } catch {
                await MainActor.run {
                    self.isLoading = false
                    self.error = ErrorHandler.shared.createUserFriendlyError(
                        title: "Authentication Error",
                        message: "Failed to check authentication status: \(error.localizedDescription)",
                        category: ErrorHandler.ErrorCategory.unknown,
                        recoveryAction: nil,
                        originalException: nil,
                        errorCode: ErrorCode.unknown
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
                let userCollector = FlowCollector<User>(flow: userFlow)

                if let user = userCollector.value {
                    self.userName = user.name
                    self.userEmail = user.email
                }
                self.isLoading = userCollector.isLoading
                if let error = userCollector.error {
                    self.error = ErrorHandler.shared.createUserFriendlyError(
                        title: "User Data Error",
                        message: "Failed to load user details: \(error.localizedDescription)",
                        category: ErrorHandler.ErrorCategory.unknown,
                        recoveryAction: nil,
                        originalException: nil,
                        errorCode: ErrorCode.unknown
                    )
                }
            } catch {
                self.isLoading = false
                self.error = ErrorHandler.shared.createUserFriendlyError(
                    title: "User Data Error",
                    message: "Failed to load user details: \(error.localizedDescription)",
                    category: ErrorHandler.ErrorCategory.unknown,
                    recoveryAction: nil,
                    originalException: nil,
                    errorCode: ErrorCode.unknown
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
                let signInCollector = FlowCollector<KotlinBoolean>(flow: signInFlow)

                await MainActor.run {
                    if let success = signInCollector.value?.boolValue, success {
                        self.isAuthenticated = true
                        self.userEmail = email
                        self.loadUserDetails()
                    } else {
                        self.error = ErrorHandler.shared.createUserFriendlyError(
                            title: "Sign In Failed",
                            message: "Invalid email or password",
                            category: ErrorHandler.ErrorCategory.authentication,
                            recoveryAction: nil,
                            originalException: nil,
                            errorCode: ErrorCode.unauthorized
                        )
                    }
                    self.isLoading = signInCollector.isLoading
                    if let error = signInCollector.error {
                        self.error = ErrorHandler.shared.createUserFriendlyError(
                            title: "Sign In Error",
                            message: "Failed to sign in: \(error.localizedDescription)",
                            category: ErrorHandler.ErrorCategory.unknown,
                            recoveryAction: nil,
                            originalException: nil,
                            errorCode: ErrorCode.unknown
                        )
                    }
                }
            } catch {
                await MainActor.run {
                    self.isLoading = false
                    self.error = ErrorHandler.shared.createUserFriendlyError(
                        title: "Sign In Error",
                        message: "Failed to sign in: \(error.localizedDescription)",
                        category: ErrorHandler.ErrorCategory.unknown,
                        recoveryAction: nil,
                        originalException: nil,
                        errorCode: ErrorCode.unknown
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
                let signOutCollector = FlowCollector<KotlinBoolean>(flow: signOutFlow)

                await MainActor.run {
                    if let success = signOutCollector.value?.boolValue, success {
                        self.isAuthenticated = false
                        self.userName = ""
                        self.userEmail = ""
                    }
                    self.isLoading = signOutCollector.isLoading
                    if let error = signOutCollector.error {
                        self.error = ErrorHandler.shared.createUserFriendlyError(
                            title: "Sign Out Error",
                            message: "Failed to sign out: \(error.localizedDescription)",
                            category: ErrorHandler.ErrorCategory.unknown,
                            recoveryAction: nil,
                            originalException: nil,
                            errorCode: ErrorCode.unknown
                        )
                    }
                }
            } catch {
                await MainActor.run {
                    self.isLoading = false
                    self.error = ErrorHandler.shared.createUserFriendlyError(
                        title: "Sign Out Error",
                        message: "Failed to sign out: \(error.localizedDescription)",
                        category: ErrorHandler.ErrorCategory.unknown,
                        recoveryAction: nil,
                        originalException: nil,
                        errorCode: ErrorCode.unknown
                    )
                }
            }
        }
    }

    func retryCheckAuthStatus() {
        checkAuthStatus()
    }
}