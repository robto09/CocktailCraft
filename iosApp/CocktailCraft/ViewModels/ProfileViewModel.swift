import SwiftUI
import shared

class ProfileViewModel: ObservableObject {
    @Published var isAuthenticated = false
    @Published var userName = ""
    @Published var userEmail = ""
    
    private let authRepository: AuthRepository
    
    init() {
        self.authRepository = koin.get(objCClass: AuthRepository.self) as! AuthRepository
        checkAuthStatus()
    }
    
    func checkAuthStatus() {
        Task {
            let user = try? await authRepository.getCurrentUser()
            await MainActor.run {
                if let user = user {
                    self.isAuthenticated = true
                    self.userName = user.name
                    self.userEmail = user.email
                } else {
                    self.isAuthenticated = false
                }
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
            try? await authRepository.signOut()
            await MainActor.run {
                self.isAuthenticated = false
                self.userName = ""
                self.userEmail = ""
            }
        }
    }
}