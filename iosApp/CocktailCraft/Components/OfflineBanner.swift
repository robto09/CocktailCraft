import SwiftUI

struct OfflineBanner: View {
    @ObservedObject var networkMonitor = NetworkMonitor.shared
    @State private var showBanner = false
    
    var body: some View {
        ZStack(alignment: .top) {
            if showBanner && !networkMonitor.isConnected {
                HStack {
                    Image(systemName: "wifi.slash")
                        .foregroundColor(.white)
                    
                    Text("You're offline")
                        .font(.subheadline)
                        .fontWeight(.medium)
                        .foregroundColor(.white)
                    
                    Spacer()
                    
                    Text("Some features may be limited")
                        .font(.caption)
                        .foregroundColor(.white.opacity(0.8))
                }
                .padding(.horizontal)
                .padding(.vertical, 12)
                .background(Color.orange)
                .transition(.move(edge: .top).combined(with: .opacity))
            }
        }
        .animation(.easeInOut(duration: 0.3), value: showBanner)
        .onAppear {
            showBanner = !networkMonitor.isConnected
        }
        .onChange(of: networkMonitor.isConnected) {
            withAnimation {
                showBanner = !networkMonitor.isConnected
            }
        }
    }
}