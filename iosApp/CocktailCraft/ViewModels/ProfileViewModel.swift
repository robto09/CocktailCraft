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

                // Use SKIE AsyncSequence pattern - cast to proper type
                if let asyncFlow = signedInFlow as? any AsyncSequence {
                    for try await value in asyncFlow {
                        if let boolValue = value as? KotlinBoolean, let isSignedIn = boolValue.boolValue as? Bool {
                            self.isAuthenticated = isSignedIn
                            if isSignedIn {
                                self.loadUserDetails()
                            }
                        }
                        self.isLoading = false
                        return // Take first emission and exit
                    }
                } else {
                    // Fallback to mock data
                    self.isAuthenticated = true
                    self.isLoading = false
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

                // Use SKIE AsyncSequence pattern - cast to proper type
                if let asyncFlow = userFlow as? any AsyncSequence {
                    for try await user in asyncFlow {
                        if let userObj = user as? User {
                            self.userName = userObj.name
                            self.userEmail = userObj.email
                        }
                        self.isLoading = false
                        return // Take first emission and exit
                    }
                } else {
                    // Fallback
                    self.isLoading = false
                }
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

                // Use SKIE AsyncSequence pattern - cast to proper type
                if let asyncFlow = signInFlow as? any AsyncSequence {
                    for try await success in asyncFlow {
                        if let boolValue = success as? KotlinBoolean, let successValue = boolValue.boolValue as? Bool, successValue {
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
                                errorCode: .unauthorized
                            )
                        }
                        self.isLoading = false
                        return // Take first emission and exit
                    }
                } else {
                    // Fallback
                    self.isLoading = false
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

                // Use SKIE AsyncSequence pattern - cast to proper type
                if let asyncFlow = signOutFlow as? any AsyncSequence {
                    for try await success in asyncFlow {
                        await MainActor.run {
                            if let boolValue = success as? KotlinBoolean, let successValue = boolValue.boolValue as? Bool, successValue {
                                self.isAuthenticated = false
                                self.userName = ""
                                self.userEmail = ""
                            }
                            self.isLoading = false
                        }
                        return // Take first emission and exit
                    }
                } else {
                    // Fallback
                    await MainActor.run {
                        self.isLoading = false
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