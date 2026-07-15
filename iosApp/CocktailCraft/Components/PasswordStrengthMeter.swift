import SwiftUI

/// Five-segment meter for the shared AuthInputValidator's 0-5 password
/// strength scale, with the matching label. Used by the sign-up and
/// change-password forms so the visualization can't drift between them.
struct PasswordStrengthMeter: View {
    let strength: Int

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                ForEach(0..<5, id: \.self) { index in
                    Rectangle()
                        .frame(height: 4)
                        .foregroundColor(index < strength ? color : AppColors.lightGray)
                        .cornerRadius(2)
                }
            }

            Text(label)
                .font(.caption)
                .foregroundColor(color)
        }
    }

    private var color: Color {
        switch strength {
        case 0...1: return AppColors.error
        case 2: return AppColors.warning
        case 3: return AppColors.secondaryLight
        case 4...5: return AppColors.success
        default: return AppColors.gray
        }
    }

    private var label: String {
        switch strength {
        case 0...1: return "Weak password"
        case 2: return "Fair password"
        case 3: return "Good password"
        case 4...5: return "Strong password"
        default: return ""
        }
    }
}
