import SwiftUI

struct SettingsRow: View {
    let icon: String
    let title: String
    let subtitle: String?
    let action: () -> Void
    let textColor: Color
    
    init(
        icon: String,
        title: String,
        subtitle: String? = nil,
        textColor: Color = .primary,
        action: @escaping () -> Void
    ) {
        self.icon = icon
        self.title = title
        self.subtitle = subtitle
        self.textColor = textColor
        self.action = action
    }
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 16) {
                Image(systemName: icon)
                    .font(.system(size: 20))
                    .foregroundColor(textColor)
                    .frame(width: 24)
                
                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .font(.system(size: 16))
                        .foregroundColor(textColor)
                    
                    if let subtitle = subtitle {
                        Text(subtitle)
                            .font(.system(size: 12))
                            .foregroundColor(.secondary)
                    }
                }
                
                Spacer()
                
                Image(systemName: "chevron.right")
                    .font(.system(size: 14))
                    .foregroundColor(.gray)
            }
            .padding(.vertical, 12)
        }
        .buttonStyle(PlainButtonStyle())
    }
}

struct SettingsToggleRow: View {
    let icon: String
    let title: String
    let subtitle: String
    @Binding var isOn: Bool
    let isEnabled: Bool
    
    init(
        icon: String,
        title: String,
        subtitle: String,
        isOn: Binding<Bool>,
        isEnabled: Bool = true
    ) {
        self.icon = icon
        self.title = title
        self.subtitle = subtitle
        self._isOn = isOn
        self.isEnabled = isEnabled
    }
    
    var body: some View {
        HStack(spacing: 16) {
            Image(systemName: icon)
                .font(.system(size: 20))
                .foregroundColor(isEnabled ? .primary : .gray)
                .frame(width: 24)
            
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.system(size: 16))
                    .foregroundColor(isEnabled ? .primary : .gray)
                
                Text(subtitle)
                    .font(.system(size: 12))
                    .foregroundColor(.secondary)
            }
            
            Spacer()
            
            Toggle("", isOn: $isOn)
                .labelsHidden()
                .disabled(!isEnabled)
        }
        .padding(.vertical, 12)
    }
}