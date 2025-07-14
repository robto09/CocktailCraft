import Foundation
import shared

class PlatformErrorHandler {
    static let shared = PlatformErrorHandler()
    
    private init() {}
    
    func getErrorFromPlatformException(
        exception: NSError,
        defaultMessage: String,
        recoveryAction: RecoveryAction?
    ) -> UserFriendlyError {
        // Map iOS-specific errors to UserFriendlyError
        switch exception.code {
        case NSURLErrorNotConnectedToInternet,
             NSURLErrorNetworkConnectionLost:
            return UserFriendlyError(
                title: "Network Error",
                message: "Unable to connect to the server. Please check your internet connection.",
                category: ErrorCategory.network,
                recoveryAction: recoveryAction,
                originalException: nil,
                errorCode: ErrorCode.network
            )
            
        case NSURLErrorTimedOut:
            return UserFriendlyError(
                title: "Connection Timeout",
                message: "The connection timed out. Please try again.",
                category: ErrorCategory.network,
                recoveryAction: recoveryAction,
                originalException: nil,
                errorCode: ErrorCode.timeout
            )
            
        case NSURLErrorCannotFindHost,
             NSURLErrorCannotConnectToHost:
            return UserFriendlyError(
                title: "Server Error",
                message: "Unable to reach the server. Please try again later.",
                category: ErrorCategory.server,
                recoveryAction: recoveryAction,
                originalException: nil,
                errorCode: ErrorCode.serverError
            )
            
        default:
            // Fallback to shared error handler
            return ErrorHandler.shared.createUserFriendlyError(
                title: "Error",
                message: exception.localizedDescription.isEmpty ? defaultMessage : exception.localizedDescription,
                category: ErrorCategory.unknown,
                recoveryAction: recoveryAction,
                originalException: nil,
                errorCode: ErrorCode.unknown
            )
        }
    }
}