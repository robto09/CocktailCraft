import SwiftUI

struct CheckoutButton: View {
    let title: String
    let isEnabled: Bool
    let isLoading: Bool
    let action: () -> Void
    @Environment(\.isDarkMode) var isDarkMode
    
    init(
        title: String = "Place Order",
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
        Button(action: action) {
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
        .accessibilityIdentifier("cart.placeOrderButton")
        .padding(.horizontal, AppTheme.Spacing.lg)
        .animation(AppTheme.Animation.quick, value: isLoading)
    }
}

#Preview {
    VStack(spacing: 20) {
        CheckoutButton(
            title: "Place Order",
            isEnabled: true,
            isLoading: false,
            action: {}
        )
        
        CheckoutButton(
            title: "Processing...",
            isEnabled: false,
            isLoading: true,
            action: {}
        )
        
        CheckoutButton(
            title: "Place Order",
            isEnabled: false,
            isLoading: false,
            action: {}
        )
    }
    .environment(\.isDarkMode, false)
    .padding()
}
