import SwiftUI

struct ConfirmationAlert: ViewModifier {
    @Binding var isPresented: Bool
    let title: String
    let message: String
    let confirmButtonText: String
    let onConfirm: () -> Void
    
    func body(content: Content) -> some View {
        content
            .alert(title, isPresented: $isPresented) {
                Button(confirmButtonText, role: .destructive, action: onConfirm)
                Button("Cancel", role: .cancel) { }
            } message: {
                Text(message)
            }
    }
}

extension View {
    func confirmationAlert(
        isPresented: Binding<Bool>,
        title: String,
        message: String,
        confirmButtonText: String = "Confirm",
        onConfirm: @escaping () -> Void
    ) -> some View {
        modifier(ConfirmationAlert(
            isPresented: isPresented,
            title: title,
            message: message,
            confirmButtonText: confirmButtonText,
            onConfirm: onConfirm
        ))
    }
}