import SwiftUI

struct NetworkErrorStateDisplay: View {
    let error: String
    let isOffline: Bool
    let onRetry: () -> Void
    let onGoOffline: () -> Void
    
    var body: some View {
        VStack(spacing: 20) {
            Spacer()
            
            // Error Icon
            Image(systemName: isOffline ? "wifi.slash" : "exclamationmark.triangle")
                .font(.system(size: 60))
                .foregroundColor(.secondary)
            
            // Error Title
            Text(isOffline ? "You're Offline" : "Connection Error")
                .font(.title2)
                .fontWeight(.semibold)
            
            // Error Message
            Text(error)
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)
            
            // Action Buttons
            VStack(spacing: 12) {
                if !isOffline {
                    Button(action: onRetry) {
                        Label("Try Again", systemImage: "arrow.clockwise")
                            .font(.headline)
                            .foregroundColor(.white)
                            .frame(maxWidth: 200)
                            .padding(.vertical, 12)
                            .background(Color.accentColor)
                            .cornerRadius(10)
                    }
                    
                    Button(action: onGoOffline) {
                        Label("Go Offline", systemImage: "wifi.slash")
                            .font(.headline)
                            .foregroundColor(.accentColor)
                            .frame(maxWidth: 200)
                            .padding(.vertical, 12)
                            .background(
                                RoundedRectangle(cornerRadius: 10)
                                    .stroke(Color.accentColor, lineWidth: 2)
                            )
                    }
                } else {
                    Text("Check your connection and try again")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
            .padding(.top, 20)
            
            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color(.systemBackground))
    }
}