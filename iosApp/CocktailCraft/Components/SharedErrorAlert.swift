import SwiftUI
import shared

/// Standard error surfacing for screens backed by a shared KMP ViewModel.
/// Presents the ViewModel's mirrored error channel as an alert, offering the
/// error's recovery action when present, and clears the error on the shared
/// side on dismissal so it cannot re-present stale state.
struct SharedErrorAlert: ViewModifier {
    let error: ErrorHandler.UserFriendlyError?
    let clearError: () -> Void

    func body(content: Content) -> some View {
        content.alert(
            error?.title ?? "Error",
            isPresented: Binding(
                get: { error != nil },
                set: { if !$0 { clearError() } }
            )
        ) {
            if let recovery = error?.recoveryAction {
                Button(recovery.actionLabel) {
                    clearError()
                    recovery.action()
                }
            }
            Button("Dismiss", role: .cancel) { clearError() }
        } message: {
            Text(error?.message ?? "")
        }
    }
}

extension View {
    /// Attach to a screen's outermost container to surface shared ViewModel errors.
    func sharedErrorAlert(
        _ error: ErrorHandler.UserFriendlyError?,
        clear: @escaping () -> Void
    ) -> some View {
        modifier(SharedErrorAlert(error: error, clearError: clear))
    }
}
