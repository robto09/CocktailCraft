import SwiftUI

struct EndOfListMessage: View {
    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: "checkmark.circle")
                .font(.title)
                .foregroundColor(.secondary)
            
            Text("You've reached the end")
                .font(.subheadline)
                .foregroundColor(.secondary)
            
            Text("That's all the cocktails for now!")
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .padding(.vertical, 20)
        .frame(maxWidth: .infinity)
        .transition(.opacity.combined(with: .scale))
        .animation(.easeInOut, value: true)
    }
}