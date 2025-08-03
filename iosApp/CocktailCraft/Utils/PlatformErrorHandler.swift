import Foundation
import shared

@MainActor
class PlatformErrorHandler {
    static let shared = PlatformErrorHandler()
    
    private init() {}
    
    func getErrorFromPlatformException(
        exception: NSError,
        defaultMessage: String,
        recoveryAction: ErrorHandler.RecoveryAction?
    ) -> ErrorHandler.UserFriendlyError {
        // Map iOS-specific errors to UserFriendlyError
        switch exception.code {
        case NSURLErrorNotConnectedToInternet,
             NSURLErrorNetworkConnectionLost:
            return ErrorHandler.UserFriendlyError(
                title: "Network Error",
                message: "Unable to connect to the server. Please check your internet connection.",
                category: ErrorHandler.ErrorCategory.network,
                recoveryAction: recoveryAction,
                originalException: nil,
                errorCode: .network
            )
            
        case NSURLErrorTimedOut:
            return ErrorHandler.UserFriendlyError(
                title: "Connection Timeout",
                message: "The connection timed out. Please try again.",
                category: ErrorHandler.ErrorCategory.network,
                recoveryAction: recoveryAction,
                originalException: nil,
                errorCode: .timeout
            )
            
        case NSURLErrorCannotFindHost,
             NSURLErrorCannotConnectToHost:
            return ErrorHandler.UserFriendlyError(
                title: "Server Error",
                message: "Unable to reach the server. Please try again later.",
                category: ErrorHandler.ErrorCategory.server,
                recoveryAction: recoveryAction,
                originalException: nil,
                errorCode: .serverError
            )
            
        default:
            // Fallback to shared error handler
            return ErrorHandler.shared.createUserFriendlyError(
                title: "Error",
                message: exception.localizedDescription.isEmpty ? defaultMessage : exception.localizedDescription,
                category: ErrorHandler.ErrorCategory.unknown,
                recoveryAction: recoveryAction,
                originalException: nil,
                errorCode: .unknown
            )
        }
    }
}