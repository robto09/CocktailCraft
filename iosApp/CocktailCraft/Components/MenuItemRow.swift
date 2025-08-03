import SwiftUI

struct MenuItemRow: View {
    let icon: String
    let title: String
    let action: () -> Void
    let textColor: Color
    let showChevron: Bool
    @Environment(\.isDarkMode) var isDarkMode

    init(
        icon: String,
        title: String,
        action: @escaping () -> Void,
        textColor: Color = .primary,
        showChevron: Bool = true
    ) {
        self.icon = icon
        self.title = title
        self.action = action
        self.textColor = textColor
        self.showChevron = showChevron
    }

    var body: some View {
        Button(action: action) {
            HStack(spacing: AppTheme.Spacing.lg) {
                Image(systemName: icon)
                    .foregroundColor(resolvedTextColor)
                    .frame(width: 24, height: 24)

                Text(title)
                    .font(AppTheme.Typography.body)
                    .foregroundColor(resolvedTextColor)

                Spacer()

                if showChevron {
                    Image(systemName: "chevron.right")
                        .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
                        .font(.caption)
                }
            }
            .padding(.vertical, AppTheme.Spacing.md)
        }
        .buttonStyle(PlainButtonStyle())
    }
    
    private var resolvedTextColor: Color {
        if textColor == .primary {
            return AppColors.textPrimary(isDarkMode: isDarkMode)
        } else {
            return textColor
        }
    }
}

struct ThemeToggleRow: View {
    let title: String
    let subtitle: String
    let icon: String
    let isChecked: Bool
    let onToggle: () -> Void
    let enabled: Bool
    @Environment(\.isDarkMode) var isDarkMode

    init(
        title: String,
        subtitle: String,
        icon: String,
        isChecked: Bool,
        onToggle: @escaping () -> Void,
        enabled: Bool = true
    ) {
        self.title = title
        self.subtitle = subtitle
        self.icon = icon
        self.isChecked = isChecked
        self.onToggle = onToggle
        self.enabled = enabled
    }

    var body: some View {
        HStack(spacing: AppTheme.Spacing.lg) {
            Image(systemName: icon)
                .foregroundColor(enabled ? AppColors.textPrimary(isDarkMode: isDarkMode) : AppColors.textSecondary(isDarkMode: isDarkMode))
                .frame(width: 24, height: 24)
                .scaleEffect(enabled ? 1.0 : 0.8)
                .animation(AppTheme.Animation.quick, value: enabled)

            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(AppTheme.Typography.body)
                    .foregroundColor(enabled ? AppColors.textPrimary(isDarkMode: isDarkMode) : AppColors.textSecondary(isDarkMode: isDarkMode))

                Text(subtitle)
                    .font(AppTheme.Typography.caption)
                    .foregroundColor(AppColors.textSecondary(isDarkMode: isDarkMode))
            }
            .opacity(enabled ? 1.0 : 0.6)
            .animation(AppTheme.Animation.standard, value: enabled)

            Spacer()

            Toggle("", isOn: Binding(
                get: { isChecked },
                set: { _ in onToggle() }
            ))
            .disabled(!enabled)
            .opacity(enabled ? 1.0 : 0.5)
        }
        .padding(.vertical, AppTheme.Spacing.md)
    }
}

#Preview {
    VStack(spacing: 0) {
        MenuItemRow(
            icon: "person",
            title: "Edit Profile",
            action: {}
        )
        
        Divider()
        
        MenuItemRow(
            icon: "arrow.right.square",
            title: "Logout",
            action: {},
            textColor: .red
        )
        
        Divider()
        
        ThemeToggleRow(
            title: "Dark Mode",
            subtitle: "On",
            icon: "moon.fill",
            isChecked: true,
            onToggle: {}
        )
        
        Divider()
        
        ThemeToggleRow(
            title: "Follow System Theme",
            subtitle: "Controlled by system",
            icon: "gear",
            isChecked: false,
            onToggle: {},
            enabled: false
        )
    }
    .environment(\.isDarkMode, false)
    .padding()
}
