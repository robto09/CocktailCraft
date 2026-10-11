import SwiftUI

struct CheckoutButton: View {
    let title: String
    let isEnabled: Bool
    let isLoading: Bool
    let action: () -> Void
    @Environment(\.isDarkMode) var isDarkMode
    
    init(
        title: String = L(\.cart_place_order),
        isEnabled: Bool = true,
        isLoading: Bool = false,
        action: @escaping () -> Void
    ) {
        self.title = title
        self.isEnabled = isEnabled
        self.isLoading = isLoading
        self.action = action
    }
    
    var body: some View {
        Button(action: {
            // Confirmation haptic, parity with Android's checkout
            UINotificationFeedbackGenerator().notificationOccurred(.success)
            action()
        }) {
            HStack {
                if isLoading {
                    ProgressView()
                        .scaleEffect(0.8)
                        .tint(.white)
                } else {
                    Text(title)
                        .font(AppTheme.Typography.headline)
                        .fontWeight(.semibold)
                }
            }
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .padding(.vertical, AppTheme.Spacing.lg)
            .background(
                isEnabled 
                    ? AppColors.primary(isDarkMode: isDarkMode)
                    : AppColors.textSecondary(isDarkMode: isDarkMode)
            )
            .cornerRadius(AppTheme.CornerRadius.md)
        }
        .disabled(!isEnabled || isLoading)
        // Keeps its name while the spinner replaces the title
        .accessibilityLabel(title)
        .accessibilityIdentifier("cart.placeOrderButton")
        .padding(.horizontal, AppTheme.Spacing.lg)
        .animation(AppTheme.Animation.quick, value: isLoading)
    }
}

#Preview {
    VStack(spacing: 20) {
        CheckoutButton(
            title: L(\.cart_place_order),
            isEnabled: true,
            isLoading: false,
            action: {}
        )
        
        CheckoutButton(
            title: L(\.checkout_processing),
            isEnabled: false,
            isLoading: true,
            action: {}
        )
        
        CheckoutButton(
            title: L(\.cart_place_order),
            isEnabled: false,
            isLoading: false,
            action: {}
        )
    }
    .environment(\.isDarkMode, false)
    .padding()
}
