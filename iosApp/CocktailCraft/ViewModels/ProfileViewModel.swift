import SwiftUI

import shared

class ProfileViewModel: ObservableObject {
    @Published var isAuthenticated = false
    @Published var userName = ""
    @Published var userEmail = ""
    
    private let authRepository: AuthRepository?

    init() {
        self.authRepository = KoinInitializer.shared.getAuthRepository()
        checkAuthStatus()
    }
    
    func checkAuthStatus() {
        // For now, use mock data since Flow collection is complex
        // TODO: Implement proper Flow collection when SKIE is available
        Task {
            await MainActor.run {
                // Mock authenticated user for testing
                self.isAuthenticated = true
                self.userName = "Test User"
                self.userEmail = "test@example.com"
            }
        }
    }
    
    func signIn() {
        // Implement sign in logic
        // For now, just set dummy data
        isAuthenticated = true
        userName = "John Doe"
        userEmail = "john.doe@example.com"
    }
    
    func signOut() {
        Task {
            // Since authRepository is nil for now, just simulate sign out
            if let authRepository = authRepository {
                try? await authRepository.signOut()
            }
            await MainActor.run {
                self.isAuthenticated = false
                self.userName = ""
                self.userEmail = ""
            }
        }
    }
}