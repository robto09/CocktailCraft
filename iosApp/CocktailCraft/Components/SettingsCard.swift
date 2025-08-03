import SwiftUI

struct SettingsCard: View {
    let title: String
    let items: [SettingsItem]
    @Environment(\.isDarkMode) var isDarkMode
    
    var body: some View {
        VStack(alignment: .leading, spacing: AppTheme.Spacing.lg) {
            Text(title)
                .font(AppTheme.Typography.headline)
                .fontWeight(.bold)
                .foregroundColor(AppColors.textPrimary(isDarkMode: isDarkMode))

            VStack(spacing: 0) {
                ForEach(Array(items.enumerated()), id: \.offset) { index, item in
                    if index > 0 {
                        Divider()
                    }
                    
                    switch item {
                    case .action(let actionItem):
                        MenuItemRow(
                            icon: actionItem.icon,
                            title: actionItem.title,
                            action: actionItem.action,
                            textColor: actionItem.textColor
                        )
                    case .toggle(let toggleItem):
                        ThemeToggleRow(
                            title: toggleItem.title,
                            subtitle: toggleItem.subtitle,
                            icon: toggleItem.icon,
                            isChecked: toggleItem.isChecked,
                            onToggle: toggleItem.onToggle,
                            enabled: toggleItem.enabled
                        )
                    }
                }
            }
        }
        .padding(AppTheme.Spacing.lg)
        .cardStyle()
    }
}

enum SettingsItem {
    case action(ActionSettingsItem)
    case toggle(ToggleSettingsItem)
}

struct ActionSettingsItem {
    let icon: String
    let title: String
    let action: () -> Void
    let textColor: Color
    
    init(icon: String, title: String, action: @escaping () -> Void, textColor: Color = .primary) {
        self.icon = icon
        self.title = title
        self.action = action
        self.textColor = textColor
    }
}

struct ToggleSettingsItem {
    let title: String
    let subtitle: String
    let icon: String
    let isChecked: Bool
    let onToggle: () -> Void
    let enabled: Bool
    
    init(title: String, subtitle: String, icon: String, isChecked: Bool, onToggle: @escaping () -> Void, enabled: Bool = true) {
        self.title = title
        self.subtitle = subtitle
        self.icon = icon
        self.isChecked = isChecked
        self.onToggle = onToggle
        self.enabled = enabled
    }
}

#Preview {
    SettingsCard(
        title: "Account Settings",
        items: [
            .action(ActionSettingsItem(
                icon: "person",
                title: "Edit Profile",
                action: {}
            )),
            .action(ActionSettingsItem(
                icon: "lock",
                title: "Change Password",
                action: {}
            )),
            .toggle(ToggleSettingsItem(
                title: "Dark Mode",
                subtitle: "On",
                icon: "moon.fill",
                isChecked: true,
                onToggle: {}
            )),
            .action(ActionSettingsItem(
                icon: "arrow.right.square",
                title: "Logout",
                action: {},
                textColor: .red
            ))
        ]
    )
    .environment(\.isDarkMode, false)
    .padding()
}
